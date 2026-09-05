package io.github.solclient.client.v1_8_9.mixins.platform.mc.world.level.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Adds the shared BlockPos view to the live legacy position class.
 */
@Mixin(net.minecraft.util.math.BlockPos.class)
public abstract class BlockPosImpl implements io.github.solclient.client.platform.mc.world.level.block.BlockPos {

}

@Mixin(io.github.solclient.client.platform.mc.world.level.block.BlockPos.class)
interface BlockPosImpl$Static {

    @Overwrite(remap = false)
    static io.github.solclient.client.platform.mc.world.level.block.BlockPos create(int x, int y, int z) {
        return (io.github.solclient.client.platform.mc.world.level.block.BlockPos) (Object)
                new net.minecraft.util.math.BlockPos(x, y, z);
    }

}
