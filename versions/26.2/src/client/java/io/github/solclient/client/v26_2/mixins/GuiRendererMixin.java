package io.github.solclient.client.v26_2.mixins;

import java.util.List;
import java.util.function.Supplier;

import io.github.solclient.client.v26_2.CommonScreenAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the replaceable common UI inside 26.2's active GUI render pass.
 *
 * <p>The modern OpenGL backend unbinds the main target when a GUI pass closes.
 * A post-render hook therefore submits valid NanoVG commands into framebuffer
 * zero, which is not the texture Minecraft presents.  Rendering immediately
 * before the final pass closes gives NanoVG the same target and viewport as the
 * native GUI while keeping the common component tree completely version-free.</p>
 */
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {

	@Shadow @Final private List<?> draws;

	@Inject(method = "executeDrawRange", at = @At(value = "INVOKE",
			target = "Lcom/mojang/blaze3d/systems/RenderPass;close()V", shift = At.Shift.BEFORE))
	private void solClient$commonUi(Supplier<String> label, RenderTarget target,
			GpuBufferSlice transforms, int startIndex, int endIndex, CallbackInfo callback) {
		// executeDrawRange is called once before the optional menu blur and once
		// after it. Submit only at the end of the final range so the common UI is
		// never blurred or rendered twice.
		if (endIndex != draws.size()) {
			return;
		}

		net.minecraft.client.gui.screens.Screen screen = Minecraft.getInstance().gui.screen();
		if (screen instanceof CommonScreenAdapter) {
			((CommonScreenAdapter) screen).renderWithUiRenderer();
		}
	}
}
