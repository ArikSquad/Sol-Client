package io.github.solclient.client.v26_2.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.game.PreRenderEvent;
import io.github.solclient.client.event.impl.world.FovEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void solClient$preRender(DeltaTracker delta, boolean renderLevel, CallbackInfo callback) {
        EventBus.DEFAULT.post(new PreRenderEvent());
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void solClient$fov(Camera camera, float tickDelta, boolean useFovSetting,
                               CallbackInfoReturnable<Float> callback) {
        callback.setReturnValue((float) EventBus.DEFAULT.post(
                new FovEvent(callback.getReturnValueF(), tickDelta)).getFov());
    }

}
