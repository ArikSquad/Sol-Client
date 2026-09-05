package io.github.solclient.client.v1_21_11.mixins;

import io.github.solclient.common.ClientEvents;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.v1_21_11.FabricPlatform;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class EntityDamageMixin {

	@Shadow private ClientLevel level;

	@Inject(method = "handleHurtAnimation", at = @At("RETURN"))
	private void solClient$damage(ClientboundHurtAnimationPacket packet, CallbackInfo callback) {
		if (level != null) {
			net.minecraft.world.entity.Entity entity = level.getEntity(packet.id());
			if (entity != null) {
				ClientEvents.damage(((FabricPlatform) PlatformServices.get()).commonEntity(entity));
			}
		}
	}
}
