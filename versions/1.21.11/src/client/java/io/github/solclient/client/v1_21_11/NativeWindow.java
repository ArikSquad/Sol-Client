package io.github.solclient.client.v1_21_11;

import io.github.solclient.client.platform.mc.Window;

final class NativeWindow implements Window {

    private final com.mojang.blaze3d.platform.Window window;

    NativeWindow(com.mojang.blaze3d.platform.Window window) {
        this.window = window;
    }

    @Override
    public int width() {
        return window.getWidth();
    }

    @Override
    public int height() {
        return window.getHeight();
    }

    @Override
    public int scaleFactor() {
        return window.getGuiScale();
    }

    @Override
    public int scaledWidth() {
        return window.getGuiScaledWidth();
    }

    @Override
    public int scaledHeight() {
        return window.getGuiScaledHeight();
    }

    @Override
    public double scaledWidthD() {
        return scaledWidth();
    }

    @Override
    public double scaledHeightD() {
        return scaledHeight();
    }
}
