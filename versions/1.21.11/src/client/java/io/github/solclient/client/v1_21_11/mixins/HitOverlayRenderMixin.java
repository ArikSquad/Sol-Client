package io.github.solclient.client.v1_21_11.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.entity.render.HitOverlayRenderEvent;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.v1_21_11.FabricPlatform;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class HitOverlayRenderMixin {

	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;"
			+ "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("RETURN"))
	private void solClient$hitOverlay(LivingEntity entity, LivingEntityRenderState state, float tickDelta,
			CallbackInfo callback) {
		if (!state.hasRedOverlay) return;
		HitOverlayRenderEvent event = EventBus.DEFAULT.post(new HitOverlayRenderEvent());
		// Modern Minecraft stores hurt feedback as a render-state flag and owns
		// the red overlay texture. Honour the common alpha/hide decision here;
		// RGB replacement remains a renderer-owned concern on this pipeline.
		if (event.getA() <= 0.0F) state.hasRedOverlay = false;
	}
}
