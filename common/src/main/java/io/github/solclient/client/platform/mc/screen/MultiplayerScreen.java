package io.github.solclient.client.platform.mc.screen;

import org.jetbrains.annotations.*;

import io.github.solclient.client.platform.PlatformServices;

public interface MultiplayerScreen extends Screen {

	static @NotNull MultiplayerScreen create(@Nullable Screen parent) {
		return PlatformServices.get().createMultiplayerScreen(parent);
	}

}
