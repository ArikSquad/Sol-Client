package io.github.solclient.client.ui.screen;

import io.github.solclient.client.Constants;
import io.github.solclient.client.mod.impl.SolClientConfig;
import io.github.solclient.client.platform.mc.DrawableHelper;
import io.github.solclient.client.platform.mc.Environment;
import io.github.solclient.client.platform.mc.MinecraftClient;
import io.github.solclient.client.platform.mc.lang.I18n;
import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.platform.mc.screen.*;
import io.github.solclient.client.platform.mc.text.Text;
import io.github.solclient.client.platform.mc.util.Input;
import io.github.solclient.client.ui.Theme;
import io.github.solclient.client.ui.component.Component;
import io.github.solclient.client.ui.component.controller.Controller;
import io.github.solclient.client.ui.component.impl.ButtonComponent;
import io.github.solclient.client.ui.screen.mods.ModsScreen;
import io.github.solclient.client.util.Utils;
import io.github.solclient.client.util.data.Colour;
import io.github.solclient.client.util.data.Rectangle;

/**
 * The shared Sol Client title screen.
 *
 * <p>The layout and palette intentionally live in {@code common}. A version
 * module only supplies the native screen wrapper, font, texture and click
 * implementations, so the menu does not drift between Minecraft versions.</p>
 */
public final class SolClientMainMenu extends PanoramaBackgroundScreen {

	public SolClientMainMenu() {
		super(Text.translation(Environment.MAJOR_RELEASE > 1 || Environment.MINOR_RELEASE >= 12
				? "narrator.screen.title" : "deathScreen.titleScreen"), new MainMenuComponent());
	}

	@Override
	public void renderScreen(int x, int y, float tickDelta) {
		renderPanorama(x, y, tickDelta);

		MinecraftClient client = MinecraftClient.getInstance();
		client.getTextureManager().bind(Identifier.minecraft("textures/gui/sol_client_logo_with_text_"
				+ Utils.getTextureScale() + ".png"));
		DrawableHelper.fillTexturedRect(width / 2 - 64, getStartY(height), 0, 0, 128, 32, 128, 32);

		super.renderScreen(x, y, tickDelta);

		int colour = Theme.getCurrent().fg.getValue();
		io.github.solclient.client.platform.mc.text.Font font = getUiFont();
		String copyrightString = "Copyright " + Environment.MOJANG + ". Do not distribute!";
		font.render(copyrightString, width - font.getTextWidth(copyrightString) - 10, height - 15, colour);
		String versionString = "Minecraft " + Environment.VERSION_ID;
		font.render(versionString, width - font.getTextWidth(versionString) - 10, height - 25, colour);
		font.render("Copyright TheKodeToad and contributors.", 10, height - 15, colour);
		font.render(Constants.NAME, 10, height - 25, colour);
	}

	@Override
	public boolean keyDown(int code, int scancode, int mods) {
		if(code == Input.ESCAPE) {
			return true;
		}
		return super.keyDown(code, scancode, mods);
	}

	private static int getStartY(int height) {
		return height / 2 - 146 / 2;
	}

	private static final class MainMenuComponent extends Component {

		private final Controller<Colour> white = (component, fallback) -> Colour.WHITE;

		MainMenuComponent() {
			add(new ButtonComponent("menu.singleplayer", Theme.accent(), Theme.accentFg())
					.withIcon("sol_client_player").width(200)
					.onClick((info, button) -> {
						if(button != 0) return false;
						Utils.playClickSound(true);
						mc.setScreen(SingleplayerScreen.create(screen));
						return true;
					}), (component, bounds) -> new Rectangle(screen.getWidth() / 2 - 100,
					getStartY(screen.getHeight()) + 44, bounds.getWidth(), bounds.getHeight()));

			add(new ButtonComponent("menu.multiplayer", Theme.accent(), Theme.accentFg())
					.withIcon("sol_client_players").width(200)
					.onClick((info, button) -> {
						if(button != 0) return false;
						Utils.playClickSound(true);
						mc.setScreen(MultiplayerScreen.create(screen));
						return true;
					}), (component, bounds) -> new Rectangle(screen.getWidth() / 2 - 100,
					getStartY(screen.getHeight()) + 68, bounds.getWidth(), bounds.getHeight()));

			add(new ButtonComponent("sol_client.mod.screen.title", Theme.button(), Theme.fg())
					.withIcon("sol_client_mods").width(200)
					.onClick((info, button) -> {
						if(button != 0) return false;
						Utils.playClickSound(true);
						mc.setScreen(new ModsScreen());
						return true;
					}), (component, bounds) -> new Rectangle(screen.getWidth() / 2 - 100,
					getStartY(screen.getHeight()) + 92, bounds.getWidth(), bounds.getHeight()));

			add(new ButtonComponent("menu.options", Theme.button(), Theme.fg())
					.withIcon("sol_client_settings").width(98)
					.onClick((info, button) -> {
						if(button != 0) return false;
						Utils.playClickSound(true);
						mc.setScreen(OptionsScreen.create(screen, mc.getOptions()));
						return true;
					}), (component, bounds) -> new Rectangle(screen.getWidth() / 2 - 100,
					getStartY(screen.getHeight()) + 126, bounds.getWidth(), bounds.getHeight()));

			add(new ButtonComponent("menu.quit", Theme.danger(), white)
					.withIcon("sol_client_exit").width(98)
					.onClick((info, button) -> {
						if(button != 0) return false;
						Utils.playClickSound(true);
						mc.quit();
						return true;
					}), (component, bounds) -> new Rectangle(screen.getWidth() / 2 + 2,
					getStartY(screen.getHeight()) + 126, bounds.getWidth(), bounds.getHeight()));

			add(new ButtonComponent("", Theme.button(), Theme.fg())
					.withIcon("sol_client_language").width(20)
					.onClick((info, button) -> {
						if(button != 0) return false;
						Utils.playClickSound(true);
						mc.setScreen(LanguageScreen.create(screen, mc.getOptions(), mc.getLanguageManager()));
						return true;
					}), (component, bounds) -> new Rectangle(screen.getWidth() / 2 - 124,
					getStartY(screen.getHeight()) + 126, bounds.getWidth(), bounds.getHeight()));
		}
	}

}
