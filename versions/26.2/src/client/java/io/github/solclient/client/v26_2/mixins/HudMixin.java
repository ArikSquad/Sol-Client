package io.github.solclient.client.v26_2.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.hud.PreHudElementRenderEvent;
import io.github.solclient.client.event.impl.hud.PreSidebarRenderEvent;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.util.VanillaHudElement;
import io.github.solclient.client.v26_2.FabricPlatform;
import io.github.solclient.client.v26_2.GuiGraphicsBackend;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.Hud.class)
public final class HudMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "extractChat", at = @At("HEAD"), cancellable = true)
    private void solClient$chat(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo callback) {
        if (cancelled(graphics, new PreHudElementRenderEvent(VanillaHudElement.CHAT,
                delta.getGameTimeDeltaPartialTick(false)))) callback.cancel();
    }

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void solClient$crosshair(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo callback) {
        if (cancelled(graphics, new PreHudElementRenderEvent(VanillaHudElement.CROSSHAIR,
                delta.getGameTimeDeltaPartialTick(false)))) callback.cancel();
    }

    @Inject(method = "extractBossOverlay", at = @At("HEAD"), cancellable = true)
    private void solClient$bossBar(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo callback) {
        if (cancelled(graphics, new PreHudElementRenderEvent(VanillaHudElement.BOSS_BAR,
                delta.getGameTimeDeltaPartialTick(false)))) callback.cancel();
    }

    @Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void solClient$sidebar(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo callback) {
        if (minecraft.level == null) return;
        Objective objective = minecraft.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective != null && cancelled(graphics, new PreSidebarRenderEvent(VanillaHudElement.SIDEBAR,
                delta.getGameTimeDeltaPartialTick(false),
                ((FabricPlatform) PlatformServices.get()).objective(objective)))) callback.cancel();
    }

    private boolean cancelled(GuiGraphicsExtractor graphics,
                              io.github.solclient.client.event.impl.hud.PreHudElementRenderEvent event) {
        PlatformServices.enterRenderBackend(new GuiGraphicsBackend(graphics, (FabricPlatform) PlatformServices.get()));
        try {
            return EventBus.DEFAULT.post(event).isCancelled();
        } finally {
            PlatformServices.leaveRenderBackend();
        }
    }

}
