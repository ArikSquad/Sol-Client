package io.github.solclient.client.v1_21_11.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.common.ClientBootstrap;
import io.github.solclient.client.v1_21_11.FabricPlatform;

@Mixin(net.minecraft.client.gui.screens.TitleScreen.class)
public abstract class TitleScreenMixin {

	@Inject(method = "init", at = @At("TAIL"))
	private void solClient$installSharedMenu(CallbackInfo callbackInfo) {
		if (ClientBootstrap.isStarted() && PlatformServices.get() instanceof FabricPlatform) {
			((FabricPlatform) PlatformServices.get()).showMainMenu();
		}
	}
}
