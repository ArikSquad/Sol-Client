package io.github.solclient.client.v1_21_11;

import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.RenderBackend;
import io.github.solclient.client.platform.mc.screen.ProxyScreen;
import io.github.solclient.client.platform.mc.screen.Screen;
import io.github.solclient.client.ui.render.UiScreenRenderer;
import io.github.solclient.client.ui.screen.PanoramaBackgroundScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Bridges one shared screen into the 1.21 screen lifecycle.
 */
public final class CommonScreenAdapter extends net.minecraft.client.gui.screens.Screen {

    private final Screen delegate;
    private final MinecraftClientAdapter client;
    private final FabricPlatform platform;
    private int lastMouseX;
    private int lastMouseY;
    private float lastTickDelta;

    CommonScreenAdapter(Screen delegate, Minecraft minecraft, FabricPlatform platform) {
        super(Component.literal(delegate.getClass().getSimpleName()));
        this.delegate = delegate;
        this.client = platform.clientAdapter();
        this.platform = platform;
    }

    Screen delegate() {
        return delegate;
    }

    static net.minecraft.client.gui.screens.Screen unwrap(Screen screen) {
        if (screen instanceof NativeScreenHandle) {
            return ((NativeScreenHandle) screen).nativeScreen();
        }
        if (screen instanceof CommonScreenAdapterHandle) {
            return ((CommonScreenAdapterHandle) screen).nativeScreen();
        }
        return null;
    }

    @Override
    protected void init() {
        delegate.update(client, width, height);
        delegate.initScreen();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        lastTickDelta = tickDelta;

        GuiGraphicsBackend nativeBackend = new GuiGraphicsBackend(graphics, platform);
        if (usesNativePanorama()) {
            PlatformServices.enterRenderBackend(nativeBackend);
            try {
                nativeBackend.renderPanorama(width, height, tickDelta);
            } finally {
                PlatformServices.leaveRenderBackend();
            }
        }

        // GuiGraphics records its commands first and the modern GUI renderer
        // submits them later. NanoVG must be submitted from that final render
        // pass as well, otherwise this frame is cleared before it reaches the
        // window. The mixin calls renderWithUiRenderer() at that boundary.
        if (PlatformServices.uiRenderer() == io.github.solclient.client.platform.UiRenderer.NONE) {
            PlatformServices.enterRenderBackend(nativeBackend);
            try {
                delegate.renderScreen(mouseX, mouseY, tickDelta);
            } finally {
                PlatformServices.leaveRenderBackend();
            }
        }
    }

    /**
     * Called while the 1.21.11 GUI render pass still owns its target.
     */
    public void renderWithUiRenderer() {
        UiScreenRenderer.render(delegate, platform.window(), RenderBackend.NONE,
                lastMouseX, lastMouseY, lastTickDelta);
    }

    private boolean usesNativePanorama() {
        return delegate instanceof PanoramaBackgroundScreen
                && ((PanoramaBackgroundScreen) delegate).wantsPanorama();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return delegate.keyDown(event.key(), event.scancode(), event.modifiers())
                || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        String value = event.codepointAsString();
        boolean handled = false;
        for (int index = 0; index < value.length(); index++) {
            handled |= delegate.characterTyped(value.charAt(index), -1);
        }
        return handled || super.charTyped(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return delegate.mouseDown((int) event.x(), (int) event.y(), event.button())
                || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return delegate.mouseUp((int) event.x(), (int) event.y(), event.button())
                || super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (delegate instanceof ProxyScreen) {
            ((ProxyScreen) delegate).scrollScreen((int) Math.round(vertical));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public void tick() {
        delegate.tickScreen();
    }

    @Override
    public void removed() {
        if (delegate instanceof ProxyScreen) {
            ((ProxyScreen) delegate).closeScreen();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return delegate instanceof ProxyScreen && ((ProxyScreen) delegate).shouldPauseGame();
    }

    /**
     * Marker interface used by reflective adapters if a screen is already native.
     */
    private interface CommonScreenAdapterHandle {
        net.minecraft.client.gui.screens.Screen nativeScreen();
    }
}
