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

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.game.FullscreenToggleEvent;
import io.github.solclient.client.event.impl.game.QuitEvent;
import io.github.solclient.client.event.impl.network.ServerMessageReceiveEvent;
import io.github.solclient.client.event.impl.network.chat.ActionBarPlayEvent;
import io.github.solclient.client.event.impl.network.chat.IncomingChatMessageEvent;
import io.github.solclient.client.event.impl.network.chat.OutgoingChatMessageEvent;
import io.github.solclient.client.event.impl.sound.SoundPlayEvent;
import io.github.solclient.client.event.impl.world.entity.EntityAttackEvent;
import io.github.solclient.client.event.impl.world.entity.EntityDamageEffectEvent;
import io.github.solclient.client.event.impl.world.level.LevelLoadEvent;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.platform.mc.sound.SoundType;
import io.github.solclient.client.platform.mc.text.Text;
import io.github.solclient.client.platform.mc.world.entity.Entity;
import io.github.solclient.client.platform.mc.world.level.ClientLevel;

import java.util.Objects;

public final class ClientEvents {

    private static final ThreadLocal<OutgoingChatMessageEvent> PENDING_OUTGOING = new ThreadLocal<>();
    private static boolean quitPosted;

    private ClientEvents() {
    }

    public static boolean allowIncomingChat(Text message) {
        return !EventBus.DEFAULT.post(new IncomingChatMessageEvent(Objects.requireNonNull(message, "message")))
                .isCancelled();
    }

    public static boolean allowIncomingGame(Text message, boolean overlay) {
        Objects.requireNonNull(message, "message");
        if (overlay) {
            return !EventBus.DEFAULT.post(new ActionBarPlayEvent(message)).isCancelled();
        }
        return !EventBus.DEFAULT.post(new IncomingChatMessageEvent(message)).isCancelled();
    }

    /**
     * Starts the common outgoing-message event. Commands are passed in with a
     * leading slash so the common command registry sees the same shape on every
     * Minecraft version.
     */
    public static boolean allowOutgoing(String message) {
        OutgoingChatMessageEvent event = EventBus.DEFAULT.post(new OutgoingChatMessageEvent(
                Objects.requireNonNull(message, "message")));
        if (event.isCancelled()) {
            PENDING_OUTGOING.remove();
            return false;
        }
        PENDING_OUTGOING.set(event);
        return true;
    }

    /**
     * Completes the Fabric send-message callback and returns any common-side
     * mutation. Fabric separates its allow and modify callbacks; the
     * thread-local keeps that loader detail out of common features.
     */
    public static String modifyOutgoing(String fallback, boolean command) {
        OutgoingChatMessageEvent event = PENDING_OUTGOING.get();
        PENDING_OUTGOING.remove();
        String value = event == null ? fallback : event.getMessage();
        if (value == null) {
            value = fallback;
        }
        if (command && value.startsWith("/")) {
            return value.substring(1);
        }
        return value;
    }

    public static void levelLoaded(ClientLevel level) {
        if (level != null) {
            EventBus.DEFAULT.post(new LevelLoadEvent(level));
        }
    }

    public static void attack(Entity entity) {
        if (entity != null) {
            EventBus.DEFAULT.post(new EntityAttackEvent(entity));
        }
    }

    public static void damage(Entity entity) {
        if (entity != null) {
            EventBus.DEFAULT.post(new EntityDamageEffectEvent(entity));
        }
    }

    public static SoundPlayEvent sound(SoundType type, float volume) {
        SoundPlayEvent event = new SoundPlayEvent();
        event.setType(type);
        event.setVolume(volume);
        return EventBus.DEFAULT.post(event);
    }

    public static void serverMessage(Identifier channelId, String channel, String data) {
        EventBus.DEFAULT.post(new ServerMessageReceiveEvent(channelId, channel, data));
    }

    public static FullscreenToggleEvent fullscreen(boolean state) {
        FullscreenToggleEvent event = new FullscreenToggleEvent();
        event.setState(state);
        return EventBus.DEFAULT.post(event);
    }

    public static synchronized void quit() {
        if (!quitPosted) {
            quitPosted = true;
            EventBus.DEFAULT.post(new QuitEvent());
            PlatformServices.closeUiRenderer();
        }
    }

}
