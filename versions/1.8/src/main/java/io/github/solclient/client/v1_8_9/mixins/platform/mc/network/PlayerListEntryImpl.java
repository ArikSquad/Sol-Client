package io.github.solclient.client.v1_8_9.mixins.platform.mc.network;

import io.github.solclient.client.platform.mc.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.client.network.PlayerListEntry.class)
@Implements(@Interface(iface = PlayerListEntry.class, prefix = "platform$"))
public abstract class PlayerListEntryImpl {

    public int platform$getPing() {
        return getLatency();
    }

    @org.spongepowered.asm.mixin.Shadow
    public abstract int getLatency();

}
