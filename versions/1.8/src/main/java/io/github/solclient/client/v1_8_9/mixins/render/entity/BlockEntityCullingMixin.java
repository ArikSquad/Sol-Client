package io.github.solclient.client.v1_8_9.mixins.render.entity;

import io.github.solclient.client.culling.Cullable;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the shared asynchronous culling result before legacy block-entity rendering.
 */
@Mixin(net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher.class)
public abstract class BlockEntityCullingMixin {

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private void platform$skipCulled(BlockEntity entity, float tickDelta, int light, CallbackInfo callback) {
        if (entity instanceof Cullable && ((Cullable) entity).isCulled()) {
            callback.cancel();
        }
    }
}
