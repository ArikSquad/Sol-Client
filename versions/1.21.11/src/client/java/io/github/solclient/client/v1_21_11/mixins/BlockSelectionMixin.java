package io.github.solclient.client.v1_21_11.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.level.BlockSelectionRenderEvent;
import io.github.solclient.client.platform.PlatformServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class BlockSelectionMixin {

	@Inject(method = "renderBlockOutline", at = @At("HEAD"), cancellable = true)
	private void solClient$blockSelection(MultiBufferSource.BufferSource buffers, PoseStack pose,
			boolean translucent, LevelRenderState state, CallbackInfo callback) {
		Minecraft minecraft = Minecraft.getInstance();
		DeltaTracker delta = minecraft.getDeltaTracker();
		if (EventBus.DEFAULT.post(new BlockSelectionRenderEvent(
				PlatformServices.get().minecraftClient().getHitResult(),
				delta.getGameTimeDeltaPartialTick(false))).isCancelled()) {
			callback.cancel();
		}
	}
}
