package io.github.solclient.client.ui.render;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.RenderBackend;
import io.github.solclient.client.platform.mc.text.Font;
import io.github.solclient.client.platform.mc.text.OrderedText;
import io.github.solclient.client.platform.mc.text.Text;

/**
 * Font view used by common components. During a NanoVG/Vulkan UI frame its
 * metrics and glyphs come from the active UI backend; outside that frame it
 * delegates to Minecraft's native font for HUD and native screens.
 */
public final class UiFont implements Font {

	private final Font fallback;

	public UiFont(Font fallback) {
		this.fallback = fallback;
	}

	private RenderBackend active() {
		return PlatformServices.isUiFrameActive() ? PlatformServices.renderBackend() : null;
	}

	@Override public int render(@NotNull String text, int x, int y, int rgb) { return render(text, x, y, rgb, false); }
	@Override public int render(@NotNull String text, int x, int y, int rgb, boolean shadow) {
		RenderBackend backend = active();
		return backend == null ? fallback.render(text, x, y, rgb, shadow) : backend.render(text, x, y, rgb, shadow);
	}
	@Override public int renderWithShadow(@NotNull String text, int x, int y, int rgb) { return render(text, x, y, rgb, true); }
	@Override public int render(@NotNull Text text, int x, int y, int rgb) { return render(text, x, y, rgb, false); }
	@Override public int render(@NotNull Text text, int x, int y, int rgb, boolean shadow) {
		RenderBackend backend = active();
		return backend == null ? fallback.render(text, x, y, rgb, shadow) : backend.render(text, x, y, rgb, shadow);
	}
	@Override public int renderWithShadow(@NotNull Text text, int x, int y, int rgb) { return render(text, x, y, rgb, true); }
	@Override public int render(@NotNull OrderedText text, int x, int y, int rgb) { return render(text, x, y, rgb, false); }
	@Override public int render(@NotNull OrderedText text, int x, int y, int rgb, boolean shadow) {
		RenderBackend backend = active();
		return backend == null ? fallback.render(text, x, y, rgb, shadow) : backend.render(text, x, y, rgb, shadow);
	}
	@Override public int renderWithShadow(@NotNull OrderedText text, int x, int y, int rgb) { return render(text, x, y, rgb, true); }

	@Override public int getCharacterWidth(char character) {
		RenderBackend backend = active();
		return backend == null ? fallback.getCharacterWidth(character) : backend.getCharacterWidth(character);
	}
	@Override public int getTextWidth(@NotNull String text) {
		RenderBackend backend = active();
		return backend == null ? fallback.getTextWidth(text) : backend.getTextWidth(text);
	}
	@Override public int getTextWidth(@NotNull Text text) {
		RenderBackend backend = active();
		return backend == null ? fallback.getTextWidth(text) : backend.getTextWidth(text);
	}
	@Override public int getTextWidth(@NotNull OrderedText text) {
		RenderBackend backend = active();
		return backend == null ? fallback.getTextWidth(text) : backend.getTextWidth(text);
	}
	@Override public int getHeight() {
		RenderBackend backend = active();
		return backend == null ? fallback.getHeight() : backend.getHeight();
	}

}
