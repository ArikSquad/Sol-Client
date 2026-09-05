package io.github.solclient.client.v26_2.mixins;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.entity.render.EntityLabelRenderEvent;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.v26_2.FabricPlatform;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntRenderer.class)
public abstract class TntRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/PrimedTnt;"
            + "Lnet/minecraft/client/renderer/entity/state/TntRenderState;F)V", at = @At("RETURN"))
    private void solClient$label(PrimedTnt entity, TntRenderState state, float tickDelta, CallbackInfo callback) {
        EntityLabelRenderEvent event = EventBus.DEFAULT.post(new EntityLabelRenderEvent(
                ((FabricPlatform) PlatformServices.get()).commonEntity(entity), tickDelta));
        if (event.isCancelled()) {
            state.nameTag = null;
        } else if (event.getText() != null) {
            state.nameTag = net.minecraft.network.chat.Component.literal(event.getText().getPlain());
        }
    }
}
