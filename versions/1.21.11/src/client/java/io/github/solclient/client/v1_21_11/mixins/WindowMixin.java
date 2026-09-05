/*
 * Sol Client - an open source Minecraft client
 * Copyright (C) 2021-2026 TheKodeToad and Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package io.github.solclient.client.v1_21_11.mixins;

import io.github.solclient.common.ClientEvents;
import io.github.solclient.client.event.impl.game.FullscreenToggleEvent;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class WindowMixin {

	@Shadow
	private boolean fullscreen;

	@Inject(method = "toggleFullScreen", at = @At("HEAD"), cancellable = true)
	private void solClient$toggleFullscreen(CallbackInfo callbackInfo) {
		FullscreenToggleEvent event = ClientEvents.fullscreen(!fullscreen);
		if (event.isCancelled()) {
			callbackInfo.cancel();
		}
		else if (!event.getApplyState()) {
			fullscreen = event.getState();
			callbackInfo.cancel();
		}
	}
}
