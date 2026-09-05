package io.github.solclient.client.platform.mc.world.entity.effect;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.PlatformServices;

/**
 * Represents an instance of a status effect.
 */
public interface StatusEffect {

	static StatusEffect create(StatusEffectType type) {
		return PlatformServices.get().statusEffect(type, 0, 0);
	}

	static StatusEffect create(StatusEffectType type, int duration) {
		return PlatformServices.get().statusEffect(type, duration, 0);
	}

	static StatusEffect create(StatusEffectType type, int duration, int amplifier) {
		return PlatformServices.get().statusEffect(type, duration, amplifier);
	}

	@NotNull StatusEffectType getType();

	int getEffectDuration();

	@NotNull String getDurationText();

	int getEffectAmplifier();

	boolean showIcon();

	boolean showAmplifier();

	@NotNull String getAmplifierName();

}
