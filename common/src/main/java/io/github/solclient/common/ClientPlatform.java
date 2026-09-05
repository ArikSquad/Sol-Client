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

public interface ClientPlatform {

    String getName();

    void log(String message);

    default void log(String message, Throwable error) {
        log(message + ": " + error.getClass().getName() + ": " + error.getMessage());
    }

    default void initialize(ClientContext context) {
    }

    /**
     * Called on the game thread once the native client is ready and before the
     * first common tick. Version adapters use this to install their shared main
     * screen without making common code know a Minecraft class.
     */
    default void onClientReady(ClientContext context) {
    }
}
