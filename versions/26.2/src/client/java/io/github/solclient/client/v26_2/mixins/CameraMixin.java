package io.github.solclient.client.v26_2.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.FovEvent;
import io.github.solclient.client.event.impl.world.CameraTransformEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {

	@org.spongepowered.asm.mixin.injection.ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 0))
	private void solClient$cameraTransform(org.spongepowered.asm.mixin.injection.invoke.arg.Args args) {
		CameraTransformEvent event = EventBus.DEFAULT.post(new CameraTransformEvent(
				(float) args.get(0), (float) args.get(1)));
		args.set(0, event.getYaw());
		args.set(1, event.getPitch());
	}

	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void solClient$fov(CallbackInfoReturnable<Float> callback) {
		callback.setReturnValue((float) EventBus.DEFAULT.post(new FovEvent(
				callback.getReturnValueF(), Minecraft.getInstance().getDeltaTracker()
						.getGameTimeDeltaPartialTick(false))).getFov());
	}
}
