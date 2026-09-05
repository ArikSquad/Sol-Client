package io.github.solclient.client.platform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import io.github.solclient.client.Client;
import io.github.solclient.client.platform.mc.MinecraftClient;
import io.github.solclient.client.platform.mc.Window;
import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.platform.mc.resource.Resource;
import io.github.solclient.client.platform.mc.text.OrderedText;
import io.github.solclient.client.platform.mc.text.Text;

/**
 * The OpenGL NanoVG implementation of the shared Sol Client UI.
 *
 * <p>The component API only speaks in terms of {@link RenderBackend}
 * primitives. Keeping the NanoVG context, font, image cache and state in this
 * implementation means the eventual Vulkan renderer can implement
 * {@link UiRenderer} beside it rather than forcing a second UI implementation
 * into every version module.</p>
 */
public final class NanoVGUiRenderer implements UiRenderer {

	// Match the original develop renderer. The PNGs are already authored with
	// the alpha channel that NanoVG expects; asking NanoVG to premultiply them
	// again makes colourised white icon masks backend-dependent.
	private static final int IMAGE_FLAGS = 0;
	private static final String FONT_RESOURCE = "/fonts/Inter-Regular.ttf";

	private final boolean attributeStack;
	private final boolean coreProfile;
	private long context;
	private int font;
	private boolean fontCreated;
	private java.nio.ByteBuffer fontData;
	private boolean frame;
	private int logicalWidth;
	private int logicalHeight;
	private int tint = 0xFFFFFFFF;
	private Identifier boundTexture;
	private RenderBackend nativeFallback = RenderBackend.NONE;
	private final Map<Integer, NVGColor> colours = new HashMap<>();
	private final Map<String, Image> images = new HashMap<>();
	private boolean attributesPushed;
	private int previousActiveTexture;
	private int previousTextureBinding;
	private int previousSampler;
	private boolean textureStateCaptured;

	public NanoVGUiRenderer() {
		this(false, true);
	}

	/**
	 * @param attributeStack true only for the legacy compatibility context.
	 *                       Modern core contexts do not expose glPushAttrib.
	 */
	public NanoVGUiRenderer(boolean attributeStack) {
		this(attributeStack, !attributeStack);
	}

	public NanoVGUiRenderer(boolean attributeStack, boolean coreProfile) {
		this.attributeStack = attributeStack;
		this.coreProfile = coreProfile;
	}

	@Override
	public boolean isAvailable() {
		// Some version adapters use availability while extracting a native
		// screen. Context creation uploads NanoVG's font atlas, so keep that
		// operation inside the same small GL state boundary as a real frame.
		captureTextureState();
		try {
			return ensureContext();
		}
		finally {
			restoreTextureState();
		}
	}

	@Override
	public boolean beginFrame(Window window) {
		if (window == null) {
			return false;
		}

		captureTextureState();
		if (!ensureContext()) {
			restoreTextureState();
			return false;
		}

		logicalWidth = window.scaledWidth();
		logicalHeight = window.scaledHeight();
		if (logicalWidth <= 0 || logicalHeight <= 0) {
			restoreTextureState();
			return false;
		}

		try {
			// Minecraft's GUI coordinates are logical pixels. This mirrors the
			// original develop renderer: NanoVG draws at the logical viewport and
			// the GUI scale is applied once to the GL frame.
			if (attributeStack) {
				GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
				attributesPushed = true;
			}
			// NanoVG's frame size is the framebuffer size.  The component tree
			// operates in Minecraft's logical GUI coordinates, so apply the GUI
			// scale after beginning the physical frame, just like develop's
			// MinecraftUtils.withNvg(). Passing scaled dimensions here shifts the
			// common menu toward the lower-right and clips it away on HiDPI windows.
			NanoVG.nvgBeginFrame(context, window.width(), window.height(), 1.0F);
			NanoVG.nvgScale(context, window.scaleFactor(), window.scaleFactor());
			NanoVG.nvgGlobalCompositeOperation(context, NanoVG.NVG_SOURCE_OVER);
			NanoVG.nvgGlobalAlpha(context, 1.0F);
			NanoVG.nvgResetScissor(context);
			frame = true;
			tint = 0xFFFFFFFF;
			boundTexture = null;
			return true;
		}
		catch (Throwable error) {
			// A renderer failure must not prevent a target from starting. The
			// adapter will render the same common screen through its native
			// fallback for this frame.
			cancelFrame();
			return false;
		}
	}

	@Override
	public void endFrame() {
		if (!frame) {
			return;
		}

		try {
			if (attributeStack) {
				GL11.glDisable(GL11.GL_ALPHA_TEST);
			}
			NanoVG.nvgEndFrame(context);
		}
		finally {
			frame = false;
			if (attributesPushed) {
				attributesPushed = false;
				GL11.glPopAttrib();
			}
			restoreTextureState();
		}
	}

	@Override
	public void setNativeFallback(RenderBackend backend) {
		nativeFallback = backend == null ? RenderBackend.NONE : backend;
	}

	@Override
	public void close() {
		if (frame) {
			cancelFrame();
		}

		for (Image image : images.values()) {
			if (context != 0 && image.handle > 0) {
				NanoVG.nvgDeleteImage(context, image.handle);
			}
			if (image.data != null) {
				MemoryUtil.memFree(image.data);
			}
		}
		images.clear();
		if (fontData != null) {
			MemoryUtil.memFree(fontData);
			fontData = null;
		}

		for (NVGColor colour : colours.values()) {
			colour.free();
		}
		colours.clear();

		if (context != 0) {
			if (coreProfile) NanoVGGL3.nvgDelete(context);
			else NanoVGGL2.nvgDelete(context);
			context = 0;
		}
		font = 0;
		fontCreated = false;
	}

	private void cancelFrame() {
		if (!frame && !attributesPushed) {
			return;
		}
		if (frame) {
			try {
				NanoVG.nvgCancelFrame(context);
			}
			catch (Throwable ignored) {
			}
		}
		frame = false;
		if (attributesPushed) {
			attributesPushed = false;
			try {
				GL11.glPopAttrib();
			}
			catch (Throwable ignored) {
			}
		}
		restoreTextureState();
	}

	/**
	 * Minecraft's modern OpenGL renderer leaves sampler objects and a non-zero
	 * active texture unit bound. NanoVG's GL backend intentionally assumes unit
	 * zero and caches its texture binding, so isolate those two pieces of state
	 * around context creation, atlas uploads and the final flush.
	 */
	private void captureTextureState() {
		if (textureStateCaptured) {
			return;
		}
		previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		previousTextureBinding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		if (coreProfile) {
			// Sampler objects are part of the modern LWJGL 3 binding. The legacy
			// compatibility target only needs the texture-unit portion of this
			// boundary; resolving GL33 methods there produces a linkage error on
			// Minecraft's LWJGL 2 binding.
			previousSampler = GL33.glGetInteger(GL33.GL_SAMPLER_BINDING);
			GL33.glBindSampler(0, 0);
		}
		textureStateCaptured = true;
	}

	private void restoreTextureState() {
		if (!textureStateCaptured) {
			return;
		}
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		if (coreProfile) {
			GL33.glBindSampler(0, previousSampler);
		}
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTextureBinding);
		GL13.glActiveTexture(previousActiveTexture);
		textureStateCaptured = false;
	}

	private boolean ensureContext() {
		if (context != 0 && fontCreated) {
			return true;
		}

		try {
			if (context == 0) {
				int flags = NanoVGGL2.NVG_ANTIALIAS | NanoVGGL2.NVG_STENCIL_STROKES;
				context = coreProfile ? NanoVGGL3.nvgCreate(flags) : NanoVGGL2.nvgCreate(flags);
			}
			if (context == 0) {
				return false;
			}

			if (!fontCreated) {
				try (InputStream input = NanoVGUiRenderer.class.getResourceAsStream(FONT_RESOURCE)) {
					if (input == null) {
						throw new IOException("Missing " + FONT_RESOURCE);
					}
					fontData = read(input);
					font = createFont(fontData);
					// NanoVG's first font is assigned id 0.  Zero is a valid font
					// handle, so keep a separate initialisation bit instead of
					// treating it as a failed creation.
					fontCreated = true;
				}
			}
			return fontCreated;
		}
		catch (Throwable error) {
			// Keep this call compatible with the 1.8 Log4j 2 binding, whose
			// Logger does not expose the modern varargs overloads.
			Client.LOGGER.error("Could not initialise NanoVG UI context (coreProfile="
					+ coreProfile + "): " + error);
			if (context != 0) {
				try {
					if (coreProfile) NanoVGGL3.nvgDelete(context);
					else NanoVGGL2.nvgDelete(context);
				}
				catch (Throwable ignored) {
				}
			}
			context = 0;
			font = 0;
			fontCreated = false;
			if (fontData != null) {
				MemoryUtil.memFree(fontData);
				fontData = null;
			}
			return false;
		}
	}

	/**
	 * LWJGL changed the final parameter from an integer flag to a boolean in
	 * 3.3.3.  Common is compiled against the legacy-compatible API because the
	 * 1.8 module still ships the older binding, so resolve the tiny ABI change at
	 * the renderer boundary instead of making the UI version-specific.
	 */
	private int createFont(java.nio.ByteBuffer data) throws Exception {
		try {
			Method modern = NanoVG.class.getMethod("nvgCreateFontMem", long.class,
					CharSequence.class, java.nio.ByteBuffer.class, boolean.class);
			return ((Number) modern.invoke(null, context, "sol-client-ui", data, false)).intValue();
		}
		catch(NoSuchMethodException error) {
			Method legacy = NanoVG.class.getMethod("nvgCreateFontMem", long.class,
					CharSequence.class, java.nio.ByteBuffer.class, int.class);
			return ((Number) legacy.invoke(null, context, "sol-client-ui", data, 0)).intValue();
		}
	}

	private static java.nio.ByteBuffer read(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int read;
		while ((read = input.read(buffer)) >= 0) {
			if (read > 0) {
				output.write(buffer, 0, read);
			}
		}
		byte[] bytes = output.toByteArray();
		java.nio.ByteBuffer result = MemoryUtil.memAlloc(bytes.length);
		result.put(bytes);
		result.flip();
		return result;
	}

	private NVGColor colour(int value) {
		NVGColor colour = colours.get(value);
		if (colour == null) {
			// Use owned native memory. In modern LWJGL, create() wraps a direct
			// buffer and must not be released through Struct.free().
			colour = NVGColor.malloc();
			NanoVG.nvgRGBA((byte) ((value >> 16) & 0xFF), (byte) ((value >> 8) & 0xFF),
					(byte) (value & 0xFF), (byte) ((value >> 24) & 0xFF), colour);
			colours.put(value, colour);
		}
		return colour;
	}

	private void fillPath(int colour) {
		NanoVG.nvgFillColor(context, colour(colour));
		NanoVG.nvgFill(context);
	}

	private void strokePath(int colour) {
		NanoVG.nvgStrokeColor(context, colour(colour));
		NanoVG.nvgStroke(context);
	}

	@Override
	public void fillRect(float left, float top, float right, float bottom, int colour) {
		if (!frame) return;
		NanoVG.nvgBeginPath(context);
		NanoVG.nvgRect(context, left, top, right - left, bottom - top);
		fillPath(colour);
	}

	@Override
	public void fillRoundedRect(float left, float top, float right, float bottom,
			float radius, int colour) {
		if (!frame) return;
		float width = Math.max(0.0F, right - left);
		float height = Math.max(0.0F, bottom - top);
		if (width <= 0.0F || height <= 0.0F) return;
		NanoVG.nvgBeginPath(context);
		NanoVG.nvgRoundedRect(context, left, top, width, height,
				Math.max(0.0F, Math.min(radius, Math.min(width, height) / 2.0F)));
		fillPath(colour);
	}

	@Override
	public void fillGradientRect(int left, int top, int right, int bottom,
			int topColour, int bottomColour) {
		if (!frame) return;
		NVGPaint paint = NVGPaint.malloc();
		try {
			NanoVG.nvgBeginPath(context);
			NanoVG.nvgRect(context, left, top, right - left, bottom - top);
			NanoVG.nvgLinearGradient(context, left, top, left, bottom,
					colour(topColour), colour(bottomColour), paint);
			NanoVG.nvgFillPaint(context, paint);
			NanoVG.nvgFill(context);
		}
		finally {
			paint.free();
		}
	}

	@Override
	public void strokeRect(int left, int top, int right, int bottom, int colour) {
		if (!frame) return;
		NanoVG.nvgBeginPath(context);
		NanoVG.nvgRect(context, left + 0.5F, top + 0.5F,
				right - left - 1.0F, bottom - top - 1.0F);
		NanoVG.nvgStrokeWidth(context, 1.0F);
		strokePath(colour);
	}

	@Override
	public void fillTexturedRect(int x, int y, float u, float v, int width, int height,
			int textureWidth, int textureHeight) {
		if (!frame || boundTexture == null || width <= 0 || height <= 0) return;
		Image image = image(boundTexture);
		if (image == null || image.handle <= 0) return;

		float sourceWidth = textureWidth <= 0 ? image.width : textureWidth;
		float sourceHeight = textureHeight <= 0 ? image.height : textureHeight;
		float scaleX = width / sourceWidth;
		float scaleY = height / sourceHeight;
		NVGPaint paint = NVGPaint.malloc();
		try {
			NanoVG.nvgImagePattern(context, x - u * scaleX, y - v * scaleY,
					sourceWidth * scaleX, sourceHeight * scaleY, 0.0F, image.handle, 1.0F, paint);
			// The common icon set is a white mask. NanoVG's image paint uses the
			// inner colour as the mask tint, exactly as develop's IconComponent did.
			paint.innerColor(colour(tint));
			NanoVG.nvgBeginPath(context);
			NanoVG.nvgRect(context, x, y, width, height);
			NanoVG.nvgFillPaint(context, paint);
			NanoVG.nvgFill(context);
		}
		finally {
			paint.free();
		}
	}

	private Image image(Identifier identifier) {
		String key = identifier.namespace() + ":" + identifier.path();
		Image cached = images.get(key);
		if (cached != null) return cached;

		Resource resource = firstResource(identifier);
		if (resource == null && "minecraft".equals(identifier.namespace())) {
			resource = firstResource(Identifier.solClient(identifier.path()));
		}
		if (resource == null && "minecraft".equals(identifier.namespace())) {
			resource = firstResource(Identifier.create("sol-client", identifier.path()));
		}
		if (resource == null) return null;

		java.nio.ByteBuffer data = null;
		try (InputStream input = resource.getInput()) {
			data = read(input);
			int handle = NanoVG.nvgCreateImageMem(context, IMAGE_FLAGS, data);
			if (handle <= 0) {
				MemoryUtil.memFree(data);
				return null;
			}
			int[] width = new int[1];
			int[] height = new int[1];
			NanoVG.nvgImageSize(context, handle, width, height);
			Image image = new Image(handle, width[0], height[0], data);
			images.put(key, image);
			return image;
		}
		catch (IOException | RuntimeException error) {
			if (data != null) MemoryUtil.memFree(data);
			return null;
		}
	}

	private Resource firstResource(Identifier identifier) {
		try {
			java.util.List<Resource> resources = MinecraftClient.getInstance().getResourceManager().getResources(identifier);
			return resources == null || resources.isEmpty() ? null : resources.get(resources.size() - 1);
		}
		catch (Throwable ignored) {
			return null;
		}
	}

	@Override
	public int render(String text, int x, int y, int rgb, boolean shadow) {
		if (!frame || text == null || text.isEmpty()) return x;
		bindFont();
		if (shadow) {
			NanoVG.nvgFillColor(context, colour((rgb & 0x00FFFFFF) | 0xA0000000));
			NanoVG.nvgText(context, x + 1.0F, y + lineHeight() + 1.0F, text);
		}
		NanoVG.nvgFillColor(context, colour(rgb));
		NanoVG.nvgText(context, x, y + lineHeight(), text);
		return x + getTextWidth(text);
	}

	@Override
	public int render(Text text, int x, int y, int rgb, boolean shadow) {
		return render(text == null ? "" : text.getPlain(), x, y, rgb, shadow);
	}

	@Override
	public int render(OrderedText text, int x, int y, int rgb, boolean shadow) {
		return render((Text) text, x, y, rgb, shadow);
	}

	private void bindFont() {
		NanoVG.nvgFontFaceId(context, font);
		NanoVG.nvgFontSize(context, 8.0F);
		NanoVG.nvgTextAlign(context, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_BASELINE);
	}

	private float lineHeight() {
		bindFont();
		float[] ascender = new float[1];
		float[] descender = new float[1];
		float[] height = new float[1];
		NanoVG.nvgTextMetrics(context, ascender, descender, height);
		return height[0];
	}

	@Override
	public int getTextWidth(String text) {
		if (!frame || text == null || text.isEmpty()) return 0;
		bindFont();
		float[] bounds = new float[4];
		NanoVG.nvgTextBounds(context, 0.0F, 0.0F, text, bounds);
		return Math.max(0, Math.round(bounds[2]));
	}

	@Override
	public int getTextWidth(Text text) {
		return getTextWidth(text == null ? "" : text.getPlain());
	}

	@Override
	public int getTextWidth(OrderedText text) {
		return getTextWidth((Text) text);
	}

	@Override
	public int getCharacterWidth(char character) {
		return getTextWidth(String.valueOf(character));
	}

	@Override
	public int getHeight() {
		return frame ? Math.max(1, Math.round(lineHeight())) : 9;
	}

	@Override public void pushMatrix() { if (frame) NanoVG.nvgSave(context); }
	@Override public void popMatrix() { if (frame) NanoVG.nvgRestore(context); }
	@Override public void translate(double x, double y, double z) { if (frame) NanoVG.nvgTranslate(context, (float) x, (float) y); }
	@Override public void scale(double x, double y, double z) { if (frame) NanoVG.nvgScale(context, (float) x, (float) y); }
	@Override public void rotate(float angle, float x, float y, float z) { if (frame) NanoVG.nvgRotate(context, (float) Math.toRadians(angle)); }

	@Override public void bindTexture(Identifier identifier) { boundTexture = identifier; }
	@Override public void colour(float r, float g, float b, float a) {
		int red = Math.max(0, Math.min(255, Math.round(r * 255.0F)));
		int green = Math.max(0, Math.min(255, Math.round(g * 255.0F)));
		int blue = Math.max(0, Math.min(255, Math.round(b * 255.0F)));
		int alpha = Math.max(0, Math.min(255, Math.round(a * 255.0F)));
		tint = (alpha << 24) | (red << 16) | (green << 8) | blue;
	}
	@Override public void lineWidth(float width) { if (frame) NanoVG.nvgStrokeWidth(context, width); }
	@Override public void resetLineWidth() { if (frame) NanoVG.nvgStrokeWidth(context, 1.0F); }
	@Override public void defaultBackground() { fillRect(0, 0, logicalWidth, logicalHeight, 0xFF141414); }
	@Override public void renderPanorama(int width, int height, float tickDelta) { /* native adapter owns the panorama */ }
	@Override public void scissor(double x, double y, double width, double height) {
		if (frame) NanoVG.nvgIntersectScissor(context, (float) x, (float) y, (float) width, (float) height);
	}
	@Override public void resetScissor() { if (frame) NanoVG.nvgResetScissor(context); }

	@Override public void renderItem(Object item, int x, int y) { nativeFallback.renderItem(item, x, y); }
	@Override public void renderItemDecorations(Object item, int x, int y) { nativeFallback.renderItemDecorations(item, x, y); }

	private static final class Image {
		private final int handle;
		private final int width;
		private final int height;
		private final java.nio.ByteBuffer data;

		private Image(int handle, int width, int height, java.nio.ByteBuffer data) {
			this.handle = handle;
			this.width = width;
			this.height = height;
			this.data = data;
		}
	}

}
