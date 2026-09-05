package io.github.solclient.client.v1_8_9.mixins.render.entity;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.entity.render.HitOverlayRenderEvent;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the common hit-colour event to the legacy hurt overlay.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class HitOverlayRenderMixin<T extends LivingEntity> {

    private static float solClient$red = 1.0F;
    private static float solClient$green;
    private static float solClient$blue;
    private static float solClient$alpha = 0.3F;

    @Inject(method = "method_10252", at = @At("HEAD"))
    private void solClient$begin(T entity, float tickDelta, boolean combineTextures,
                                 CallbackInfoReturnable<Boolean> callback) {
        HitOverlayRenderEvent event = EventBus.DEFAULT.post(new HitOverlayRenderEvent());
        solClient$red = event.getR();
        solClient$green = event.getG();
        solClient$blue = event.getB();
        solClient$alpha = event.getA();
    }

    @ModifyConstant(method = "method_10252", constant = @Constant(floatValue = 1.0F, ordinal = 0))
    private float solClient$red(float original) {
        return solClient$red;
    }

    @ModifyConstant(method = "method_10252", constant = @Constant(floatValue = 0.0F, ordinal = 0))
    private float solClient$green(float original) {
        return solClient$green;
    }

    @ModifyConstant(method = "method_10252", constant = @Constant(floatValue = 0.0F, ordinal = 1))
    private float solClient$blue(float original) {
        return solClient$blue;
    }

    @ModifyConstant(method = "method_10252", constant = @Constant(floatValue = 0.3F, ordinal = 0))
    private float solClient$alpha(float original) {
        return solClient$alpha;
    }
}
