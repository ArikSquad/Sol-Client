package io.github.solclient.client.v1_8_9.mixins;

import org.lwjgl.opengl.GLContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accesses the native function lookup owned by LWJGL 2.
 */
@Mixin(GLContext.class)
public interface GLContextAccessor {

    @Invoker("getFunctionAddress")
    static long getFunctionAddress(String name) {
        throw new UnsupportedOperationException();
    }

    @Invoker("ngetFunctionAddress")
    static long ngetFunctionAddress(long name) {
        throw new UnsupportedOperationException();
    }

}
