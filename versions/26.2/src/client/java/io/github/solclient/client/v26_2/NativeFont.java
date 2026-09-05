package io.github.solclient.client.v26_2;

import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.text.Font;
import io.github.solclient.client.platform.mc.text.Text;
import org.jspecify.annotations.NonNull;

final class NativeFont implements Font {

    private final net.minecraft.client.gui.Font font;

    NativeFont(net.minecraft.client.gui.Font font) {
        this.font = font;
    }

    @Override
    public int render(@NonNull String text, int x, int y, int rgb) {
        return render(text, x, y, rgb, false);
    }

    @Override
    public int render(@NonNull String text, int x, int y, int rgb, boolean shadow) {
        return PlatformServices.renderBackend().render(text, x, y, rgb, shadow);
    }

    @Override
    public int renderWithShadow(@NonNull String text, int x, int y, int rgb) {
        return render(text, x, y, rgb, true);
    }

    @Override
    public int render(Text text, int x, int y, int rgb) {
        return render(text, x, y, rgb, false);
    }

    @Override
    public int render(Text text, int x, int y, int rgb, boolean shadow) {
        return PlatformServices.renderBackend().render(text, x, y, rgb, shadow);
    }

    @Override
    public int renderWithShadow(Text text, int x, int y, int rgb) {
        return render(text, x, y, rgb, true);
    }

    @Override
    public int getCharacterWidth(char character) {
        return font.width(String.valueOf(character));
    }

    @Override
    public int getTextWidth(String text) {
        return font.width(text);
    }

    @Override
    public int getTextWidth(Text text) {
        return font.width(text == null ? "" : text.getPlain());
    }

    @Override
    public int getHeight() {
        return font.lineHeight;
    }

    net.minecraft.client.gui.Font nativeFont() {
        return font;
    }
}
