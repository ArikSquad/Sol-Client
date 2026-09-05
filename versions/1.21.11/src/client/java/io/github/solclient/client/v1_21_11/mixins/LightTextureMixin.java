package io.github.solclient.client.v1_21_11.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.GammaEvent;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {

	@Redirect(method = "updateLightTexture", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 2))
	private Object solClient$gamma(OptionInstance<?> option) {
		float gamma = ((Number) option.get()).floatValue();
		return Double.valueOf(EventBus.DEFAULT.post(new GammaEvent(gamma)).getGamma());
	}
}
