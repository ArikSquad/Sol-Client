package io.github.solclient.client.v1_8_9;

import io.github.solclient.client.platform.mc.world.level.block.BlockPos;
import io.github.solclient.client.platform.mc.world.level.chunk.CompiledChunk;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Common view of a Legacy Fabric 1.8.9 built chunk.
 */
public final class LegacyCompiledChunk implements CompiledChunk {

    private static final Map<net.minecraft.client.world.BuiltChunk, LegacyCompiledChunk> INSTANCES =
            new WeakHashMap<>();

    private final WeakReference<net.minecraft.client.world.BuiltChunk> nativeChunk;
    private BlockPos position;

    private LegacyCompiledChunk(net.minecraft.client.world.BuiltChunk chunk) {
        this.nativeChunk = new WeakReference<>(chunk);
    }

    public static synchronized LegacyCompiledChunk get(net.minecraft.client.world.BuiltChunk chunk) {
        LegacyCompiledChunk result = INSTANCES.get(chunk);
        if (result == null) {
            result = new LegacyCompiledChunk(chunk);
            INSTANCES.put(chunk, result);
        }
        return result;
    }

    public synchronized boolean updatePosition(net.minecraft.util.math.BlockPos next) {
        BlockPos common = BlockPos.create(next.getX(), next.getY(), next.getZ());
        if (position != null && position.x() == common.x() && position.y() == common.y() && position.z() == common.z()) {
            return false;
        }
        position = common;
        return true;
    }

    public net.minecraft.client.world.BuiltChunk nativeChunk() {
        return nativeChunk.get();
    }

    @Override
    public synchronized BlockPos getPos() {
        return position;
    }

}
