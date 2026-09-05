package io.github.solclient.client.platform.mc.lang;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.PlatformServices;
import lombok.experimental.UtilityClass;

@UtilityClass
public class I18n {

	public @NotNull String translate(@NotNull String key) {
		return PlatformServices.get().translate(key);
	}

	public @NotNull String translate(@NotNull String key, @NotNull Object... values) {
		return PlatformServices.get().translate(key, values);
	}

	public @NotNull Optional<String> translateOpt(@NotNull String key) {
		return PlatformServices.get().translateOptional(key);
	}

	public @NotNull Optional<String> translateOpt(@NotNull String key, @NotNull Object... values) {
		return PlatformServices.get().translateOptional(key, values);
	}

}
