package io.github.solclient.client.platform.mc.sound;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.Helper;
import io.github.solclient.client.platform.PlatformServices;

public interface SoundInstance {

	@Helper
	static @NotNull SoundInstance ui(SoundType sound, float pitch) {
		return PlatformServices.get().uiSound(sound, pitch);
	}

}
