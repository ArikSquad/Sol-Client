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

import io.github.solclient.client.Client;
import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.game.PostStartEvent;
import io.github.solclient.client.event.impl.game.PostTickEvent;
import io.github.solclient.client.event.impl.game.PreTickEvent;
import io.github.solclient.client.platform.PlatformServices;

import java.util.Objects;

/**
 * Coordinates the loader entrypoint, the first native-client callback, and
 * the common client lifecycle.
 *
 * <p>Loader entrypoints run before Minecraft has a usable client instance, so
 * they only install the version provider here. The actual Sol Client object is
 * created lazily from the first game-thread tick. This is the key boundary
 * that keeps version modules small and keeps common feature code shared.</p>
 */
public final class ClientBootstrap {

    private static volatile ClientContext context;
    private static volatile boolean started;
    private static volatile boolean postStarted;

    private ClientBootstrap() {
    }

    public static synchronized ClientContext install(GameVersion version, ClientPlatform platform,
                                                     PlatformServices.Provider services) {
        Objects.requireNonNull(services, "services");
        PlatformServices.install(services);

        if (context == null) {
            context = ClientRuntime.initialize(version, platform);
        } else if (!context.getVersion().equals(version)) {
            throw new IllegalStateException("Sol Client is already installed for " + context.getVersion());
        }

        return context;
    }

    public static synchronized boolean isInstalled() {
        return context != null;
    }

    public static synchronized boolean isStarted() {
        return started;
    }

    public static void start() {
        ClientContext current;
        synchronized (ClientBootstrap.class) {
            if (started || context == null) {
                return;
            }
            current = context;
            Client.INSTANCE.init();
            started = true;
        }

        try {
            current.getPlatform().onClientReady(current);
        } catch (Throwable error) {
            current.getPlatform().log("Could not finish native client setup", error);
        }

        synchronized (ClientBootstrap.class) {
            if (!postStarted) {
                postStarted = true;
                EventBus.DEFAULT.post(new PostStartEvent());
            }
        }
    }

    public static void preTick() {
        start();
        if (started) {
            EventBus.DEFAULT.post(new PreTickEvent());
        }
    }

    public static void postTick() {
        if (started) {
            ClientRuntime.tick();
            EventBus.DEFAULT.post(new PostTickEvent());
        }
    }

    /**
     * Convenience bridge for adapters which only have one tick callback. New
     * adapters should prefer {@link #preTick()} and {@link #postTick()} so the
     * common events retain their original ordering around the native tick.
     */
    public static void tick() {
        preTick();
        postTick();
    }

}
