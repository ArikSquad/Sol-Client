package io.github.solclient.client.v26_2.mixins;

import io.github.solclient.client.culling.Cullable;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherCullingMixin {

	@Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true)
	private void solClient$skipCulled(BlockEntity entity, float tickDelta,
			ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, boolean outline,
			CallbackInfoReturnable<?> callback) {
		if (entity instanceof Cullable && ((Cullable) entity).isCulled()) {
			callback.setReturnValue(null);
		}
	}
}
