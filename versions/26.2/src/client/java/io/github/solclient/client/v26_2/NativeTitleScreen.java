package io.github.solclient.client.v26_2;

import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.screen.TitleScreen;

final class NativeTitleScreen extends NativeScreenHandle implements TitleScreen {

    NativeTitleScreen(net.minecraft.client.gui.screens.Screen nativeScreen) {
        super(nativeScreen);
    }

    @Override
    public void renderTitlePanorama(int x, int y, float tickDelta) {
        PlatformServices.renderBackend().renderPanorama(getWidth(), getHeight(), tickDelta);
    }
}
