package io.github.solclient.client.v1_8_9.mixins.render;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.game.PreRenderEvent;
import io.github.solclient.client.event.impl.input.CameraRotateEvent;
import io.github.solclient.client.event.impl.world.CameraTransformEvent;
import io.github.solclient.client.event.impl.world.FovEvent;
import io.github.solclient.client.event.impl.world.GammaEvent;
import io.github.solclient.client.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    public void preRender(CallbackInfo callback) {
        EventBus.DEFAULT.post(new PreRenderEvent());
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    public void fovEvent(float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> callback) {
        callback.setReturnValue(
                (float) EventBus.DEFAULT.post(new FovEvent(callback.getReturnValueF(), tickDelta)).getFov());
    }

    @Redirect(method = "updateLightmap", at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/GameOptions;gamma:F"))
    public float modifyGamma(GameOptions options) {
        return EventBus.DEFAULT.post(new GammaEvent(options.gamma)).getGamma();
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ClientPlayerEntity;increaseTransforms(FF)V"))
    public void cameraRotateEvent(Args args) {
        float yaw = (Float) args.get(0);
        float pitch = (Float) args.get(1);
        CameraRotateEvent event = EventBus.DEFAULT.post(new CameraRotateEvent(yaw, pitch));
        if (event.isCancelled() || Utils.isSpectatingEntityInReplay()) {
            args.set(0, 0.0F);
            args.set(1, 0.0F);
        } else {
            args.set(0, event.getYaw());
            args.set(1, event.getPitch());
        }
    }

    private static float sc$yaw, sc$prevYaw, sc$pitch, sc$prevPitch;

    @Inject(method = "transformCamera", at = @At("HEAD"))
    public void rotationEvent(float partialTicks, CallbackInfo callback) {
        CameraTransformEvent event = EventBus.DEFAULT.post(new CameraTransformEvent(client.getCameraEntity().yaw, client.getCameraEntity().pitch));
        sc$yaw = event.getYaw();
        sc$pitch = event.getPitch();

        event = EventBus.DEFAULT.post(new CameraTransformEvent(client.getCameraEntity().prevYaw, client.getCameraEntity().prevPitch));
        sc$prevYaw = event.getYaw();
        sc$prevPitch = event.getPitch();
    }

    @Shadow
    private MinecraftClient client;

    @Redirect(method = "transformCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;yaw:F"))
    public float eventYaw(Entity entity) {
        return sc$yaw;
    }

    @Redirect(method = "transformCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevYaw:F"))
    public float eventPrevYaw(Entity entity) {
        return sc$prevYaw;
    }

    @Redirect(method = "transformCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;pitch:F"))
    public float eventPitch(Entity entity) {
        return sc$pitch;
    }

    @Redirect(method = "transformCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevPitch:F"))
    public float eventPrevPitch(Entity entity) {
        return sc$prevPitch;
    }

}
