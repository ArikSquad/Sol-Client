package io.github.solclient.client.v1_8_9.mixins.platform.mc;

import io.github.solclient.client.platform.mc.Timer;
import net.minecraft.client.render.ClientTickTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientTickTracker.class)
public class TimerImpl implements Timer {

    @Override
    public float getTickDelta() {
        return tickDelta;
    }

    @Shadow
    public float tickDelta;

}
