package io.github.solclient.client.v1_8_9.mixins.gui.hud;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.hud.PreHudElementRenderEvent;
import io.github.solclient.client.util.VanillaHudElement;
import io.github.solclient.common.ClientEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void preRender(int ticks, CallbackInfo callback) {
        if (EventBus.DEFAULT
                .post(new PreHudElementRenderEvent(VanillaHudElement.CHAT,
                        ((io.github.solclient.client.platform.mc.MinecraftClient) client).getTimer().getTickDelta()))
                .isCancelled()) {
            callback.cancel();
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void solClient$incoming(Text message, CallbackInfo callback) {
        if (!ClientEvents.allowIncomingChat((io.github.solclient.client.platform.mc.text.Text) (Object) message)) {
            callback.cancel();
        }
    }

    @Shadow
    private @Final MinecraftClient client;

}
