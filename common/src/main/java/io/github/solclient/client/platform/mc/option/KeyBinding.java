package io.github.solclient.client.platform.mc.option;

import java.util.List;

import org.jetbrains.annotations.*;

import io.github.solclient.client.platform.PlatformServices;

public interface KeyBinding {

	static @NotNull KeyBinding create(@NotNull String name, int initialKey, @NotNull String category) {
		return PlatformServices.get().createKeyBinding(name, initialKey, category);
	}

	static void reload() {
		PlatformServices.get().reloadKeyBindings();
	}

	@NotNull String getKeyCategory();

	@NotNull String getName();

	int getKeyCode();

	@Nullable String getBoundKeyName();

	boolean isHeld();

	boolean consumePress();

	default void clearPresses() {
		while(consumePress()) {
			;
		}
	}

	@NotNull List<KeyBinding> getConflictingKeys();

	boolean conflicts();

}
