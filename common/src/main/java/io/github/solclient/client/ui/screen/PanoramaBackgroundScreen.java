package io.github.solclient.client.ui.screen;

import io.github.solclient.client.platform.mc.MinecraftClient;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.screen.TitleScreen;
import io.github.solclient.client.platform.mc.text.Text;
import io.github.solclient.client.ui.component.*;

public abstract class PanoramaBackgroundScreen extends Screen {

	private final TitleScreen titleScreen = MinecraftClient.getInstance().getTitleScreen();

	public PanoramaBackgroundScreen(Text title, Component root) {
		super(title, root);
		background = false;
	}

	@Override
	public void initScreen() {
		super.initScreen();
		if(!mc.hasLevel()) {
			titleScreen.update(mc, width, height);
		}
	}

	@Override
	public void tickScreen() {
		super.tickScreen();
		titleScreen.tickScreen();
	}

	protected void renderPanorama(int x, int y, float tickDelta) {
		// The version adapter submits this native primitive before the NanoVG
		// frame. Calling the title screen again here would mix native GUI state
		// into the vector frame (and double-render the panorama on 1.8).
		if(!PlatformServices.isUiFrameActive()) {
			titleScreen.renderTitlePanorama(x, y, tickDelta);
		}
	}

	public boolean wantsPanorama() {
		return !mc.hasLevel();
	}

	public final void renderNativePanorama(int x, int y, float tickDelta) {
		if (wantsPanorama()) {
			titleScreen.renderTitlePanorama(x, y, tickDelta);
		}
	}

}
