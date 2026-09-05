package io.github.solclient.client.v26_2.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.level.DetermineTimeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelTimeMixin {

	@Inject(method = "getOverworldClockTime", at = @At("RETURN"), cancellable = true)
	private void solClient$overworldTime(CallbackInfoReturnable<Long> callback) {
		solClient$time(callback);
	}

	@Inject(method = "getDefaultClockTime", at = @At("RETURN"), cancellable = true)
	private void solClient$defaultTime(CallbackInfoReturnable<Long> callback) {
		solClient$time(callback);
	}

	private void solClient$time(CallbackInfoReturnable<Long> callback) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == (Object) this) {
			DetermineTimeEvent event = new DetermineTimeEvent();
			event.setTime(callback.getReturnValueJ());
			callback.setReturnValue(EventBus.DEFAULT.post(event).getTime());
		}
	}
}
