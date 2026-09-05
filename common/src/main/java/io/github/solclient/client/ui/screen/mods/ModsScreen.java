package io.github.solclient.client.ui.screen.mods;

import io.github.solclient.client.Client;
import io.github.solclient.client.mod.Mod;
import io.github.solclient.client.mod.ModUiStateManager;
import io.github.solclient.client.mod.impl.SolClientConfig;
import io.github.solclient.client.platform.mc.lang.I18n;
import io.github.solclient.client.platform.mc.screen.Screen;
import io.github.solclient.client.platform.mc.text.Text;
import io.github.solclient.client.platform.mc.util.Input;
import io.github.solclient.client.ui.Theme;
import io.github.solclient.client.ui.component.*;
import io.github.solclient.client.ui.component.controller.*;
import io.github.solclient.client.ui.component.impl.*;
import io.github.solclient.client.ui.screen.PanoramaBackgroundScreen;
import io.github.solclient.client.util.Utils;
import io.github.solclient.client.util.data.*;
import lombok.Getter;

public final class ModsScreen extends PanoramaBackgroundScreen {

	private final ModsScreenComponent component;

	public ModsScreen() {
		this(null);
	}

	public ModsScreen(Mod mod) {
		super(Text.translation("sol_client.mod.screen.title"), new ModsRoot(mod));
		component = (ModsScreenComponent) root.getSubComponents().get(0);
		background = false;
	}

	@Override
	public void renderScreen(int x, int y, float tickDelta) {
		if(!mc.hasLevel()) {
			if(SolClientConfig.INSTANCE.fancyMainMenu) {
				background = false;
				renderPanorama(x, y, tickDelta);
			}
			else {
				background = true;
			}
		}
		else {
			// The redesigned screen is opaque over the world.  Let the shared
			// screen pass draw the full logical viewport, so NanoVG and the
			// native fallback have identical behaviour.
			background = true;
		}

		super.renderScreen(x, y, tickDelta);
	}

	@Override
	public boolean wantsPanorama() {
		return !mc.hasLevel() && SolClientConfig.INSTANCE.fancyMainMenu;
	}

	public void switchMod(Mod mod) {
		component.switchMod(mod);
	}

	@Override
	protected void onClose() {
		Client.INSTANCE.save();
	}

	@Override
	public void closeAll() {
		if(!mc.hasLevel() && SolClientConfig.INSTANCE.fancyMainMenu && parentScreen != null) {
			mc.setScreen(parentScreen);
			return;
		}

		super.closeAll();
	}

	private static final class ModsRoot extends Component {

		ModsRoot(Mod mod) {
			add(new ModsScreenComponent(mod), new AlignedBoundsController(Alignment.CENTRE, Alignment.CENTRE));
		}
	}

	public static final class ModsScreenComponent extends BlockComponent {

		@Getter
		private Mod mod;
		private TextFieldComponent search;
		private ModsScroll scroll;
		private int noModsScroll;
		private boolean singleModMode;
		private ModListing pendingPinDrag;
		private int pendingPinDragX;
		private int pendingPinDragY;

		public ModsScreenComponent(Mod startingMod) {
			super(Theme.bg(), 12F);

			if(startingMod != null) {
				singleModMode = true;
			}

			add(new LabelComponent((component, defaultText) -> mod != null ? mod.getName()
					: I18n.translate("sol_client.mod.screen.title"), Theme.fg()).scaled(1.45F),
					new AlignedBoundsController(Alignment.START, Alignment.START,
							(component, defaultBounds) -> new Rectangle(getBaseX() + (mod != null && !singleModMode ? 24 : 0),
									getBaseX() + 2, defaultBounds.getWidth(), defaultBounds.getHeight())));

			final ButtonComponent done = ButtonComponent.done(() -> {
				if(mod == null || singleModMode) {
					if(!search.getText().isEmpty()) {
						search.setText("");
						search.setFocused(false);
						scroll.load();
					}
					screen.close();
				}
				else {
					switchMod(null);
				}
			}).width(50);
			add(done, new AlignedBoundsController(Alignment.END, Alignment.START,
					(component, defaultBounds) -> defaultBounds.offset(-getBaseX(), getBaseX())));

			if(!singleModMode) {
				add(new ButtonComponent("sol_client.hud.edit", Theme.button(), Theme.fg())
						.withIcon("sol_client_hud").width(60)
						.onClick((info, button) -> {
							if(button != 0) {
								return false;
							}
							Utils.playClickSound(true);
							mc.setScreen(new MoveHudsScreen());
							return true;
						}),
						(component, defaultBounds) -> defaultBounds.offset(done.getBounds().getX()
								- defaultBounds.getWidth() - 4, getBaseX()));
			}

			add(scroll = new ModsScroll(this), (component, defaultBounds) -> new Rectangle(0, 60,
					getBounds().getWidth(), getBounds().getHeight() - 60));

			search = new TextFieldComponent(230, false).autoFlush().onUpdate((value) -> {
				scroll.snapTo(0);
				scroll.load();
				return true;
			}).placeholder("sol_client.mod.screen.search").withIcon("sol_client_search");

			switchMod(startingMod, true);
		}

		private int getBaseX() {
			return getBounds().getWidth() / 2 - 230 / 2;
		}

		public void singleModMode() {
			singleModMode = true;
		}

		public void switchMod(Mod mod) {
			switchMod(mod, false);
		}

		public void refreshModList() {
			if(mod == null) {
				scroll.load();
			}
		}

		void beginPinDrag(ModListing listing, ComponentRenderInfo listingInfo) {
			if(pendingPinDrag != null || ModUiStateManager.INSTANCE.getPins().size() < 2) {
				return;
			}

			Rectangle listingBounds = getRelativeBounds(listing);
			pendingPinDrag = listing;
			pendingPinDragX = listingBounds.getX() + listingInfo.getRelativeMouseX();
			pendingPinDragY = listingBounds.getY() + listingInfo.getRelativeMouseY();
		}

		private Rectangle getRelativeBounds(Component child) {
			Rectangle childBounds = child.getBounds();
			int x = 0;
			int y = 0;
			Component current = child;

			while(current != this && current.getParent() != null) {
				Rectangle bounds = current.getBounds();
				x += bounds.getX();
				y += bounds.getY();
				current = current.getParent();
			}

			return new Rectangle(x, y, childBounds.getWidth(), childBounds.getHeight());
		}

		private void finishPinDrag(ComponentRenderInfo info, int button) {
			if(button != 0 || pendingPinDrag == null) {
				return;
			}

			ModListing listing = pendingPinDrag;
			pendingPinDrag = null;
			boolean moved = Math.abs(info.getRelativeMouseX() - pendingPinDragX) > 2
					|| Math.abs(info.getRelativeMouseY() - pendingPinDragY) > 2;

			if(!moved) {
				Utils.playClickSound(true);
				listing.activatePrimaryAction();
				return;
			}

			int pinCount = ModUiStateManager.INSTANCE.getPins().size();
			int contentY = info.getRelativeMouseY() - scroll.getBounds().getY() + scroll.getScroll();
			int newIndex = (contentY - 16 + 17) / 35;
			newIndex = Utils.clamp(newIndex, 0, pinCount - 1);
			ModUiStateManager.INSTANCE.reorderPin(listing.getMod(), newIndex);
			scroll.load();
		}

		@Override
		public boolean mouseReleasedAnywhere(ComponentRenderInfo info, int button, boolean inside) {
			if(pendingPinDrag != null && button == 0) {
				finishPinDrag(info, button);
				return true;
			}
			return super.mouseReleasedAnywhere(info, button, inside);
		}

		public void switchMod(Mod mod, boolean first) {
			if(mod == null) {
				if(!getSubComponents().contains(search)) {
					add(search, (component, defaultBounds) -> new Rectangle(getBaseX(), 38,
							230, defaultBounds.getHeight()));
				}
				if(!first && getSubComponents().contains(backButton())) {
					remove(backButton());
				}
				this.mod = null;
				scroll.load();
				return;
			}

			if(!getSubComponents().contains(scroll)) {
				add(scroll, (component, defaultBounds) -> new Rectangle(0, 60,
						getBounds().getWidth(), getBounds().getHeight() - 60));
			}
			if(this.mod == null && !singleModMode && !getSubComponents().contains(back)) {
				add(back, new AlignedBoundsController(Alignment.START, Alignment.START,
						(component, defaultBounds) -> defaultBounds.offset(getBaseX(), getBaseX() + 2)));
			}
			if(!first && getSubComponents().contains(search)) {
				remove(search);
			}

			this.mod = mod;
			scroll.load();
		}

		private final ButtonComponent back = new ButtonComponent("", Theme.button(), Theme.fg())
				.withIcon("sol_client_previous").width(16).height(16)
				.onClick((info, button) -> {
					if(button != 0) {
						return false;
					}
					Utils.playClickSound(true);
					switchMod(null);
					return true;
				});

		private ButtonComponent backButton() {
			return back;
		}

		@Override
		public boolean keyPressed(ComponentRenderInfo info, int code, int scancode, int mods) {
			boolean result = super.keyPressed(info, code, scancode, mods);

			if((screen.getRoot().getDialog() == null && (code == Input.ENTER || code == Input.KP_ENTER))
					&& !scroll.getSubComponents().isEmpty()) {
				Component firstComponent = scroll.getSubComponents().get(0);
				return firstComponent.mouseClickedAnywhere(info,
						firstComponent instanceof ModListing ? 1 : 0, true, false);
			}

			if(result) {
				return true;
			}

			if(mod == null && code == Input.F && (mods & Input.COMMAND_MODIFIER) != 0
					&& (mods & Input.SHIFT_MODIFIER) == 0 && (mods & Input.ALT_MODIFIER) == 0) {
				search.setFocused(true);
				return true;
			}
			else if(mod != null
					&& (code == Input.BACKSPACE || (code == Input.LEFT && (mods & Input.ALT_MODIFIER) != 0
							&& (mods & Input.COMMAND_MODIFIER) == 0 && (mods & Input.SHIFT_MODIFIER) == 0))
					&& screen.getRoot().getDialog() == null) {
				switchMod(null);
				return true;
			}

			if(code == SolClientConfig.INSTANCE.modsKey.getKeyCode()) {
				mc.closeScreen();
				return true;
			}

			return false;
		}

		@Override
		public boolean characterTyped(ComponentRenderInfo info, char character) {
			if(mod == null && !search.isFocused()) {
				search.setFocused(true);
				search.setText("");
			}

			return super.characterTyped(info, character);
		}

		public String getQuery() {
			return search.getText();
		}

		@Override
		protected Rectangle getDefaultBounds() {
			return Rectangle.ofDimensions(256, 290);
		}
	}

}
