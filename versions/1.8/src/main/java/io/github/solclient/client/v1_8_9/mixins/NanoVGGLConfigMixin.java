package io.github.solclient.client.v1_8_9.mixins;

import io.github.solclient.client.v1_8_9.Lwjgl2FunctionProvider;
import org.lwjgl.system.FunctionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "org.lwjgl.nanovg.NanoVGGLConfig")
public class NanoVGGLConfigMixin {

    @Overwrite(remap = false)
    private static FunctionProvider getFunctionProvider(String className) {
        return new Lwjgl2FunctionProvider();
    }

}
