package io.github.solclient.client.v1_8_9.mixins.platform.mc;

import io.github.solclient.client.platform.mc.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.client.util.Window.class)
public class WindowImpl implements Window {

    @Shadow
    private int scaleFactor;
    @Shadow
    private double scaledWidth, scaledHeight;

    @Override
    public int width() {
        // The 1.8 Window fields are already GUI-scaled. NanoVG's frame is
        // physical, just like the modern Window adapter, so read the display
        // framebuffer directly here and apply the GUI scale once in the shared
        // renderer.
        return org.lwjgl.opengl.Display.getWidth();
    }

    @Override
    public int height() {
        return org.lwjgl.opengl.Display.getHeight();
    }

    @Override
    public int scaleFactor() {
        return scaleFactor;
    }

    @Override
    public int scaledWidth() {
        return (int) scaledWidth;
    }

    @Override
    public int scaledHeight() {
        return (int) scaledHeight;
    }

    @Override
    public double scaledWidthD() {
        return scaledWidth;
    }

    @Override
    public double scaledHeightD() {
        return scaledHeight;
    }

}
