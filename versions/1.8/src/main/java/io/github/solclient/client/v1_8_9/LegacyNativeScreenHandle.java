package io.github.solclient.client.v1_8_9;

import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.screen.LanguageScreen;
import io.github.solclient.client.platform.mc.screen.MultiplayerScreen;
import io.github.solclient.client.platform.mc.screen.OptionsScreen;
import io.github.solclient.client.platform.mc.screen.Screen;
import io.github.solclient.client.platform.mc.screen.SingleplayerScreen;

public final class LegacyNativeScreenHandle
        implements Screen, OptionsScreen, SingleplayerScreen, MultiplayerScreen, LanguageScreen {

    private final net.minecraft.client.gui.screen.Screen nativeScreen;

    public LegacyNativeScreenHandle(net.minecraft.client.gui.screen.Screen nativeScreen) {
        this.nativeScreen = nativeScreen;
    }

    public net.minecraft.client.gui.screen.Screen nativeScreen() {
        return nativeScreen;
    }

    public static net.minecraft.client.gui.screen.Screen unwrap(Screen screen) {
        if (screen instanceof LegacyNativeScreenHandle) {
            return ((LegacyNativeScreenHandle) screen).nativeScreen;
        }
        return screen == null ? null : (net.minecraft.client.gui.screen.Screen) (Object) screen;
    }

    @Override
    public void update(io.github.solclient.client.platform.mc.MinecraftClient mc, int width, int height) {
    }

    @Override
    public void initScreen() {
    }

    @Override
    public void renderScreen(int mouseX, int mouseY, float tickDelta) {
    }

    @Override
    public boolean characterTyped(char character, int key) {
        return false;
    }

    @Override
    public boolean keyDown(int code, int scancode, int mods) {
        return false;
    }

    @Override
    public boolean mouseDown(int x, int y, int button) {
        return false;
    }

    @Override
    public boolean mouseUp(int x, int y, int button) {
        return false;
    }

    @Override
    public int getWidth() {
        return nativeScreen == null ? PlatformServices.get().window().scaledWidth() : nativeScreen.width;
    }

    @Override
    public int getHeight() {
        return nativeScreen == null ? PlatformServices.get().window().scaledHeight() : nativeScreen.height;
    }

    @Override
    public void tickScreen() {
    }
}
