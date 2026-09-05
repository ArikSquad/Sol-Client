package io.github.solclient.client.v26_2.mixins;

import io.github.solclient.client.culling.Cullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(net.minecraft.world.level.block.entity.BlockEntity.class)
@Implements(@Interface(iface = Cullable.class, prefix = "solClient$"))
public abstract class CulledBlockEntityMixin {

    @Unique
    private boolean solClient$culled;

    public boolean solClient$isCulled() {
        return solClient$culled;
    }

    public void solClient$setCulled(boolean culled) {
        solClient$culled = culled;
    }
}
