package io.github.solclient.client.util;

import java.io.*;
import java.util.*;

import com.google.gson.*;

import io.github.solclient.client.platform.mc.MinecraftClient;
import io.github.solclient.client.platform.mc.resource.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TranslationProvider {

	private static final Map<String, String> TRANSLATIONS = new HashMap<>();

	public void clear() {
		TRANSLATIONS.clear();
	}

	public void accept(InputStream json) {
		JsonObject obj = JsonParser.parseReader(new InputStreamReader(json, java.nio.charset.StandardCharsets.UTF_8))
				.getAsJsonObject();
		for(Map.Entry<String, JsonElement> entry : obj.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().getAsString();
			TRANSLATIONS.put(key, value);
			// Keep both the canonical common spelling and the unqualified spelling
			// used by the first converted JSON packs.
			if(key.startsWith("sol_client.")) {
				TRANSLATIONS.putIfAbsent(key.substring("sol_client.".length()), value);
			}
			else {
				TRANSLATIONS.putIfAbsent("sol_client." + key, value);
			}
		}
	}

	public String translate(String key) {
		String value = TRANSLATIONS.get(key);
		if(value == null && key.startsWith("sol_client.")) {
			// JSON packs produced by the first multi-version sketch omitted the
			// namespace from custom keys. Keep those packs readable while the
			// canonical key remains sol_client.* everywhere in common code.
			value = TRANSLATIONS.get(key.substring("sol_client.".length()));
		}
		if(value == null && !key.startsWith("sol_client.")) {
			value = TRANSLATIONS.get("sol_client." + key);
		}
		return value == null ? key : value;
	}

	public static Boolean hasTranslation(String key) {
		return TRANSLATIONS.containsKey(key)
				|| (key.startsWith("sol_client.")
						&& TRANSLATIONS.containsKey(key.substring("sol_client.".length())))
				|| (!key.startsWith("sol_client.")
						&& TRANSLATIONS.containsKey("sol_client." + key));
	}

	public int size() {
		return TRANSLATIONS.size();
	}

	public static Identifier getLanguageId(String id) {
		return Identifier.solClient("lang/" + id + ".json");
	}

	public static void load() {
		clear();

		try {
			MinecraftClient mc = MinecraftClient.getInstance();
			ResourceManager resourceManager = mc.getResourceManager();

			String selected = mc.getOptions().languageCode();
			if(selected == null || selected.isEmpty()) selected = "en_us";

			loadLanguage(resourceManager, "en_us");
			if(!"en_us".equalsIgnoreCase(selected)) {
				loadLanguage(resourceManager, selected);
			}
		}
		catch(IOException ignored) {
			// Some loaders rebuild their resource manager asynchronously. The
			// classpath fallback in loadLanguage keeps the shaded common resources
			// available during that transition.
		}
	}

	private void loadLanguage(ResourceManager resourceManager, String language) throws IOException {
		List<Resource> resources = resourceManager.getResources(Identifier.solClient("lang/" + language + ".json"));
		if(!resources.isEmpty()) {
			for(Resource resource : resources) {
				try(InputStream in = resource.getInput()) {
					accept(in);
				}
			}
			return;
		}

		ClassLoader loader = TranslationProvider.class.getClassLoader();
		String[] paths = {
			"assets/sol_client/lang/" + language + ".json",
			"assets/sol-client/lang/" + language + ".json"
		};
		for(String path : paths) {
			try(InputStream in = loader == null ? ClassLoader.getSystemResourceAsStream(path)
					: loader.getResourceAsStream(path)) {
				if(in != null) {
					accept(in);
					return;
				}
			}
		}
	}

}
