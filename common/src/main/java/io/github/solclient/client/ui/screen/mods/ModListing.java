package io.github.solclient.client.ui.screen.mods;

import java.net.URI;

import io.github.solclient.client.Client;
import io.github.solclient.client.mod.Mod;
import io.github.solclient.client.mod.ModUiStateManager;
import io.github.solclient.client.platform.mc.DrawableHelper;
import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.ui.Theme;
import io.github.solclient.client.ui.component.*;
import io.github.solclient.client.ui.component.controller.*;
import io.github.solclient.client.ui.component.impl.*;
import io.github.solclient.client.ui.screen.mods.ModsScreen.ModsScreenComponent;
import io.github.solclient.client.util.Utils;
import io.github.solclient.client.util.data.*;

/**
 * One renderer-neutral entry in the common mod browser.
 *
 * <p>The old multi-version sketch used a textured 300px row whose colours
 * came directly from the legacy config. The redesigned entry is a compact
 * 230px card: the card uses the shared theme and the left stripe communicates
 * enabled, blocked and disabled state. Both 1.8 and modern targets execute
 * this exact component tree.</p>
 */
public final class ModListing extends ColouredComponent {

	private final Mod mod;
	private final ModsScreenComponent screen;
	private final Component settingsButton;
	private final Component pinButton;
	private final boolean pinnedCategory;
	private String iconName;

	public ModListing(Mod mod, ModsScreenComponent screen) {
		this(mod, screen, false);
	}

	public ModListing(Mod mod, ModsScreenComponent screen, boolean pinnedCategory) {
		super(new AnimatedColourController((component, defaultColour) -> {
			ModListing entry = (ModListing) component;
			return entry.isFullyHovered() ? Theme.getCurrent().buttonHover : Theme.getCurrent().button;
		}));

		this.mod = mod;
		this.screen = screen;
		this.pinnedCategory = pinnedCategory;

		add(new ScaledIconComponent((component, defaultName) -> getIconName(), 16, 16, Theme.fg()),
				new AlignedBoundsController(Alignment.START, Alignment.CENTRE,
						(component, defaultBounds) -> new Rectangle(8, defaultBounds.getY(),
								defaultBounds.getWidth(), defaultBounds.getHeight())));

		final LabelComponent name = new LabelComponent((component, defaultText) -> mod.getName()
				+ (mod.isBlocked() ? " (" + io.github.solclient.client.platform.mc.lang.I18n.translate("sol_client.mod.blocked") + ")" : ""),
				Theme.fg());
		add(name,
				new AlignedBoundsController(Alignment.START, Alignment.START,
						(component, defaultBounds) -> new Rectangle(43, 4,
								defaultBounds.getWidth(), defaultBounds.getHeight())));

		add(new LabelComponent((component, defaultText) -> mod.getDescription(),
				(component, defaultColour) -> Theme.getCurrent().muted).scaled(0.8F),
				new AlignedBoundsController(Alignment.START, Alignment.START,
						(component, defaultBounds) -> new Rectangle(33, 17,
								defaultBounds.getWidth(), defaultBounds.getHeight())));

		add(new LabelComponent((component, defaultText) -> {
			String detail = mod.getDetail();
			return detail == null ? "" : io.github.solclient.client.platform.mc.lang.I18n.translate(detail);
		}, (component, defaultColour) -> Theme.getCurrent().mutedStrong).scaled(0.8F),
				new AlignedBoundsController(Alignment.START, Alignment.START,
						(component, defaultBounds) -> new Rectangle(name.getBounds().getEndX() - 1,
								name.getBounds().getY() + 2, defaultBounds.getWidth(), defaultBounds.getHeight())));

		add(pinButton = new PinComponent(mod),
				new AlignedBoundsController(Alignment.START, Alignment.START,
						(component, defaultBounds) -> new Rectangle(33, 7,
								defaultBounds.getWidth(), defaultBounds.getHeight())));

		add(settingsButton = new SettingsButton(),
				new AlignedBoundsController(Alignment.END, Alignment.START));
	}

	public boolean isFullyHovered() {
		return isHovered() && (mod.isLocked() || mod.isBlocked() || !settingsButton.isHovered());
	}

	Mod getMod() {
		return mod;
	}

	void activatePrimaryAction() {
		if(mod.isBlocked()) {
			if(Client.INSTANCE.detectedServer == null) {
				return;
			}

			URI blockedModPage = Client.INSTANCE.detectedServer.getBlockedModPage();
			if(blockedModPage != null) {
				Utils.openUrl(blockedModPage.toString());
			}
		}
		else if(mod.isLocked()) {
			screen.switchMod(mod);
		}
		else {
			mod.toggle();
		}
	}

	private String getIconName() {
		if(iconName != null) {
			return iconName;
		}

		String candidate = "sol_client_" + mod.getId();
		try {
			if(mc.getResourceManager().getResources(Identifier.minecraft("textures/gui/"
					+ candidate + "_" + Utils.getTextureScale() + ".png")).isEmpty()) {
				candidate = "sol_client_confusion";
			}
		}
		catch(Throwable ignored) {
			candidate = "sol_client_confusion";
		}

		return iconName = candidate;
	}

	@Override
	public void render(ComponentRenderInfo info) {
		DrawableHelper.fillRoundedRect(0, 0, getBounds().getWidth(), getBounds().getHeight(), 4,
				getColourValue());

		if(!mod.isLocked()) {
			Colour stripe = mod.isEnabled() ? Theme.getCurrent().accent
					: mod.isBlocked() ? Theme.getCurrent().danger : Theme.getCurrent().buttonSecondary;
			if(isFullyHovered()) {
				stripe = mod.isEnabled() ? Theme.getCurrent().accentHover
						: mod.isBlocked() ? Theme.getCurrent().dangerHover : Theme.getCurrent().buttonSecondaryHover;
			}
			DrawableHelper.fillRect(0, 0, 3, getBounds().getHeight(), stripe.getValue());
		}

		super.render(info);
	}

	@Override
	protected Rectangle getDefaultBounds() {
		return Rectangle.ofDimensions(230, 30);
	}

	@Override
	public boolean mouseClicked(ComponentRenderInfo info, int button) {
		if(button != 0 && button != 1) {
			return false;
		}

		if(!mod.isBlocked() && (settingsButton.isHovered() || button == 1)) {
			Utils.playClickSound(true);
			screen.switchMod(mod);
			return true;
		}

		if(button == 0) {
			if(pinButton.isHovered()) {
				Utils.playClickSound(true);
				mod.setPinned(!mod.isPinned());
				screen.refreshModList();
				return true;
			}

			if(pinnedCategory && ModUiStateManager.INSTANCE.getPins().size() > 1) {
				screen.beginPinDrag(this, info);
				return true;
			}

			Utils.playClickSound(true);
			activatePrimaryAction();
			return true;
		}

		return false;
	}

	private final class SettingsButton extends ColouredComponent {

		SettingsButton() {
			super(Theme.buttonSecondary());
			add(new ScaledIconComponent((component, defaultIcon) ->
					mod.isBlocked() ? "sol_client_lock" : "sol_client_settings", 16, 16, Theme.fgButton()),
					new AlignedBoundsController(Alignment.CENTRE, Alignment.CENTRE));
		}

		@Override
		public void render(ComponentRenderInfo info) {
			if(!mod.isLocked() && !mod.isBlocked()) {
				DrawableHelper.fillRoundedRect(0, 0, getBounds().getWidth(), getBounds().getHeight(), 3,
						getColourValue());
			}
			super.render(info);
		}

		@Override
		protected Rectangle getDefaultBounds() {
			return Rectangle.ofDimensions(30, 30);
		}
	}

}
