package io.github.solclient.client.mod;

import java.util.*;
import java.util.stream.Collectors;

import io.github.solclient.client.Client;
import io.github.solclient.client.platform.mc.lang.I18n;
import lombok.RequiredArgsConstructor;

/**
 * Categories of Sol Client mods.
 */
@RequiredArgsConstructor
public enum ModCategory {
	/**
	 * All mods.
	 */
	ALL(false),
	PINNED(true),
	/**
	 * General uncategorisable mods.
	 */
	GENERAL(true),
	/**
	 * HUD widgets.
	 */
	HUD(true),
	/**
	 * Utility mods.
	 */
	UTILITY(true),
	/**
	 * Aesthetic/graphical mods.
	 */
	VISUAL(true),
	/**
	 * Integration mods.
	 */
	INTEGRATION(true);

	private final boolean showName;
	private List<Mod> mods;

	@Override
	public String toString() {
		return showName ? I18n.translate("sol_client.mod.category." + name().toLowerCase(Locale.ROOT) + ".name") : null;
	}

	public List<Mod> getMods(String filter) {
		if(this == ALL) {
			mods = Client.INSTANCE.getMods();
		}
		else if(this == PINNED) {
			mods = ModUiStateManager.INSTANCE.getPins();
		}
		else if(mods == null) {
			mods = Client.INSTANCE.getMods().stream().filter((mod) -> mod.getCategory() == ModCategory.this)
					.collect(Collectors.toList());
		}

		if(filter.isEmpty()) {
			return mods;
		}

		final String query = filter.toLowerCase(Locale.ROOT);
		return mods.stream().filter((mod) -> mod.getName().toLowerCase(Locale.ROOT).contains(query)
				|| mod.getDescription().toLowerCase(Locale.ROOT).contains(query)
				|| (mod.getDetail() != null && mod.getDetail().toLowerCase(Locale.ROOT).contains(query)))
				.sorted((o1, o2) -> {
					return Boolean.compare(!o1.getName().toLowerCase(Locale.ROOT).startsWith(query),
							!o2.getName().toLowerCase(Locale.ROOT).startsWith(query));
				}).collect(Collectors.toList());
	}

}
