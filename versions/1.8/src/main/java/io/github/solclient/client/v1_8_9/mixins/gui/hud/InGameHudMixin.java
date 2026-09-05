package io.github.solclient.client.v1_8_9.mixins.gui.hud;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.hud.PostHudRenderEvent;
import io.github.solclient.client.event.impl.hud.PreHudElementRenderEvent;
import io.github.solclient.client.event.impl.hud.PreSidebarRenderEvent;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.util.VanillaHudElement;
import io.github.solclient.client.v1_8_9.LegacyScoreboardAdapters;
import io.github.solclient.common.ClientEvents;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.Window;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderScoreboardObjective", at = @At("HEAD"), cancellable = true)
    private void preSidebar(ScoreboardObjective objective, Window window, CallbackInfo callback) {
        if (objective != null && EventBus.DEFAULT.post(new PreSidebarRenderEvent(VanillaHudElement.SIDEBAR,
                ((io.github.solclient.client.platform.mc.MinecraftClient) PlatformServices.get().minecraftClient())
                        .getTimer().getTickDelta(), LegacyScoreboardAdapters.objective(objective))).isCancelled()) {
            callback.cancel();
        }
    }

    @Inject(method = "renderBossBar", at = @At("HEAD"), cancellable = true)
    private void solClient$bossBar(CallbackInfo callback) {
        if (EventBus.DEFAULT.post(new PreHudElementRenderEvent(VanillaHudElement.BOSS_BAR,
                ((io.github.solclient.client.platform.mc.MinecraftClient) PlatformServices.get().minecraftClient())
                        .getTimer().getTickDelta())).isCancelled()) {
            callback.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void postRender(CallbackInfo callback) {
        EventBus.DEFAULT.post(new PostHudRenderEvent());
    }

    @Inject(method = "setOverlayMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"), cancellable = true)
    private void solClient$actionBar(Text message, boolean tinted, CallbackInfo callback) {
        if (!ClientEvents.allowIncomingGame((io.github.solclient.client.platform.mc.text.Text) (Object) message, true)) {
            callback.cancel();
        }
    }

}
