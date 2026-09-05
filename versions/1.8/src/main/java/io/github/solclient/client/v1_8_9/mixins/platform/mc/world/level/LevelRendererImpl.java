package io.github.solclient.client.v1_8_9.mixins.platform.mc.world.level;

import io.github.solclient.client.platform.mc.world.level.LevelRenderer;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldRenderer.class)
@Implements(@Interface(iface = LevelRenderer.class, prefix = "platform$"))
public abstract class LevelRendererImpl {

    public void platform$scheduleUpdate() {
        scheduleTerrainUpdate();
    }

    @Shadow
    public abstract void scheduleTerrainUpdate();

}

