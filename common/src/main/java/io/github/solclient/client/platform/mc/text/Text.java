package io.github.solclient.client.platform.mc.text;

import java.util.*;

import org.jetbrains.annotations.*;

import io.github.solclient.client.platform.Helper;
import io.github.solclient.client.platform.PlatformServices;

public interface Text extends Cloneable {

	static @NotNull MutableText literal(@NotNull String text) {
		return PlatformServices.get().literal(text);
	}

	static @NotNull MutableText format(@NotNull String fmt, Object... args) {
		return PlatformServices.get().format(fmt, args);
	}

	static @NotNull MutableText translation(String key, Object... args) {
		return PlatformServices.get().translation(key, args);
	}

	@Helper
	static boolean plainEquals(@Nullable Text text, @NotNull String to) {
		Objects.requireNonNull(to);

		if(text == null) {
			return false;
		}

		return text.getPlain().equals(to);
	}

	@NotNull List<Text> getExtraText();

	@NotNull Text clone();

	@NotNull String getPlain();

	@NotNull Style getStyle();

	@NotNull
	String getLegacy();

}
