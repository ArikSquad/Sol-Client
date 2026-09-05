/*
 * Sol Client - an open source Minecraft client
 * Copyright (C) 2021-2026 TheKodeToad and Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package io.github.solclient.common;

import java.util.Objects;

public final class ClientRuntime {

    private static volatile ClientContext context;

    private ClientRuntime() {
    }

    public static synchronized ClientContext initialize(GameVersion version, ClientPlatform platform) {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(platform, "platform");

        if (context != null) {
            if (context.getVersion().equals(version)) {
                return context;
            }
            throw new IllegalStateException("Sol Client is already initialized for " + context.getVersion());
        }

        ClientContext next = new ClientContext(version, platform);
        platform.log("Initializing Sol Client for " + version + " on " + platform.getName());
        try {
            platform.initialize(next);
            context = next;
            platform.log("Common client runtime initialized");
            return next;
        } catch (RuntimeException | Error error) {
            platform.log("Common client runtime failed to initialize", error);
            throw error;
        }
    }

    public static synchronized boolean isInitialized() {
        return context != null;
    }

    public static synchronized ClientContext getContext() {
        return context;
    }

    public static void tick() {
        ClientContext current = context;
        if (current != null) {
            current.tick();
        }
    }
}
