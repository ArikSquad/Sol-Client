/**
 * Modified from danterus' mixin-client-template.
 *
 * MIT License
 *
 * Copyright © 2022 danterusdev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.solclient.client.wrapper;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;

import io.github.solclient.client.Constants;
import io.github.solclient.client.wrapper.mixin.ClientMixinService;
import io.github.solclient.client.wrapper.transformer.Transformer;
import io.github.solclient.client.wrapper.transformer.impl.guava.*;
import io.github.solclient.client.wrapper.transformer.impl.mc.PackageAccessFixer;

/**
 * A class loader which modifies classes and applies mixins.
 */
public final class WrapperClassLoader extends ClassLoader {

	public static final WrapperClassLoader INSTANCE = new WrapperClassLoader();
	private static final Logger LOGGER = LogManager.getLogger();

	private final List<String> exclusions = new ArrayList<>();
	private final Map<String, String> preferredResourceSources = new LinkedHashMap<>();
	private final ClassLoader parent = WrapperClassLoader.class.getClassLoader();
	private final List<Transformer> transformers = new ArrayList<>();

	private WrapperClassLoader() {
		addDefaultExclusions();
		registerDefaultTransformers();

		MixinBootstrap.init();
		MixinExtrasBootstrap.init();
	}

	private void addDefaultExclusions() {
		excludePackage("java");
		excludePackage("jdk");
		excludePackage("javax");

		excludePackage("sun");
		excludePackage("com.sun");
		excludePackage("org.xml");
		excludePackage("org.w3c");

		excludePackage("org.apache");
		excludePackage("org.slf4j");
		excludePackage("com.mojang.blocklist");

		excludePackage("io.github.solclient.client.wrapper");

		excludePackage("org.spongepowered.asm");
		excludePackage("org.objectweb.asm");
	}

	public void excludePackage(String name) {
		exclusions.add(name);
	}

	private void registerDefaultTransformers() {
		// Patch guava classes to support old Minecraft versions.
		registerTransformer(new LegacyObjectsTransformer());
		registerTransformer(new LegacyFuturesTransformer());
		registerTransformer(new LegacyIteratorsTransformer());

		if(Constants.DEV) {
			registerTransformer(new PackageAccessFixer());
		}
	}

	public void registerTransformer(Transformer transformer) {
		transformers.add(transformer);
	}

	/**
	 * Prefer classes from a specific library when the legacy launcher has more
	 * than one binding for the same package on its parent class path. This is
	 * intentionally a small class-loader seam: Minecraft 1.8 ships LWJGL 2,
	 * while Sol's patched LWJGL 3 NanoVG bridge uses relocated pointer-buffer
	 * classes and must be selected as one coherent set.
	 */
	public void preferResourceSource(String packageName, String sourceMarker) {
		if (packageName == null || packageName.isEmpty() || sourceMarker == null || sourceMarker.isEmpty()) {
			return;
		}
		preferredResourceSources.put(packageName + '.', sourceMarker);
	}

	private final Map<String, Class<?>> cache = new HashMap<>();

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		for(String exclusion : exclusions) {
			if(name.startsWith(exclusion + '.')) {
				try {
					return parent.loadClass(name);
				}
				catch(ClassNotFoundException ignored) {}
			}
		}

		Class<?> clazz = super.findLoadedClass(name);
		if(clazz == null) {
			clazz = cache.get(name);
		}

		if(clazz == null) {
			byte[] data = getModifiedBytes(name);
			clazz = super.defineClass(name, data, 0, data.length);
			cache.put(name, clazz);
		}

		return clazz;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> clazz = this.loadClass(name);

		if(resolve) {
			resolveClass(clazz);
		}

		return clazz;
	}

	public byte[] getModifiedBytes(String name) throws ClassNotFoundException {
		byte[] data = loadClassData(name);

		if(data == null) {
			if((data = ClientMixinService.getInstance().getTransformer()
					.generateClass(MixinEnvironment.getDefaultEnvironment(), name)) == null) {
				throw new ClassNotFoundException();
			}
		}

		String internalName = name.replace(".", "/");

		List<Transformer> applicableTransformers = transformers.stream()
				.filter((transformer) -> transformer.willModify(internalName)).collect(Collectors.toList());

		if(!applicableTransformers.isEmpty()) {
			try {
				ClassReader reader = new ClassReader(data);
				ClassNode node = new ClassNode();

				reader.accept(node, 0);

				for(Transformer transformer : applicableTransformers) {
					transformer.accept(node);
				}

				ClassWriter writer = new ClassWriter(0);
				node.accept(writer);
				data = writer.toByteArray();
			}
			catch(ClassNotFoundException | IOException error) {
				throw new ClassNotFoundException(name, error);
			}
		}

		try {
			data = ClientMixinService.getInstance().getTransformer()
					.transformClass(MixinEnvironment.getDefaultEnvironment(), name, data);
		}
		catch(Throwable error) {
			throw new ClassNotFoundException(name, error);
		}

		return data;
	}

	private byte[] loadClassData(String className) {
		try {
			Enumeration<URL> resources = getResources(className.replace(".", "/") + ".class");
			URL first = null;
			URL preferred = null;

			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				if (first == null) {
					first = resource;
				}
				for (Map.Entry<String, String> entry : preferredResourceSources.entrySet()) {
					if (className.startsWith(entry.getKey())
							&& resource.toString().contains(entry.getValue())) {
						preferred = resource;
						break;
					}
				}
			}

			URL selected = preferred == null ? first : preferred;
			if (selected != null) {
				return IOUtils.toByteArray(selected);
			}
		}
		catch(IOException error) {
			LOGGER.error(error);
		}

		return null;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		return parent.getResources(name);
	}

	@Override
	public URL getResource(String name) {
		return parent.getResource(name);
	}

}
