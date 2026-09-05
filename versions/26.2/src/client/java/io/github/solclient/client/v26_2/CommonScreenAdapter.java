package io.github.solclient.client.v26_2;

import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.RenderBackend;
import io.github.solclient.client.platform.UiRenderer;
import io.github.solclient.client.platform.mc.screen.ProxyScreen;
import io.github.solclient.client.platform.mc.screen.Screen;
import io.github.solclient.client.ui.render.UiScreenRenderer;
import io.github.solclient.client.ui.screen.PanoramaBackgroundScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

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
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		lastTickDelta = tickDelta;

		// 26.2 extracts commands before GameRenderer clears and draws the main
		// target. The native panorama is the one background primitive we still
		// want in that command stream; the common UI is submitted after the
		// native GuiRenderer has finished (see GameRendererMixin).
		if (usesNativePanorama()) {
			GuiGraphicsBackend nativeBackend = new GuiGraphicsBackend(graphics, platform);
			PlatformServices.enterRenderBackend(nativeBackend);
			try {
				nativeBackend.renderPanorama(width, height, tickDelta);
			}
			finally {
				PlatformServices.leaveRenderBackend();
			}
		}

		// Keep a complete native fallback for a machine without the NanoVG
		// binding/native library. In the normal path no common UI commands are
		// extracted here because that frame would be cleared before display.
		// Do not probe NanoVG while 26.2 is extracting its GUI commands.  The
		// actual UI frame has to initialise and upload its resources while the
		// final RenderPass owns the target; probing here creates the context too
		// early and can leave texture uploads out of that pass.  NONE is the
		// explicit provider-level fallback for an installation without a UI
		// renderer.
		if (PlatformServices.uiRenderer() == UiRenderer.NONE) {
			GuiGraphicsBackend nativeBackend = new GuiGraphicsBackend(graphics, platform);
			PlatformServices.enterRenderBackend(nativeBackend);
			try {
				delegate.renderScreen(mouseX, mouseY, tickDelta);
			}
			finally {
				PlatformServices.leaveRenderBackend();
			}
		}
	}

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

	private interface CommonScreenAdapterHandle {
		net.minecraft.client.gui.screens.Screen nativeScreen();
	}
}
