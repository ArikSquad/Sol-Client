package io.github.solclient.client.v26_2;

import io.github.solclient.client.platform.RenderBackend;
import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.platform.mc.text.Text;

public final class GuiGraphicsBackend implements RenderBackend {

	private final net.minecraft.client.gui.GuiGraphicsExtractor graphics;
	private final FabricPlatform platform;
	private final NativePanoramaScreen panorama = new NativePanoramaScreen();

	public GuiGraphicsBackend(net.minecraft.client.gui.GuiGraphicsExtractor graphics, FabricPlatform platform) {
		this.graphics = graphics;
		this.platform = platform;
	}

	@Override public void fillRect(float left, float top, float right, float bottom, int colour) {
		graphics.fill(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom), colour);
	}

	@Override public void fillGradientRect(int left, int top, int right, int bottom, int topColour, int bottomColour) {
		graphics.fillGradient(left, top, right, bottom, topColour, bottomColour);
	}

	@Override public void fillTexturedRect(int x, int y, float u, float v, int width, int height,
			int textureWidth, int textureHeight) {
		Identifier id = boundTexture;
		if (id != null) {
			graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, nativeId(id),
					x, y, u, v, width, height, textureWidth, textureHeight);
		}
	}

	@Override public void renderHorizontalLine(int startX, int endX, int y, int colour) {
		graphics.horizontalLine(startX, endX, y, colour);
	}

	@Override public void renderVerticalLine(int x, int startY, int endY, int colour) {
		graphics.verticalLine(x, startY, endY, colour);
	}

	@Override public int render(String text, int x, int y, int rgb, boolean shadow) {
		NativeFont font = platform.clientAdapter().nativeFont();
		graphics.text(font.nativeFont(), text, x, y, rgb, shadow);
		return x + font.getTextWidth(text);
	}

	@Override public int render(Text text, int x, int y, int rgb, boolean shadow) {
		return render(text == null ? "" : text.getPlain(), x, y, rgb, shadow);
	}

	@Override public int getTextWidth(String text) { return platform.clientAdapter().nativeFont().getTextWidth(text); }
	@Override public int getTextWidth(Text text) { return getTextWidth(text == null ? "" : text.getPlain()); }
	@Override public int getCharacterWidth(char character) { return getTextWidth(String.valueOf(character)); }
	@Override public int getHeight() { return platform.clientAdapter().nativeFont().getHeight(); }

	@Override public void pushMatrix() { graphics.pose().pushMatrix(); }
	@Override public void popMatrix() { graphics.pose().popMatrix(); }
	@Override public void translate(double x, double y, double z) { graphics.pose().translate((float) x, (float) y); }
	@Override public void scale(double x, double y, double z) { graphics.pose().scale((float) x, (float) y); }
	@Override public void rotate(float angle, float x, float y, float z) {
		if (z != 0) {
			graphics.pose().rotate((float) Math.toRadians(angle));
		}
	}

	@Override public void bindTexture(Identifier identifier) { boundTexture = identifier; }
	@Override public void renderItem(Object item, int x, int y) {
		if (item instanceof net.minecraft.world.item.ItemStack) {
			graphics.item((net.minecraft.world.item.ItemStack) item, x, y);
		}
	}
	@Override public void renderItemDecorations(Object item, int x, int y) {
		if (item instanceof net.minecraft.world.item.ItemStack) {
			graphics.itemDecorations(platform.clientAdapter().nativeFont().nativeFont(),
					(net.minecraft.world.item.ItemStack) item, x, y);
		}
	}
	@Override public void defaultBackground() { graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0xFF141414); }
	@Override public void renderPanorama(int width, int height, float tickDelta) {
		// 26.2's TitleScreen intentionally leaves extractBackground empty; its
		// full extractRenderState owns the panorama plus native widgets. Expose
		// only the protected panorama primitive through a tiny version adapter.
		panorama.extractPanoramaForSol(graphics, tickDelta);
	}
	@Override public void scissor(double x, double y, double width, double height) {
		graphics.enableScissor((int) x, (int) y, (int) (x + width), (int) (y + height));
	}
	@Override public Object nativeIdentifier(Identifier identifier) { return nativeId(identifier); }

	private net.minecraft.resources.Identifier nativeId(Identifier identifier) {
		return (net.minecraft.resources.Identifier) platform.nativeIdentifier(identifier);
	}

	private Identifier boundTexture;

	private static final class NativePanoramaScreen extends net.minecraft.client.gui.screens.TitleScreen {
		void extractPanoramaForSol(net.minecraft.client.gui.GuiGraphicsExtractor graphics, float tickDelta) {
			super.extractPanorama(graphics, tickDelta);
		}
	}
}
