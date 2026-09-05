package io.github.solclient.client.v1_8_9.mixins.combat;

import io.github.solclient.common.ClientEvents;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class EntityDamageMixin {

    @Shadow
    private ClientWorld clientWorld;

    @Inject(method = "onEntityStatus", at = @At("RETURN"))
    private void solClient$damage(EntityStatusS2CPacket packet, CallbackInfo callback) {
        if (packet.getStatus() == 2 && clientWorld != null) {
            net.minecraft.entity.Entity entity = packet.getEntity(clientWorld);
            if (entity != null) {
                ClientEvents.damage((io.github.solclient.client.platform.mc.world.entity.Entity) (Object) entity);
            }
        }
    }
}
