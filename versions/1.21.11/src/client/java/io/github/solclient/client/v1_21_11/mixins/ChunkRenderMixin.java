package io.github.solclient.client.v1_21_11.mixins;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.solclient.client.event.EventBus;
import io.github.solclient.client.event.impl.world.level.chunk.CompiledChunkPositionEvent;
import io.github.solclient.client.event.impl.world.level.chunk.PreRenderChunkEvent;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;

@Mixin(LevelRenderer.class)
public abstract class ChunkRenderMixin {

	@Unique
	private static final ThreadLocal<Float> SOL_CLIENT_CHUNK_OFFSET = new ThreadLocal<>();

	@WrapOperation(method = "prepareChunkRenders", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;getRenderOrigin()Lnet/minecraft/core/BlockPos;"))
	private BlockPos solClient$chunkPosition(SectionRenderDispatcher.RenderSection section,
		Operation<BlockPos> original) {
		BlockPos position = original.call(section);
		io.github.solclient.client.v1_21_11.NativeCompiledChunk chunk =
			io.github.solclient.client.v1_21_11.NativeCompiledChunk.get(section, position);
		if (chunk.updatePosition(position)) {
			EventBus.DEFAULT.post(new CompiledChunkPositionEvent(chunk));
		}
		PreRenderChunkEvent event = EventBus.DEFAULT.post(new PreRenderChunkEvent(chunk));
		SOL_CLIENT_CHUNK_OFFSET.set(event.getOffsetY());
		return position;
	}

	@ModifyArg(method = "prepareChunkRenders", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/DynamicUniforms$ChunkSectionInfo;<init>(Lorg/joml/Matrix4fc;IIIFII)V"),
		index = 0)
	private Matrix4fc solClient$chunkTransform(Matrix4fc modelView) {
		Float offset = SOL_CLIENT_CHUNK_OFFSET.get();
		SOL_CLIENT_CHUNK_OFFSET.remove();
		if (offset == null || offset.floatValue() == 0.0F) {
			return modelView;
		}
		return new Matrix4f(modelView).translate(0.0F, offset.floatValue(), 0.0F);
	}

}
