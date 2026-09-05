package io.github.solclient.client.v1_8_9.mixins.combat;

import io.github.solclient.common.ClientEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class EntityAttackMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void solClient$attack(Entity entity, CallbackInfo callback) {
        if (entity != null) {
            ClientEvents.attack((io.github.solclient.client.platform.mc.world.entity.Entity) (Object) entity);
        }
    }
}
