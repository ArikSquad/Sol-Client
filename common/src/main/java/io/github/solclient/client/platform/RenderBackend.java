package io.github.solclient.client.platform;

import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.platform.mc.text.OrderedText;
import io.github.solclient.client.platform.mc.text.Text;

/**
 * The tiny rendering seam used by the shared UI and HUD code.
 *
 * <p>Version modules bind one implementation while a native screen is being
 * rendered. The common code can consequently keep using its old immediate
 * mode-style drawing vocabulary without importing a Minecraft class.</p>
 */
public interface RenderBackend {

	RenderBackend NONE = new RenderBackend() {};

	default void fillRect(float left, float top, float right, float bottom, int colour) {}

	/**
	 * Draws a filled rounded rectangle in GUI coordinates. The common raster
	 * implementation is deliberately scanline based: it keeps the shape
	 * identical on the old immediate-mode renderer and the modern collected
	 * GUI renderer without making either version own a second UI layout.
	 */
	default void fillRoundedRect(float left, float top, float right, float bottom,
			float radius, int colour) {
		int l = Math.round(left);
		int t = Math.round(top);
		int r = Math.round(right);
		int b = Math.round(bottom);
		int width = r - l;
		int height = b - t;
		float cornerRadius = Math.max(0.0F, Math.min(radius, Math.min(width, height) / 2.0F));
		if(width <= 0 || height <= 0) {
			return;
		}
		if(cornerRadius <= 0.0F) {
			fillRect(l, t, r, b, colour);
			return;
		}

		for(int row = 0; row < height; row++) {
			float y = row + 0.5F;
			float distanceFromCorner = 0.0F;
			if(y < cornerRadius) {
				distanceFromCorner = cornerRadius - y;
			}
			else if(y > height - cornerRadius) {
				distanceFromCorner = y - (height - cornerRadius);
			}

			float cornerInset = distanceFromCorner <= 0.0F ? 0.0F
					: cornerRadius - (float) Math.sqrt(Math.max(0.0F,
							cornerRadius * cornerRadius - distanceFromCorner * distanceFromCorner));
			int inset = (int) Math.ceil(cornerInset);
			fillRect(l + inset, t + row, r - inset, t + row + 1, colour);
		}
	}

	default void fillGradientRect(int left, int top, int right, int bottom, int topColour, int bottomColour) {
		fillRect(left, top, right, bottom, topColour);
	}

	default void strokeRect(int left, int top, int right, int bottom, int colour) {
		renderHorizontalLine(left, right - 1, top, colour);
		renderHorizontalLine(left, right - 1, bottom - 1, colour);
		renderVerticalLine(left, top, bottom - 1, colour);
		renderVerticalLine(right - 1, top, bottom - 1, colour);
	}

	default void fillTexturedRect(int x, int y, float u, float v, int width, int height,
			int textureWidth, int textureHeight) {}

	default void renderHorizontalLine(int startX, int endX, int y, int colour) {
		fillRect(startX, y, endX + 1, y + 1, colour);
	}

	default void renderVerticalLine(int x, int startY, int endY, int colour) {
		fillRect(x, startY, x + 1, endY + 1, colour);
	}

	default int render(String text, int x, int y, int rgb, boolean shadow) {
		return x + getTextWidth(text);
	}

	default int render(Text text, int x, int y, int rgb, boolean shadow) {
		return render(text == null ? "" : text.getPlain(), x, y, rgb, shadow);
	}

	default int render(OrderedText text, int x, int y, int rgb, boolean shadow) {
		return render((Text) text, x, y, rgb, shadow);
	}

	default int getTextWidth(String text) {
		return text == null ? 0 : text.length() * 6;
	}

	default int getTextWidth(Text text) {
		return getTextWidth(text == null ? "" : text.getPlain());
	}

	default int getTextWidth(OrderedText text) {
		return getTextWidth((Text) text);
	}

	default int getCharacterWidth(char character) {
		return getTextWidth(String.valueOf(character));
	}

	default int getHeight() {
		return 9;
	}

	default void pushMatrix() {}

	default void popMatrix() {}

	default void translate(double x, double y, double z) {}

	default void scale(double x, double y, double z) {}

	default void rotate(float angle, float x, float y, float z) {}

	default void bindTexture(int id) {}

	default void bindTexture(Identifier identifier) {}

	default void renderItem(Object nativeItem, int x, int y) {}

	default void renderItemDecorations(Object nativeItem, int x, int y) {}

	default void enableBlend() {}

	default void disableBlend() {}

	default void enableDepth() {}

	default void disableDepth() {}

	default void enableCull() {}

	default void disableCull() {}

	default void enableTexture() {}

	default void disableTexture() {}

	default void depthMask(boolean mask) {}

	default void colour(float r, float g, float b, float a) {}

	default void blendFunction(int sourceRgb, int destinationRgb, int sourceAlpha, int destinationAlpha) {}

	default void blendFunction(int source, int destination) {}

	default void lineWidth(float width) {}

	default void resetLineWidth() {}

	default void defaultBackground() {
		fillRect(0, 0, 1, 1, 0xFF141414);
	}

	default void renderPanorama(int width, int height, float tickDelta) {
		fillGradientRect(0, 0, width, height, 0xFF202020, 0xFF080808);
	}

	default void scissor(double x, double y, double width, double height) {}

	default void resetScissor() {}

	default Object nativeIdentifier(Identifier identifier) {
		return identifier;
	}
}
