package io.github.solclient.client.mod;

import java.util.*;

import com.google.gson.*;

import io.github.solclient.client.Client;
import lombok.Getter;

/**
 * Shared UI state for the redesigned mod browser.
 *
 * <p>This deliberately belongs to common rather than a loader module. The
 * 1.8 and modern screens therefore have the same pin order and collapsed
 * category behaviour, while the version modules only render the pixels.</p>
 */
public final class ModUiStateManager {

	public static final ModUiStateManager INSTANCE = new ModUiStateManager();

	@Getter
	private final List<Mod> pins = new LinkedList<>();
	private final Set<ModCategory> collapsedCategories = new HashSet<>();

	private ModUiStateManager() {
	}

	public void load(JsonObject root) {
		pins.clear();
		collapsedCategories.clear();

		if(root == null || !root.has("ui")) {
			return;
		}

		JsonObject ui = root.getAsJsonObject("ui");
		if(ui.has("pins")) {
			for(JsonElement element : ui.getAsJsonArray("pins")) {
				Mod mod = Client.INSTANCE.getModById(element.getAsString());
				if(mod != null && !pins.contains(mod)) {
					pins.add(mod);
					mod.notifyPin();
				}
			}
		}

		if(ui.has("collapsedCategories")) {
			for(JsonElement element : ui.getAsJsonArray("collapsedCategories")) {
				try {
					collapsedCategories.add(ModCategory.valueOf(element.getAsString()));
				}
				catch(IllegalArgumentException ignored) {
					// A removed category should not prevent the rest of the UI state
					// from loading.
				}
			}
		}
	}

	public void save(JsonObject root) {
		JsonObject ui = new JsonObject();
		JsonArray savedPins = new JsonArray();
		for(Mod mod : pins) {
			savedPins.add(new JsonPrimitive(mod.getId()));
		}
		ui.add("pins", savedPins);

		JsonArray collapsed = new JsonArray();
		for(ModCategory category : collapsedCategories) {
			collapsed.add(new JsonPrimitive(category.name()));
		}
		ui.add("collapsedCategories", collapsed);
		root.add("ui", ui);
	}

	public boolean isExpanded(ModCategory category) {
		return !collapsedCategories.contains(category);
	}

	public void setExpanded(ModCategory category, boolean expanded) {
		if(expanded) {
			collapsedCategories.remove(category);
		}
		else {
			collapsedCategories.add(category);
		}
	}

	public void notifyPin(Mod mod) {
		if(!pins.contains(mod)) {
			pins.add(mod);
		}
	}

	public void notifyUnpin(Mod mod) {
		pins.remove(mod);
	}

	public void reorderPin(Mod mod, int newIndex) {
		if(!pins.remove(mod)) {
			return;
		}
		pins.add(Math.max(0, Math.min(newIndex, pins.size())), mod);
	}

}
