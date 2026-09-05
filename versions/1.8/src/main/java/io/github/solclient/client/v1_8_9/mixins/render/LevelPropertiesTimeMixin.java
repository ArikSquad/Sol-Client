package io.github.solclient.client.v1_8_9.mixins.render;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.level.DetermineTimeEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the common time override to the active legacy client world.
 */
@Mixin(LevelProperties.class)
public abstract class LevelPropertiesTimeMixin {

    @Shadow
    private long timeOfDay;

    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void solClient$time(CallbackInfoReturnable<Long> callback) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.world != null && (Object) this == minecraft.world.getLevelProperties()) {
            DetermineTimeEvent event = new DetermineTimeEvent();
            event.setTime(timeOfDay);
            callback.setReturnValue(EventBus.DEFAULT.post(event).getTime());
        }
    }
}
