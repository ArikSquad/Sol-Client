package io.github.solclient.client.v1_8_9.mixins.platform.mc.resource;

import io.github.solclient.client.platform.mc.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.InputStream;

@Mixin(net.minecraft.resource.Resource.class)
public abstract interface ResourceImpl extends Resource {

    @Override
    default @NotNull InputStream getInput() {
        return getInputStream();
    }

    @Shadow
    public abstract InputStream getInputStream();
}
