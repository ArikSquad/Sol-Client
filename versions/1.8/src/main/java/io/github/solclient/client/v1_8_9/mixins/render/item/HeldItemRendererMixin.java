package io.github.solclient.client.v1_8_9.mixins.render.item;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.item.FirstPersonItemTransformEvent;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs the common first-person item transforms at the legacy GL boundary.
 */
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Shadow
    private ItemStack mainHand;

    @Inject(method = "applyEquipAndSwingOffset(FF)V", at = @At("HEAD"))
    private void solClient$transform(float equipProgress, float swingProgress, CallbackInfo callback) {
        if (mainHand == null || mainHand.isEmpty()) return;
        EventBus.DEFAULT.post(new FirstPersonItemTransformEvent(
                (io.github.solclient.client.platform.mc.world.item.ItemStack) (Object) mainHand));
    }
}
