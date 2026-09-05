package io.github.solclient.client.v1_21_11.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.hud.PreHudElementRenderEvent;
import io.github.solclient.client.event.impl.hud.PreSidebarRenderEvent;
import io.github.solclient.client.event.impl.hud.PostHudRenderEvent;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.v1_21_11.FabricPlatform;
import io.github.solclient.client.v1_21_11.GuiGraphicsBackend;
import io.github.solclient.client.util.VanillaHudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.Gui.class)
public final class GuiMixin {

	@Shadow @Final private Minecraft minecraft;

	@Inject(method = "renderChat", at = @At("HEAD"), cancellable = true)
	private void solClient$chat(GuiGraphics graphics, DeltaTracker delta, CallbackInfo callback) {
		if (EventBus.DEFAULT.post(new PreHudElementRenderEvent(VanillaHudElement.CHAT,
				delta.getGameTimeDeltaPartialTick(false))).isCancelled()) {
			callback.cancel();
		}
	}

	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void solClient$crosshair(GuiGraphics graphics, DeltaTracker delta, CallbackInfo callback) {
		if (EventBus.DEFAULT.post(new PreHudElementRenderEvent(VanillaHudElement.CROSSHAIR,
				delta.getGameTimeDeltaPartialTick(false))).isCancelled()) {
			callback.cancel();
		}
	}

	@Inject(method = "renderBossOverlay", at = @At("HEAD"), cancellable = true)
	private void solClient$bossBar(GuiGraphics graphics, DeltaTracker delta, CallbackInfo callback) {
		if (EventBus.DEFAULT.post(new PreHudElementRenderEvent(VanillaHudElement.BOSS_BAR,
				delta.getGameTimeDeltaPartialTick(false))).isCancelled()) {
			callback.cancel();
		}
	}

	@Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
	private void solClient$sidebar(GuiGraphics graphics, DeltaTracker delta, CallbackInfo callback) {
		if (minecraft.level == null) return;
		Objective objective = minecraft.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
		if (objective != null && EventBus.DEFAULT.post(new PreSidebarRenderEvent(VanillaHudElement.SIDEBAR,
				delta.getGameTimeDeltaPartialTick(false),
				((FabricPlatform) PlatformServices.get()).objective(objective))).isCancelled()) {
			callback.cancel();
		}
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void solClient$begin(GuiGraphics graphics, DeltaTracker delta, CallbackInfo callback) {
		PlatformServices.enterRenderBackend(new GuiGraphicsBackend(graphics, (FabricPlatform) PlatformServices.get()));
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void solClient$end(GuiGraphics graphics, DeltaTracker delta, CallbackInfo callback) {
		try {
			EventBus.DEFAULT.post(new PostHudRenderEvent());
		}
		finally {
			PlatformServices.leaveRenderBackend();
		}
	}
}
