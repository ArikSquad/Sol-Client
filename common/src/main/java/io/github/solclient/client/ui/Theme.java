package io.github.solclient.client.ui;

import java.util.function.Supplier;

import io.github.solclient.client.ui.component.controller.AnimatedColourController;
import io.github.solclient.client.ui.component.controller.Controller;
import io.github.solclient.client.util.data.Colour;
import lombok.Getter;
import lombok.Setter;

public class Theme implements Cloneable {

	public static final Theme DARK;
	public static final Theme LIGHT;

	@Getter
	@Setter
	private static Theme current;

	static {
		DARK = new Theme();
		DARK.bg = new Colour(0xFF1F1F1F);
		DARK.button = new Colour(0xFF2A2A2A);
		DARK.buttonHover = new Colour(0xFF2E2E2E);
		DARK.buttonSecondary = new Colour(0xFF383838);
		DARK.buttonSecondaryHover = new Colour(0xFF3C3C3C);
		DARK.fg = Colour.WHITE;
		DARK.accent = new Colour(0xFFFFB400);
		DARK.accentHover = new Colour(0xFFFFCD26);
		DARK.accentFg = new Colour(0xFF1F1F1F);
		DARK.fgButton = Colour.WHITE;
		DARK.fgButtonHover = new Colour(0xFFD4D4D4);
		DARK.transparent1 = new Colour(0xFF282828);
		DARK.transparent2 = new Colour(0xFF3C3C3C);
		DARK.overlay = new Colour(0x80000000);
		DARK.overlayHover = new Colour(0x80FFFFFF);
		DARK.muted = new Colour(0xFFA0A0A0);
		DARK.mutedStrong = new Colour(0xFF787878);
		DARK.selection = new Colour(0xFF3E78B3);
		DARK.checkerLight = new Colour(0xFF464646);
		DARK.checkerDark = new Colour(0xFF323232);
		DARK.danger = new Colour(0xFFFF2929);
		DARK.dangerHover = new Colour(0xFFFF4B4B);

		LIGHT = DARK.clone();
		LIGHT.bg = new Colour(0xFFEBEBEB);
		LIGHT.button = new Colour(0xFFDFDFDF);
		LIGHT.buttonHover = new Colour(0xFFD9D9D9);
		LIGHT.buttonSecondary = new Colour(0xFFCCCCCC);
		LIGHT.buttonSecondaryHover = new Colour(0xFFC7C7C7);
		LIGHT.fg = new Colour(0xFF2A2A2A);
		LIGHT.muted = new Colour(0xFF606060);
		LIGHT.mutedStrong = new Colour(0xFF777777);
		LIGHT.selection = new Colour(0xFF7BAED6);
		LIGHT.checkerLight = new Colour(0xFFD0D0D0);
		LIGHT.checkerDark = new Colour(0xFFB8B8B8);
		LIGHT.accentHover = new Colour(0xFFFAA000);
		LIGHT.fgButton = new Colour(0xFF2A2A2A);
		LIGHT.fgButtonHover = new Colour(0xFF454545);
		LIGHT.overlay = new Colour(0x80000000);
		LIGHT.overlayHover = new Colour(0x80FFFFFF);

		current = DARK;
	}

	public Colour bg;
	public Colour button;
	public Colour buttonHover;
	public Colour buttonSecondary;
	public Colour buttonSecondaryHover;
	public Colour fgButton;
	public Colour fgButtonHover;
	public Colour fg;
	public Colour accent;
	public Colour accentHover;
	public Colour accentFg;
	public Colour transparent1;
	public Colour transparent2;
	public Colour overlay;
	public Colour overlayHover;
	public Colour muted;
	public Colour mutedStrong;
	public Colour selection;
	public Colour checkerLight;
	public Colour checkerDark;
	public Colour danger;
	public Colour dangerHover;

	public static Controller<Colour> bg() {
		return new AnimatedColourController((component, fallback) -> current.bg);
	}

	public static Controller<Colour> fg() {
		return new AnimatedColourController((component, fallback) -> current.fg);
	}

	public static Controller<Colour> accentFg() {
		return new AnimatedColourController((component, fallback) -> current.accentFg);
	}

	private static Controller<Colour> hoverPair(Supplier<Colour> base, Supplier<Colour> hover) {
		return new AnimatedColourController((component, fallback) -> component.isHovered() ? hover.get() : base.get());
	}

	public static Controller<Colour> button() {
		return hoverPair(() -> current.button, () -> current.buttonHover);
	}

	public static Controller<Colour> buttonSecondary() {
		return hoverPair(() -> current.buttonSecondary, () -> current.buttonSecondaryHover);
	}

	public static Controller<Colour> fgButton() {
		return hoverPair(() -> current.fgButton, () -> current.fgButtonHover);
	}

	public static Controller<Colour> accent() {
		return hoverPair(() -> current.accent, () -> current.accentHover);
	}

	public static Controller<Colour> danger() {
		return hoverPair(() -> current.danger, () -> current.dangerHover);
	}

	@Override
	public Theme clone() {
		try {
			return (Theme) super.clone();
		}
		catch(CloneNotSupportedException error) {
			throw new AssertionError("Theme should implement Cloneable", error);
		}
	}
}
