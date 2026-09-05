package io.github.solclient.client.v1_8_9.mixins.platform.mc.world.level.block;

import io.github.solclient.client.culling.Cullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Stores the shared culling flag on the live legacy block entity.
 */
@Mixin(net.minecraft.block.entity.BlockEntity.class)
@Implements(@Interface(iface = Cullable.class, prefix = "platform$"))
public abstract class CulledBlockEntityImpl {

    @Unique
    private boolean platform$culled;

    public boolean platform$isCulled() {
        return platform$culled;
    }

    public void platform$setCulled(boolean culled) {
        platform$culled = culled;
    }
}
