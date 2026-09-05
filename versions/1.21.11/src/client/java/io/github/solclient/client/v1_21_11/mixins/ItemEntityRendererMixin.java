package io.github.solclient.client.v1_21_11.mixins;

import java.util.IdentityHashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.entity.render.ItemEntityRenderEvent;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.model.BakedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

	@Shadow @Final private RandomSource random;

	@Unique private final Map<ItemEntityRenderState, ItemEntity> solClient$entities = new IdentityHashMap<>();
	@Unique private final Map<ItemEntityRenderState, Float> solClient$partialTicks = new IdentityHashMap<>();

	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V",
		at = @At("RETURN"))
	private void solClient$rememberEntity(ItemEntity entity, ItemEntityRenderState state, float partialTicks,
			CallbackInfo callback) {
		solClient$entities.put(state, entity);
		solClient$partialTicks.put(state, partialTicks);
	}

	@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;submitMultipleFromCount(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ItemClusterRenderState;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/phys/AABB;)V"),
		cancellable = true)
	private void solClient$itemRender(ItemEntityRenderState state, PoseStack pose, SubmitNodeCollector collector,
			CameraRenderState camera, CallbackInfo callback) {
		ItemEntity entity = solClient$entities.remove(state);
		Float partialTicks = solClient$partialTicks.remove(state);
		if (entity == null) return;

		ItemStackRenderState item = state.item;
		AABB bounds = item.getModelBoundingBox();
		boolean gui3d = bounds != null && bounds.getZsize() > 0.0625D;
		ItemEntityRenderEvent event = new ItemEntityRenderEvent(
				(io.github.solclient.client.platform.mc.world.entity.item.ItemEntity)
						((io.github.solclient.client.v1_21_11.FabricPlatform) PlatformServices.get()).commonEntity(entity),
				BakedModel.create(gui3d), 0.0D, 0.0D, 0.0D,
				partialTicks == null ? 0.0F : partialTicks);

		PlatformServices.enterRenderBackend(new io.github.solclient.client.v1_21_11.PoseStackBackend(pose));
		try {
			EventBus.DEFAULT.post(event);
			if (event.isCancelled()) {
				if (event.getReturnValue() > 0 && bounds != null) {
					ItemEntityRenderer.submitMultipleFromCount(pose, collector, event.getReturnValue(),
							(ItemClusterRenderState) state, random, bounds);
				}
				pose.popPose();
				callback.cancel();
			}
		}
		finally {
			PlatformServices.leaveRenderBackend();
		}
	}
}
