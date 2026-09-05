package io.github.solclient.client.v1_8_9.mixins.platform.mc.world.level.chunk;

import io.github.solclient.client.platform.mc.world.level.block.BlockEntity;
import io.github.solclient.client.platform.mc.world.level.block.BlockPos;
import io.github.solclient.client.platform.mc.world.level.chunk.Chunk;
import io.github.solclient.client.v1_8_9.LegacyBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(net.minecraft.world.chunk.Chunk.class)
@Implements(@Interface(iface = Chunk.class, prefix = "platform$"))
public class ChunkImpl {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NotNull Map<BlockPos, BlockEntity> platform$getBlockEntityMap() {
        Map<BlockPos, BlockEntity> result = new java.util.HashMap<>();
        for (Map.Entry<net.minecraft.util.math.BlockPos, net.minecraft.block.entity.BlockEntity> entry
                : blockEntities.entrySet()) {
            net.minecraft.util.math.BlockPos nativePos = entry.getKey();
            result.put(BlockPos.create(nativePos.getX(), nativePos.getY(), nativePos.getZ()),
                    new LegacyBlockEntity(entry.getValue()));
        }
        return result;
    }

    @Shadow
    protected @Final Map<net.minecraft.util.math.BlockPos, net.minecraft.block.entity.BlockEntity> blockEntities;

}
