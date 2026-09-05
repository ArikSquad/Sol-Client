package io.github.solclient.client.v1_21_11.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.CameraTransformEvent;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraTransformMixin {

	@ModifyArgs(method = "setup", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 0))
	private void solClient$cameraTransform(Args args) {
		CameraTransformEvent event = EventBus.DEFAULT.post(new CameraTransformEvent(
				(float) args.get(0), (float) args.get(1)));
		args.set(0, event.getYaw());
		args.set(1, event.getPitch());
	}
}
