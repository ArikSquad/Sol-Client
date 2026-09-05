package io.github.solclient.client.platform.mc;

import lombok.experimental.UtilityClass;

import io.github.solclient.client.platform.PlatformServices;

@UtilityClass
public class DrawableHelper {

	public void fillRect(int left, int top, int right, int bottom, int colour) {
		PlatformServices.get().fillRect(left, top, right, bottom, colour);
	}

	public void fillRect(float left, float top, float right, float bottom, int colour) {
		PlatformServices.get().fillRect(left, top, right, bottom, colour);
	}

	public void fillRoundedRect(float left, float top, float right, float bottom, float radius, int colour) {
		PlatformServices.get().fillRoundedRect(left, top, right, bottom, radius, colour);
	}

	public void fillGradientRect(int left, int top, int right, int bottom, int topColour, int bottomColour) {
		PlatformServices.get().fillGradientRect(left, top, right, bottom, topColour, bottomColour);
	}

	public void strokeRect(int left, int top, int right, int bottom, int colour) {
		PlatformServices.get().strokeRect(left, top, right, bottom, colour);
	}

	public void fillTexturedRect(int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		PlatformServices.get().fillTexturedRect(x, y, u, v, width, height, textureWidth, textureHeight);
	}

	public void renderHorizontalLine(int startX, int endX, int y, int colour) {
		PlatformServices.get().renderHorizontalLine(startX, endX, y, colour);
	}

	public void renderVerticalLine(int x, int startY, int endY, int colour) {
		PlatformServices.get().renderVerticalLine(x, startY, endY, colour);
	}

}
