package io.github.solclient.client.platform.mc.screen;

import io.github.solclient.client.platform.mc.MinecraftClient;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.text.*;

public class ProxyScreen implements Screen {

	protected int width, height;
	protected MinecraftClient mc;
	protected Font font;
	private final Text title;

	public ProxyScreen(Text title) {
		this.title = title;
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

	/**
	 * Called when the mouse is scrolled.
	 * You should not call super.
	 * @param by Scroll amount.
	 */
	protected void scroll(int by) {
	}

	public final void scrollScreen(int by) {
		scroll(by);
	}

	@Override
	public final void update(MinecraftClient mc, int width, int height) {
		this.mc = mc;
		this.width = width;
		this.height = height;
		this.font = mc == null ? null : mc.getFont();
	}

	@Override
	public void initScreen() {
	}

	protected void onClose() {
	}

	public final void closeScreen() {
		onClose();
	}

	@SuppressWarnings("static-method")
	protected final void renderDefaultBackground() {
		PlatformServices.get().defaultBackground();
	}

	@Override
	public void tickScreen() {
	}

	protected boolean pausesGame() {
		return true;
	}

	public final boolean shouldPauseGame() {
		return pausesGame();
	}

	@Override
	public final int getWidth() {
		return width;
	}

	@Override
	public final int getHeight() {
		return height;
	}

	public final Text getTitle() {
		return title;
	}

}
