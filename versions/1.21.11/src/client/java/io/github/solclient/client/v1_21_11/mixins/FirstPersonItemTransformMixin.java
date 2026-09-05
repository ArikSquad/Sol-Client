package io.github.solclient.client.v1_21_11.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.item.FirstPersonItemTransformEvent;
import io.github.solclient.client.platform.PlatformServices;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonItemTransformMixin {

	@Inject(method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
		at = @At("HEAD"))
	private void solClient$transform(AbstractClientPlayer player, float pitch, float swing,
			InteractionHand hand, float equip, ItemStack item, float partialTicks, PoseStack pose,
			SubmitNodeCollector collector, int light, CallbackInfo callback) {
		if (item == null || item.isEmpty()) return;
		PlatformServices.enterRenderBackend(new io.github.solclient.client.v1_21_11.PoseStackBackend(pose));
		try {
			EventBus.DEFAULT.post(new FirstPersonItemTransformEvent(
					((io.github.solclient.client.v1_21_11.FabricPlatform) PlatformServices.get()).commonItemStack(item)));
		}
		finally {
			PlatformServices.leaveRenderBackend();
		}
	}
}
