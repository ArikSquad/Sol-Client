package io.github.solclient.client.v26_2.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.input.MouseDownEvent;
import io.github.solclient.client.event.impl.input.ScrollWheelEvent;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.WrapWithCondition;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

	@Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
	private void solClient$button(long window, MouseButtonInfo button, int action, CallbackInfo callback) {
		if (action == 1 && EventBus.DEFAULT.post(new MouseDownEvent(button.button())).isCancelled()) {
			callback.cancel();
		}
	}

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void solClient$scroll(long window, double horizontal, double vertical, CallbackInfo callback) {
		int amount = vertical > 0.0D ? 1 : vertical < 0.0D ? -1 : 0;
		if (amount != 0 && EventBus.DEFAULT.post(new ScrollWheelEvent(amount)).isCancelled()) {
			callback.cancel();
		}
	}

	@WrapWithCondition(method = "turnPlayer", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private boolean solClient$cameraRotate(LocalPlayer player, double yaw, double pitch) {
		return !EventBus.DEFAULT.post(new io.github.solclient.client.event.impl.input.CameraRotateEvent(
				(float) yaw, (float) -pitch)).isCancelled();
	}
}
