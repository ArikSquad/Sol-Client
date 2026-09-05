package io.github.solclient.client.platform.mc.render;

import io.github.solclient.client.platform.Helper;
import io.github.solclient.client.platform.PlatformServices;

public class GlStateManager {

	public static void enableBlend() {
		PlatformServices.get().enableBlend();
	}

	public static void disableBlend() {
		PlatformServices.get().disableBlend();
	}

	public static void enableDepth() {
		PlatformServices.get().enableDepth();
	}

	public static void disableDepth() {
		PlatformServices.get().disableDepth();
	}

	public static void enableCull() {
		PlatformServices.get().enableCull();
	}

	public static void disableCull() {
		PlatformServices.get().disableCull();
	}

	public static void pushMatrix() {
		PlatformServices.get().pushMatrix();
	}

	public static void popMatrix() {
		PlatformServices.get().popMatrix();
	}

	public static void disableTexture2d() {
		PlatformServices.get().disableTexture();
	}

	public static void enableTexture2d() {
		PlatformServices.get().enableTexture();
	}

	public static void depthMask(boolean mask) {
		PlatformServices.get().depthMask(mask);
	}

	public static void scale(float x, float y, float z) {
		PlatformServices.get().scale(x, y, z);
	}

	public static void scale(double x, double y, double z) {
		PlatformServices.get().scale(x, y, z);
	}

	public static void translate(float x, float y, float z) {
		PlatformServices.get().translate(x, y, z);
	}

	public static void translate(double x, double y, double z) {
		PlatformServices.get().translate(x, y, z);
	}

	@Helper
	public static void resetColour() {
		colour(1, 1, 1, 1);
	}

	public static void colour(float r, float g, float b, float a) {
		PlatformServices.rememberColour(r, g, b, a);
		PlatformServices.get().colour(r, g, b, a);
	}

	public static void blendFunction(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
		PlatformServices.get().blendFunction(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
	}

	public static void blendFunction(int sfactor, int dfactor) {
		PlatformServices.get().blendFunction(sfactor, dfactor);
	}

	public static void lineWidth(float width) {
		PlatformServices.rememberLineWidth(width);
		PlatformServices.get().lineWidth(width);
	}

	public static void resetLineWidth() {
		PlatformServices.rememberLineWidth(2.0F);
		PlatformServices.get().resetLineWidth();
	}

	public static void rotate(float angle, float x, float y, float z) {
		PlatformServices.get().rotate(angle, x, y, z);
	}

	public static void bindTexture(int id) {
		PlatformServices.get().bindTexture(id);
	}

}
