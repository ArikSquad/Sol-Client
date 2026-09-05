package io.github.solclient.client.ui.render;

import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.RenderBackend;
import io.github.solclient.client.platform.UiRenderer;
import io.github.solclient.client.platform.mc.Window;
import io.github.solclient.client.platform.mc.screen.Screen;

public final class UiScreenRenderer {
	private UiScreenRenderer() {}

	/**
	 * @return {@code true} when the selected UI renderer owned the frame.
	 *         Callers should use their native backend when this returns false.
	 */
	public static boolean render(Screen screen, Window window, RenderBackend nativeFallback,
			int mouseX, int mouseY, float tickDelta) {
		UiRenderer renderer = PlatformServices.uiRenderer();
		if (renderer == null) {
			return false;
		}
		renderer.setNativeFallback(nativeFallback);
		boolean began = renderer.beginFrame(window);
		if (!began) {
			return false;
		}

		PlatformServices.enterUiFrame();
		PlatformServices.enterRenderBackend(renderer);
		try {
			screen.renderScreen(mouseX, mouseY, tickDelta);
		}
		finally {
			PlatformServices.leaveRenderBackend();
			PlatformServices.leaveUiFrame();
			renderer.endFrame();
		}
		return true;
	}

}
