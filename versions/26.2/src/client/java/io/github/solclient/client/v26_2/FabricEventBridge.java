/*
 * Sol Client - an open source Minecraft client
 * Copyright (C) 2021-2026 TheKodeToad and Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package io.github.solclient.client.v26_2;

import io.github.solclient.common.ClientBootstrap;
import io.github.solclient.common.ClientEvents;
import io.github.solclient.client.platform.PlatformServices;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class FabricEventBridge {

	private static boolean installed;

	private FabricEventBridge() {
	}

	public static synchronized void install() {
		if (installed) {
			return;
		}
		installed = true;

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> ClientBootstrap.start());
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ClientEvents.quit());

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ClientBootstrap.start();
			ClientEvents.levelLoaded(PlatformServices.get().minecraftClient().getLevel());
		});

		ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, timestamp) -> {
			ClientBootstrap.start();
			return ClientEvents.allowIncomingChat(NativeAdapters.text(message));
		});
		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
			ClientBootstrap.start();
			return ClientEvents.allowIncomingGame(NativeAdapters.text(message), overlay);
		});

		ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
			ClientBootstrap.start();
			return ClientEvents.allowOutgoing(message);
		});
		ClientSendMessageEvents.MODIFY_CHAT.register(message -> ClientEvents.modifyOutgoing(message, false));
		ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {
			ClientBootstrap.start();
			return ClientEvents.allowOutgoing("/" + command);
		});
		ClientSendMessageEvents.MODIFY_COMMAND.register(command -> ClientEvents.modifyOutgoing(command, true));
	}

}
