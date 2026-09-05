package io.github.solclient.client.v1_8_9.mixins;

import io.github.solclient.client.Constants;
import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.game.FullscreenToggleEvent;
import io.github.solclient.client.event.impl.input.MouseDownEvent;
import io.github.solclient.client.event.impl.input.ScrollWheelEvent;
import io.github.solclient.client.event.impl.world.level.LevelLoadEvent;
import io.github.solclient.client.platform.mc.world.level.ClientLevel;
import io.github.solclient.common.ClientBootstrap;
import io.github.solclient.common.ClientEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.world.ClientWorld;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @ModifyConstant(method = "setPixelFormat", constant = @Constant(stringValue = "Minecraft 1.8.9"))
    public String getTitle(String title) {
        return Constants.NAME + " | " + title;
    }

    @Inject(method = "initializeGame", at = @At("RETURN"))
    public void init(CallbackInfo callback) {
        ClientBootstrap.start();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void preTick(CallbackInfo callback) {
        ClientBootstrap.preTick();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void postTick(CallbackInfo callback) {
        ClientBootstrap.postTick();
    }

    @Inject(method = "toggleFullscreen", at = @At("HEAD"), cancellable = true)
    private void solClient$toggleFullscreen(CallbackInfo callbackInfo) {
        FullscreenToggleEvent event = ClientEvents.fullscreen(!options.fullscreen);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        } else if (!event.getApplyState()) {
            options.fullscreen = event.getState();
            callbackInfo.cancel();
        }
    }

    @Inject(method = "connect(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V", at = @At("HEAD"))
    private void loadWorld(ClientWorld world, String loadingMessage, CallbackInfo callback) {
        EventBus.DEFAULT.post(new LevelLoadEvent((ClientLevel) world));
    }

    @Inject(method = "getMaxFramerate", at = @At("HEAD"), cancellable = true)
    public void getMaxFramerate(CallbackInfoReturnable<Integer> callback) {
        if (currentScreen instanceof Screen && world == null) {
            // limit to 60 instead of 30
            // it should be perfectly acceptable
            callback.setReturnValue(60);
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z"))
    public boolean mouseEvent() {
        boolean next = Mouse.next();

        if (next && Mouse.getEventButtonState()) {
            if (EventBus.DEFAULT.post(new MouseDownEvent(Mouse.getEventButton())).isCancelled()) {
                next = mouseEvent(); // Skip
            }
        } else if (next && Mouse.getEventDWheel() != 0) {
            int amount = Mouse.getEventDWheel() > 0 ? 1 : -1;
            if (EventBus.DEFAULT.post(new ScrollWheelEvent(amount)).isCancelled()) {
                next = mouseEvent(); // Skip
            }
        }

        return next;
    }


    @Shadow
    public Screen currentScreen;

    @Shadow
    public ClientWorld world;

    @Shadow
    public GameOptions options;

}
