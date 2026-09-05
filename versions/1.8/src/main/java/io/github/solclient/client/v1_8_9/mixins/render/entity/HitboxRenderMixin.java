package io.github.solclient.client.v1_8_9.mixins.render.entity;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.entity.render.HitboxRenderEvent;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the legacy debug hitbox geometry with the common event path.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class HitboxRenderMixin {

    @Inject(method = "method_4328(Lnet/minecraft/entity/Entity;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void solClient$renderHitbox(Entity entity, double x, double y, double z, float yaw, float tickDelta,
                                        CallbackInfo callback) {
        HitboxRenderEvent event = EventBus.DEFAULT.post(new HitboxRenderEvent(
                (io.github.solclient.client.platform.mc.world.entity.Entity) (Object) entity,
                tickDelta, x, y, z));
        if (event.isCancelled()) callback.cancel();
    }
}
