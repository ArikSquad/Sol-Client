package io.github.solclient.client.v1_21_11.mixins;

import java.util.List;
import java.util.function.Supplier;

import io.github.solclient.client.v1_21_11.CommonScreenAdapter;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {

	@Shadow @Final private List<?> draws;

	@Inject(method = "executeDrawRange", at = @At(value = "INVOKE",
			target = "Lcom/mojang/blaze3d/systems/RenderPass;close()V", shift = At.Shift.BEFORE))
	private void solClient$commonUi(Supplier<String> label, RenderTarget target,
			GpuBufferSlice fog, GpuBufferSlice dynamicTransforms, GpuBuffer indexBuffer,
			VertexFormat.IndexType indexType, int startIndex, int endIndex, CallbackInfo callback) {
		// The draw method can execute one range before blur and one after it.
		// Submit only after the final range so the common UI is neither blurred
		// nor rendered twice.
		if (endIndex != draws.size()) {
			return;
		}

		net.minecraft.client.gui.screens.Screen screen = Minecraft.getInstance().screen;
		if (screen instanceof CommonScreenAdapter) {
			((CommonScreenAdapter) screen).renderWithUiRenderer();
		}
	}
}
