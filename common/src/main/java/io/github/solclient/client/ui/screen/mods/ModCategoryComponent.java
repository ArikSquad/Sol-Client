package io.github.solclient.client.ui.screen.mods;

import java.util.List;

import io.github.solclient.client.mod.Mod;
import io.github.solclient.client.mod.ModCategory;
import io.github.solclient.client.mod.ModUiStateManager;
import io.github.solclient.client.platform.mc.DrawableHelper;
import io.github.solclient.client.ui.Theme;
import io.github.solclient.client.ui.component.*;
import io.github.solclient.client.ui.component.controller.AlignedBoundsController;
import io.github.solclient.client.ui.component.impl.*;
import io.github.solclient.client.ui.screen.mods.ModsScreen.ModsScreenComponent;
import io.github.solclient.client.util.Utils;
import io.github.solclient.client.util.data.*;

public final class ModCategoryComponent extends Component {

	private static final int WIDTH = 230;
	private static final int HEADER_HEIGHT = 16;
	private static final int ENTRY_SPACING = 5;

	private final ModCategory category;
	private boolean expanded;

	public ModCategoryComponent(ModCategory category, ModsScreenComponent screen) {
		this.category = category;
		this.expanded = ModUiStateManager.INSTANCE.isExpanded(category);

		add(new Header(), new AlignedBoundsController(Alignment.START, Alignment.START));

		List<Mod> mods = category.getMods("");
		for(final Mod mod : mods) {
			ModListing listing = new ModListing(mod, screen, category == ModCategory.PINNED);
			listing.visibilityController((component, defaultVisibility) -> expanded);
			add(listing, new AlignedBoundsController(Alignment.START, Alignment.START,
					(component, defaultBounds) -> {
						int index = subComponents.indexOf(component) - 1;
						return new Rectangle(0, HEADER_HEIGHT + index * (30 + ENTRY_SPACING),
								defaultBounds.getWidth(), defaultBounds.getHeight());
					}));
		}
	}

	@Override
	protected Rectangle getDefaultBounds() {
		int entries = Math.max(0, subComponents.size() - 1);
		return Rectangle.ofDimensions(WIDTH,
				HEADER_HEIGHT + (expanded ? entries * (30 + ENTRY_SPACING) : 0));
	}

	private final class Header extends Component {

		private final ChevronComponent arrow = new ChevronComponent(() -> expanded);
		private final LabelComponent title = new LabelComponent(
				(component, defaultText) -> category.toString(), Theme.fgButton()).scaled(0.8F);

		Header() {
			add(arrow, new AlignedBoundsController(Alignment.START, Alignment.CENTRE,
					(component, defaultBounds) -> new Rectangle(4, 0,
							defaultBounds.getWidth(), defaultBounds.getHeight())));
			add(title, new AlignedBoundsController(Alignment.START, Alignment.CENTRE,
					(component, defaultBounds) -> new Rectangle(16, 0,
							defaultBounds.getWidth(), defaultBounds.getHeight())));
		}

		@Override
		public boolean mouseClicked(ComponentRenderInfo info, int button) {
			if(button != 0) {
				return false;
			}

			Utils.playClickSound(true);
			expanded = !expanded;
			ModUiStateManager.INSTANCE.setExpanded(category, expanded);
			return true;
		}

		@Override
		protected Rectangle getDefaultBounds() {
			return Rectangle.ofDimensions(WIDTH, HEADER_HEIGHT);
		}
	}

}
