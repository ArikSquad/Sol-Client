package io.github.solclient.client.platform;

import io.github.solclient.client.platform.mc.Window;

/**
 * A screen renderer which owns an entire 2D UI frame.
 *
 * <p>This is intentionally separate from the world/HUD {@link RenderBackend}:
 * a version can replace NanoVG with the future Vulkan UI renderer without
 * changing the common component tree or the screen adapters.</p>
 */
public interface UiRenderer extends RenderBackend {

	UiRenderer NONE = new UiRenderer() {};

	default boolean isAvailable() {
		return false;
	}

	default boolean beginFrame(Window window) {
		return false;
	}

	default void endFrame() {}

	default void close() {}

	default void setNativeFallback(RenderBackend backend) {}

}
