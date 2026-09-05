package io.github.solclient.client.platform.mc.screen;

import org.jetbrains.annotations.*;

import io.github.solclient.client.platform.mc.lang.LanguageManager;
import io.github.solclient.client.platform.mc.option.Options;
import io.github.solclient.client.platform.PlatformServices;

public interface LanguageScreen extends Screen {

	static @NotNull LanguageScreen create(@Nullable Screen parent, @NotNull Options options, @NotNull LanguageManager languages) {
		return PlatformServices.get().createLanguageScreen(parent, options, languages);
	}

}
