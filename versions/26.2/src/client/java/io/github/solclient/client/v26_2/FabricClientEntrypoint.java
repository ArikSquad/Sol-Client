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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.solclient.common.ClientBootstrap;
import io.github.solclient.common.ClientPlatform;
import io.github.solclient.common.GameVersions;
import io.github.solclient.client.platform.PlatformServices;
import net.fabricmc.api.ClientModInitializer;

public final class FabricClientEntrypoint implements ClientModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger("sol-client/26.2");

	@Override
	public void onInitializeClient() {
		FabricEventBridge.install();
		ClientBootstrap.install(GameVersions.FABRIC_26_2, new ClientPlatform() {
			@Override
			public String getName() {
				return "Fabric 26.2";
			}

			@Override
			public void log(String message) {
				LOGGER.info(message);
			}

			@Override
			public void onClientReady(io.github.solclient.common.ClientContext context) {
				((FabricPlatform) PlatformServices.get()).showMainMenu();
			}
		}, new FabricPlatform());
	}
}
