package io.github.solclient.client.v1_21_11.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.entity.render.HitboxRenderEvent;
import io.github.solclient.client.platform.PlatformServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class HitboxRenderMixin {

	@Inject(method = "showHitboxes(Lnet/minecraft/world/entity/Entity;FZ)V", at = @At("HEAD"), cancellable = true)
	private void solClient$renderHitbox(Entity entity, float partialTicks, boolean serverEntity,
			CallbackInfo callback) {
		// The vanilla renderer calls this twice for an integrated server entity. The
		// common client feature owns the client pass; avoid drawing it twice.
		if (serverEntity) return;

		Minecraft minecraft = Minecraft.getInstance();
		Vec3 position = entity.getPosition(partialTicks);
		Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
		HitboxRenderEvent event = new HitboxRenderEvent(
				((io.github.solclient.client.v1_21_11.FabricPlatform) PlatformServices.get()).commonEntity(entity),
				partialTicks, position.x - camera.x, position.y - camera.y, position.z - camera.z);
		if (EventBus.DEFAULT.post(event).isCancelled()) callback.cancel();
	}
}
