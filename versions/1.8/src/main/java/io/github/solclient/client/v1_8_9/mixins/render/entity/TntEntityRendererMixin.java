package io.github.solclient.client.v1_8_9.mixins.render.entity;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.entity.render.EntityLabelRenderEvent;
import io.github.solclient.client.platform.mc.text.Text;
import io.github.solclient.client.platform.mc.world.entity.Entity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Feeds TNT labels through the common entity-label event.
 */
@Mixin(TntEntityRenderer.class)
public abstract class TntEntityRendererMixin extends EntityRenderer<TntEntity> {

    protected TntEntityRendererMixin(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Inject(method = "render(Lnet/minecraft/entity/TntEntity;DDDFF)V", at = @At("RETURN"))
    private void solClient$label(TntEntity entity, double x, double y, double z, float yaw, float tickDelta,
                                 CallbackInfo callback) {
        EntityLabelRenderEvent event = EventBus.DEFAULT.post(new EntityLabelRenderEvent(
                (Entity) (Object) entity, tickDelta));
        Text text = event.getText();
        if (!event.isCancelled() && text != null) {
            renderLabelIfPresent(entity, text.getPlain(), x, y, z, 64);
        }
    }
}
