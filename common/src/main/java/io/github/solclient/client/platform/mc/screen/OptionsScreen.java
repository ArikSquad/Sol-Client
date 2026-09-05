package io.github.solclient.client.platform.mc.screen;

import org.jetbrains.annotations.*;

import io.github.solclient.client.platform.mc.option.Options;
import io.github.solclient.client.platform.PlatformServices;

public interface OptionsScreen extends Screen {

	static @NotNull OptionsScreen create(@Nullable Screen parent, @NotNull Options options) {
		return PlatformServices.get().createOptionsScreen(parent, options);
	}

}
