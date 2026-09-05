package io.github.solclient.client.platform.mc.screen;

import org.jetbrains.annotations.*;

import io.github.solclient.client.platform.PlatformServices;

public interface SingleplayerScreen extends Screen {

	static @NotNull SingleplayerScreen create(@Nullable Screen parent) {
		return PlatformServices.get().createSingleplayerScreen(parent);
	}

}
