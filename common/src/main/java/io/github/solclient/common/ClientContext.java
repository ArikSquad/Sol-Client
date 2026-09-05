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
import java.util.concurrent.CopyOnWriteArrayList;

public final class ClientContext {

    private final GameVersion version;
    private final ClientPlatform platform;
    private final CopyOnWriteArrayList<Runnable> tickListeners = new CopyOnWriteArrayList<>();

    ClientContext(GameVersion version, ClientPlatform platform) {
        this.version = Objects.requireNonNull(version, "version");
        this.platform = Objects.requireNonNull(platform, "platform");
    }

    public GameVersion getVersion() {
        return version;
    }

    public ClientPlatform getPlatform() {
        return platform;
    }

    public void addTickListener(Runnable listener) {
        tickListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    void tick() {
        for (Runnable listener : tickListeners) {
            try {
                listener.run();
            } catch (Throwable error) {
                platform.log("A client tick listener failed", error);
            }
        }
    }
}
