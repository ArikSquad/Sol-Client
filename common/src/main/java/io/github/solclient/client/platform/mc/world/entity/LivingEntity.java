package io.github.solclient.client.platform.mc.world.entity;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.mc.world.entity.effect.*;
import io.github.solclient.client.platform.mc.world.item.ItemStack;

public interface LivingEntity extends Entity {

	@NotNull Collection<StatusEffect> getEntityStatusEffects();

	boolean hasStatusEffect(StatusEffectType effect);

	boolean isEntityClimbing();

	@NotNull ItemStack getMainHandItem();

	@NotNull LivingEntityType getLivingEntityType();

	/**
	 * Whether this entity is currently using an item.
	 *
	 * Older Minecraft versions expose the state only on players.  Keeping a
	 * safe base implementation lets mob adapters implement the common entity
	 * contract without pretending that the old player-only methods exist on
	 * every LivingEntity class.
	 */
	default boolean isEntityUsingItem() {
		return false;
	}

	default int getItemUsageRemaining() {
		return 0;
	}

}
