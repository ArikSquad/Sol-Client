package io.github.solclient.client.ui.screen.mods;

import java.util.Locale;

import io.github.solclient.client.Client;
import io.github.solclient.client.mod.*;
import io.github.solclient.client.ui.component.Component;
import io.github.solclient.client.ui.component.controller.AlignedBoundsController;
import io.github.solclient.client.ui.component.impl.*;
import io.github.solclient.client.ui.screen.mods.ModsScreen.ModsScreenComponent;
import io.github.solclient.client.util.data.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ModsScroll extends ScrollListComponent {

	private final ModsScreenComponent screen;

	@Override
	protected int getScrollStep() {
		return 30 + getSpacing();
	}

	public void load() {
		clear();

		if(screen.getMod() != null) {
			for(ModOption option : screen.getMod().getOptions()) {
				add(new ModOptionComponent(option));
			}
			return;
		}

		String query = screen.getQuery().trim();
		if(query.isEmpty()) {
			for(ModCategory category : ModCategory.values()) {
				if(category == ModCategory.ALL || category.getMods("").isEmpty()) {
					continue;
				}
				add(new ModCategoryComponent(category, screen));
			}
		}
		else {
			String lowerQuery = query.toLowerCase(Locale.ROOT);
			boolean found = false;
			for(Mod mod : Client.INSTANCE.getMods()) {
				String detail = mod.getDetail() == null ? "" : mod.getDetail();
				if(mod.getCategory() == null || mod.getName().toLowerCase(Locale.ROOT).contains(lowerQuery)
						|| mod.getDescription().toLowerCase(Locale.ROOT).contains(lowerQuery)
						|| detail.toLowerCase(Locale.ROOT).contains(lowerQuery)) {
					add(new ModListing(mod, screen));
					found = true;
				}
			}

			if(!found) {
				add(new LabelComponent("sol_client.no_results"),
						new AlignedBoundsController(Alignment.CENTRE, Alignment.CENTRE));
			}
		}
	}

	@Override
	public int getSpacing() {
		return 5;
	}

}
