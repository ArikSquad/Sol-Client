package io.github.solclient.client.v1_8_9.mixins.render;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.level.BlockSelectionRenderEvent;
import io.github.solclient.client.platform.PlatformServices;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bridges the legacy block outline method to the common selection feature.
 */
@Mixin(WorldRenderer.class)
public abstract class BlockSelectionMixin {

    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void solClient$blockSelection(PlayerEntity player, BlockHitResult hit, int subMode,
                                          float tickDelta, CallbackInfo callback) {
        if (EventBus.DEFAULT.post(new BlockSelectionRenderEvent(
                PlatformServices.get().minecraftClient().getHitResult(), tickDelta)).isCancelled()) {
            callback.cancel();
        }
    }
}
