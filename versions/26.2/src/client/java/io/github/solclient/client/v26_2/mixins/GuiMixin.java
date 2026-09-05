package io.github.solclient.client.v26_2.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.hud.PostHudRenderEvent;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.v26_2.FabricPlatform;
import io.github.solclient.client.v26_2.GuiGraphicsBackend;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public final class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private GuiRenderState guiRenderState;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void solClient$render(DeltaTracker delta, boolean renderChat, boolean renderDebug, CallbackInfo callback) {
        GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(minecraft, guiRenderState,
                minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        PlatformServices.enterRenderBackend(new GuiGraphicsBackend(graphics, (FabricPlatform) PlatformServices.get()));
        try {
            EventBus.DEFAULT.post(new PostHudRenderEvent());
        } finally {
            PlatformServices.leaveRenderBackend();
        }
    }
}
