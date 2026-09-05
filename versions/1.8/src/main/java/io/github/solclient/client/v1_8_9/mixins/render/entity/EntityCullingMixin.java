package io.github.solclient.client.v1_8_9.mixins.render.entity;

import io.github.solclient.client.culling.Cullable;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the shared asynchronous culling result before legacy rendering.
 */
@Mixin(net.minecraft.client.render.entity.EntityRenderDispatcher.class)
public abstract class EntityCullingMixin {

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private void platform$skipCulled(Entity entity, float tickDelta, CallbackInfoReturnable<Boolean> callback) {
        if (entity instanceof Cullable && ((Cullable) entity).isCulled()) {
            callback.setReturnValue(false);
        }
    }
}
