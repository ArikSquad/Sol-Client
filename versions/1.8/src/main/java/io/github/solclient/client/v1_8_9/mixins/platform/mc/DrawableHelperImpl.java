package io.github.solclient.client.v1_8_9.mixins.platform.mc;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.DrawableHelper;
import io.github.solclient.client.v1_8_9.SharedObjects;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(DrawableHelper.class)
public class DrawableHelperImpl {

    @Overwrite(remap = false)
    public static void fillRect(int left, int top, int right, int bottom, int colour) {
        fillRect((float) left, top, right, bottom, colour);
    }

    @Overwrite(remap = false)
    public static void fillRect(float left, float top, float right, float bottom, int colour) {
        if (PlatformServices.isUiFrameActive()) {
            PlatformServices.get().fillRect(left, top, right, bottom, colour);
            return;
        }

        {
            float tmp;

            if (left < right) {
                tmp = left;
                left = right;
                right = tmp;
            }

            if (top < bottom) {
                tmp = top;
                top = bottom;
                bottom = tmp;
            }
        }

        float r = (colour >> 16 & 255) / 255F,
                g = (colour >> 8 & 255) / 255F,
                b = (colour & 255) / 255F,
                a = (colour >> 24 & 255) / 255F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffers = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color4f(r, g, b, a);
        buffers.begin(7, VertexFormats.POSITION);
        buffers.vertex(left, bottom, 0).next();
        buffers.vertex(right, bottom, 0).next();
        buffers.vertex(right, top, 0).next();
        buffers.vertex(left, top, 0).next();
        tessellator.draw();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    /**
     * The common UI uses this primitive for every themed pill and panel.  The
     * legacy target cannot call the modern render backend, so keep the exact
     * same small raster contract here using the already-overwritten fillRect.
     */
    @Overwrite(remap = false)
    public static void fillRoundedRect(float left, float top, float right, float bottom,
                                       float radius, int colour) {
        if (PlatformServices.isUiFrameActive()) {
            PlatformServices.get().fillRoundedRect(left, top, right, bottom, radius, colour);
            return;
        }

        int l = Math.round(left);
        int t = Math.round(top);
        int r = Math.round(right);
        int b = Math.round(bottom);
        int width = r - l;
        int height = b - t;
        float cornerRadius = Math.max(0.0F, Math.min(radius, Math.min(width, height) / 2.0F));

        if (width <= 0 || height <= 0) {
            return;
        }
        if (cornerRadius <= 0.0F) {
            fillRect(l, t, r, b, colour);
            return;
        }

        for (int row = 0; row < height; row++) {
            float y = row + 0.5F;
            float distanceFromCorner = 0.0F;
            if (y < cornerRadius) {
                distanceFromCorner = cornerRadius - y;
            } else if (y > height - cornerRadius) {
                distanceFromCorner = y - (height - cornerRadius);
            }

            float cornerInset = distanceFromCorner <= 0.0F ? 0.0F
                    : cornerRadius - (float) Math.sqrt(Math.max(0.0F,
                    cornerRadius * cornerRadius - distanceFromCorner * distanceFromCorner));
            int inset = (int) Math.ceil(cornerInset);
            fillRect(l + inset, t + row, r - inset, t + row + 1, colour);
        }
    }

    @Overwrite(remap = false)
    public static void fillGradientRect(int left, int top, int right, int bottom, int topColour, int bottomColour) {
        if (PlatformServices.isUiFrameActive()) {
            PlatformServices.get().fillGradientRect(left, top, right, bottom, topColour, bottomColour);
            return;
        }
        SharedObjects.HANDY_HELPER.fillGradient(left, top, right, bottom, topColour, bottomColour);
    }

    @Overwrite(remap = false)
    public static void strokeRect(int left, int top, int right, int bottom, int colour) {
        if (PlatformServices.isUiFrameActive()) {
            PlatformServices.get().strokeRect(left, top, right, bottom, colour);
            return;
        }
        renderHorizontalLine(left, right - 1, top, colour);
        renderHorizontalLine(left, right - 1, bottom - 1, colour);
        renderVerticalLine(left, top, bottom - 1, colour);
        renderVerticalLine(right - 1, top, bottom - 1, colour);
    }

    @Overwrite(remap = false)
    public static void fillTexturedRect(int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        if (PlatformServices.isUiFrameActive()) {
            PlatformServices.get().fillTexturedRect(x, y, u, v, width, height, textureWidth, textureHeight);
            return;
        }
        net.minecraft.client.gui.DrawableHelper.drawTexture(x, y, u, v, width, height, textureWidth, textureHeight);
    }

    @Overwrite(remap = false)
    public static void renderHorizontalLine(int startX, int endX, int y, int colour) {
        if (PlatformServices.isUiFrameActive()) {
            PlatformServices.get().renderHorizontalLine(startX, endX, y, colour);
            return;
        }
        SharedObjects.HANDY_HELPER.drawHorizontalLine(startX, endX, y, colour);
    }

    @Overwrite(remap = false)
    public static void renderVerticalLine(int x, int startY, int endY, int colour) {
        if (PlatformServices.isUiFrameActive()) {
            PlatformServices.get().renderVerticalLine(x, startY, endY, colour);
            return;
        }
        SharedObjects.HANDY_HELPER.drawVerticalLine(x, startY, endY, colour);
    }

}
