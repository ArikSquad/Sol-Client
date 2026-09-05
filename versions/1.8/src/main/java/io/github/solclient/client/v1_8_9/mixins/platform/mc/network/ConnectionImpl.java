package io.github.solclient.client.v1_8_9.mixins.platform.mc.network;

import io.github.solclient.client.platform.mc.network.C2SChatMessagePacket;
import io.github.solclient.client.platform.mc.network.Connection;
import io.github.solclient.client.platform.mc.network.Packet;
import io.github.solclient.client.platform.mc.network.PlayerListEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(net.minecraft.client.network.ClientPlayNetworkHandler.class)
@Implements(@Interface(iface = Connection.class, prefix = "platform$"))
public abstract class ConnectionImpl {

    public void platform$sendPacket(@NotNull Packet packet) {
        if (packet instanceof C2SChatMessagePacket) {
            // 1.8.9 sends chat through ClientPlayerEntity; the play network
            // handler only exposes the generic packet method.
            net.minecraft.client.MinecraftClient.getInstance().player
                    .sendChatMessage(((C2SChatMessagePacket) packet).message());
        }
    }

    public @Nullable PlayerListEntry platform$getPlayerListEntry(@NotNull UUID uuid) {
        net.minecraft.client.network.PlayerListEntry entry = getPlayerListEntry(uuid);
        return entry == null ? null : (PlayerListEntry) (Object) entry;
    }

    @Shadow
    public abstract net.minecraft.client.network.PlayerListEntry getPlayerListEntry(UUID uuid);

}
