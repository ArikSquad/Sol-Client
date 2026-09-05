package io.github.solclient.client.v1_21_11.mixins;

import io.github.solclient.client.culling.Cullable;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.renderer.entity.EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherCullingMixin {

	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void solClient$skipCulled(Entity entity, Frustum frustum, double x, double y, double z,
			CallbackInfoReturnable<Boolean> callback) {
		if (entity instanceof Cullable && ((Cullable) entity).isCulled()) {
			callback.setReturnValue(false);
		}
	}
}
