package io.github.solclient.client.v1_8_9.mixins.platform.mc.render;

import io.github.solclient.client.platform.mc.render.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GlStateManager.class)
public class GlStateManagerImpl {

    // blaze3d?

    @Overwrite(remap = false)
    public static void enableBlend() {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) return;
        com.mojang.blaze3d.platform.GlStateManager.enableBlend();
    }

    @Overwrite(remap = false)
    public static void disableBlend() {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) return;
        com.mojang.blaze3d.platform.GlStateManager.disableBlend();
    }

    @Overwrite(remap = false)
    public static void enableDepth() {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) return;
        com.mojang.blaze3d.platform.GlStateManager.enableDepthTest();
    }

    @Overwrite(remap = false)
    public static void disableDepth() {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) return;
        com.mojang.blaze3d.platform.GlStateManager.disableDepthTest();
    }

    @Overwrite(remap = false)
    public static void enableCull() {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) return;
        com.mojang.blaze3d.platform.GlStateManager.enableCull();
    }

    @Overwrite(remap = false)
    public static void disableCull() {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) return;
        com.mojang.blaze3d.platform.GlStateManager.disableCull();
    }

    @Overwrite(remap = false)
    public static void pushMatrix() {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) {
            io.github.solclient.client.platform.PlatformServices.renderBackend().pushMatrix();
            return;
        }
        com.mojang.blaze3d.platform.GlStateManager.pushMatrix();
    }

    @Overwrite(remap = false)
    public static void popMatrix() {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) {
            io.github.solclient.client.platform.PlatformServices.renderBackend().popMatrix();
            return;
        }
        com.mojang.blaze3d.platform.GlStateManager.popMatrix();
    }

    @Overwrite(remap = false)
    public static void disableTexture2d() {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) return;
        com.mojang.blaze3d.platform.GlStateManager.disableTexture();
    }

    @Overwrite(remap = false)
    public static void enableTexture2d() {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) return;
        com.mojang.blaze3d.platform.GlStateManager.enableTexture();
    }

    @Overwrite(remap = false)
    public static void depthMask(boolean mask) {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) return;
        com.mojang.blaze3d.platform.GlStateManager.depthMask(mask);
    }

    @Overwrite(remap = false)
    public static void scale(float x, float y, float z) {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) {
            io.github.solclient.client.platform.PlatformServices.renderBackend().scale(x, y, z);
            return;
        }
        com.mojang.blaze3d.platform.GlStateManager.scalef(x, y, z);
    }

    @Overwrite(remap = false)
    public static void scale(double x, double y, double z) {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) {
            io.github.solclient.client.platform.PlatformServices.renderBackend().scale(x, y, z);
            return;
        }
        com.mojang.blaze3d.platform.GlStateManager.scaled(x, y, z);
    }

    @Overwrite(remap = false)
    public static void translate(float x, float y, float z) {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) {
            io.github.solclient.client.platform.PlatformServices.renderBackend().translate(x, y, z);
            return;
        }
        com.mojang.blaze3d.platform.GlStateManager.translatef(x, y, z);
    }

    @Overwrite(remap = false)
    public static void translate(double x, double y, double z) {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) {
            io.github.solclient.client.platform.PlatformServices.renderBackend().translate(x, y, z);
            return;
        }
        com.mojang.blaze3d.platform.GlStateManager.translated(x, y, z);
    }

    @Overwrite(remap = false)
    public static void colour(float r, float g, float b, float a) {
        io.github.solclient.client.platform.PlatformServices.rememberColour(r, g, b, a);
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) {
            io.github.solclient.client.platform.PlatformServices.renderBackend().colour(r, g, b, a);
            return;
        }
        com.mojang.blaze3d.platform.GlStateManager.color4f(r, g, b, a);
    }

    @Overwrite(remap = false)
    public static void blendFunction(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) return;
        com.mojang.blaze3d.platform.GlStateManager.blendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }

    @Overwrite(remap = false)
    public static void blendFunction(int sfactor, int dfactor) {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) return;
        com.mojang.blaze3d.platform.GlStateManager.blendFunc(sfactor, dfactor);
    }

    @Overwrite(remap = false)
    public static void lineWidth(float width) {
        io.github.solclient.client.platform.PlatformServices.rememberLineWidth(width);
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) {
            io.github.solclient.client.platform.PlatformServices.renderBackend().lineWidth(width);
            return;
        }
        GL11.glLineWidth(width);
    }

    @Overwrite(remap = false)
    public static void resetLineWidth() {
        io.github.solclient.client.platform.PlatformServices.rememberLineWidth(2.0F);
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) {
            io.github.solclient.client.platform.PlatformServices.renderBackend().resetLineWidth();
            return;
        }
        GL11.glLineWidth(2);
    }

    @Overwrite(remap = false)
    public static void rotate(float angle, float x, float y, float z) {
        if (io.github.solclient.client.platform.PlatformServices.isUiFrameActive()) {
            io.github.solclient.client.platform.PlatformServices.renderBackend().rotate(angle, x, y, z);
            return;
        }
        com.mojang.blaze3d.platform.GlStateManager.rotatef(angle, x, y, z);
    }

    @Overwrite(remap = false)
    public static void bindTexture(int id) {
        com.mojang.blaze3d.platform.GlStateManager.bindTexture(id);
    }

}
