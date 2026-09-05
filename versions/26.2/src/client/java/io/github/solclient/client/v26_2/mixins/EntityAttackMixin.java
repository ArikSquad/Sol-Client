package io.github.solclient.client.v26_2.mixins;

import io.github.solclient.common.ClientEvents;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.v26_2.FabricPlatform;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class EntityAttackMixin {

	@Inject(method = "attack", at = @At("HEAD"))
	private void solClient$attack(Player player, Entity entity, CallbackInfo callback) {
		if (entity != null) {
			ClientEvents.attack(((FabricPlatform) PlatformServices.get()).commonEntity(entity));
		}
	}
}
