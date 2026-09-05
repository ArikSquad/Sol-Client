package io.github.solclient.client.v26_2;

import io.github.solclient.client.platform.mc.world.level.block.BlockPos;
import io.github.solclient.client.platform.mc.world.level.chunk.CompiledChunk;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public final class NativeCompiledChunk implements CompiledChunk {

    private static final Map<net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection,
            NativeCompiledChunk> INSTANCES = new WeakHashMap<>();

    private final WeakReference<net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection> nativeSection;
    private BlockPos position;

    private NativeCompiledChunk(net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection section,
                                net.minecraft.core.BlockPos position) {
        this.nativeSection = new WeakReference<>(section);
    }

    public static synchronized NativeCompiledChunk get(
            net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection section,
            net.minecraft.core.BlockPos position) {
        NativeCompiledChunk result = INSTANCES.get(section);
        if (result == null) {
            result = new NativeCompiledChunk(section, position);
            INSTANCES.put(section, result);
        }
        return result;
    }

    private static BlockPos commonPosition(net.minecraft.core.BlockPos position) {
        return BlockPos.create(position.getX(), position.getY(), position.getZ());
    }

    public synchronized boolean updatePosition(net.minecraft.core.BlockPos next) {
        BlockPos common = commonPosition(next);
        if (position != null && position.x() == common.x() && position.y() == common.y() && position.z() == common.z()) {
            return false;
        }
        position = common;
        return true;
    }

    public net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection nativeSection() {
        return nativeSection.get();
    }

    @Override
    public synchronized BlockPos getPos() {
        return position;
    }

}
