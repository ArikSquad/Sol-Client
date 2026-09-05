package io.github.solclient.client.v1_8_9.mixins.render;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.level.chunk.CompiledChunkPositionEvent;
import io.github.solclient.client.event.impl.world.level.chunk.PreRenderChunkEvent;
import io.github.solclient.client.platform.mc.render.GlStateManager;
import io.github.solclient.client.v1_8_9.LegacyCompiledChunk;
import net.minecraft.client.render.world.AbstractChunkRenderManager;
import net.minecraft.client.world.BuiltChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bridges the old immediate-mode chunk pass into the common chunk events.
 */
@Mixin(AbstractChunkRenderManager.class)
public abstract class ChunkRenderMixin {

    @Inject(method = "method_9770", at = @At("RETURN"))
    private void solClient$chunk(BuiltChunk nativeChunk, CallbackInfo callback) {
        LegacyCompiledChunk chunk = LegacyCompiledChunk.get(nativeChunk);
        if (chunk.updatePosition(nativeChunk.getPos())) {
            EventBus.DEFAULT.post(new CompiledChunkPositionEvent(chunk));
        }
        PreRenderChunkEvent event = EventBus.DEFAULT.post(new PreRenderChunkEvent(chunk));
        if (event.getOffsetY() != 0.0F) {
            GlStateManager.translate(0.0F, event.getOffsetY(), 0.0F);
        }
    }

}
