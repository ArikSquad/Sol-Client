package io.github.solclient.client.platform.mc.option;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.PlatformServices;

public interface ToggleKeyBinding extends KeyBinding {

	/**
	 * Create a toggle key binding.
	 * @param name
	 * @param initialKey
	 * @param category
	 * @param toggleMode Returns whether the key binding should act as "normal".
	 * @param maxHoldTime The max hold time before acting "normal".
	 * @return The key binding.
	 */
	static @NotNull ToggleKeyBinding create(@NotNull String name, int initialKey, @NotNull String category,
			BooleanSupplier toggleMode, int maxHoldTime) {
		return PlatformServices.get().createToggleKeyBinding(name, initialKey, category, toggleMode, maxHoldTime);
	}

	/**
	 * Returns whether the key binding is actually held on the keyboard.
	 * @return The actual value of isHeld.
	 */
	boolean realHeld();

}
