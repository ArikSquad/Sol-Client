package io.github.solclient.client.v1_8_9.mixins.render.entity;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.entity.render.ItemEntityRenderEvent;
import io.github.solclient.client.platform.mc.model.BakedModel;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bridges legacy immediate-mode item rendering to the common item physics event.
 */
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Inject(method = "method_10221(Lnet/minecraft/entity/ItemEntity;DDDFLnet/minecraft/client/render/model/BakedModel;)I",
            at = @At("HEAD"), cancellable = true)
    private void solClient$itemRender(net.minecraft.entity.ItemEntity entity, double x, double y, double z,
                                      float tickDelta, net.minecraft.client.render.model.BakedModel model,
                                      CallbackInfoReturnable<Integer> callback) {
        ItemEntityRenderEvent event = EventBus.DEFAULT.post(new ItemEntityRenderEvent(
                (io.github.solclient.client.platform.mc.world.entity.item.ItemEntity) (Object) entity,
                BakedModel.create(model != null && model.hasDepth()), x, y, z, tickDelta));
        if (event.isCancelled()) callback.setReturnValue(event.getReturnValue());
    }
}
