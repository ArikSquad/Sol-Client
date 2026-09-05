package io.github.solclient.client.v26_2.mixins;

import io.github.solclient.common.ClientEvents;
import io.github.solclient.client.event.impl.sound.SoundPlayEvent;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.sound.SoundType;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

	@Inject(method = "calculateVolume", at = @At("RETURN"), cancellable = true)
	private void solClient$soundVolume(SoundInstance sound, CallbackInfoReturnable<Float> callback) {
		float original = sound.getVolume();
		SoundType type = PlatformServices.get().soundType(sound.getIdentifier().toString());
		SoundPlayEvent event = ClientEvents.sound(type, original);
		if (original != 0.0F) {
			callback.setReturnValue(callback.getReturnValueF() * event.getVolume() / original);
		}
	}
}
