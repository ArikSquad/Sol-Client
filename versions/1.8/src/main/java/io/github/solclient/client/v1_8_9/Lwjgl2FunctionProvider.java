package io.github.solclient.client.v1_8_9;

import io.github.solclient.client.v1_8_9.mixins.GLContextAccessor;
import org.lwjgl.MemoryUtil;
import org.lwjgl.system.FunctionProvider;

import java.nio.ByteBuffer;

public final class Lwjgl2FunctionProvider implements FunctionProvider {
    @Override
    public long getFunctionAddress(CharSequence functionName) {
        return GLContextAccessor.getFunctionAddress(functionName.toString());
    }

    @Override
    public long getFunctionAddress(ByteBuffer functionName) {
        return GLContextAccessor.ngetFunctionAddress(MemoryUtil.getAddress(functionName));
    }

}
