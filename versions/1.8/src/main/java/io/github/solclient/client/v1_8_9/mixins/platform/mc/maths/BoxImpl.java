package io.github.solclient.client.v1_8_9.mixins.platform.mc.maths;

import io.github.solclient.client.platform.mc.maths.Box;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Makes legacy hitboxes usable by the common render and culling code.
 */
@Mixin(net.minecraft.util.math.Box.class)
public abstract class BoxImpl implements Box {

    @Override
    public double minX() {
        return ((net.minecraft.util.math.Box) (Object) this).minX;
    }

    @Override
    public double minY() {
        return ((net.minecraft.util.math.Box) (Object) this).minY;
    }

    @Override
    public double minZ() {
        return ((net.minecraft.util.math.Box) (Object) this).minZ;
    }

    @Override
    public double maxX() {
        return ((net.minecraft.util.math.Box) (Object) this).maxX;
    }

    @Override
    public double maxY() {
        return ((net.minecraft.util.math.Box) (Object) this).maxY;
    }

    @Override
    public double maxZ() {
        return ((net.minecraft.util.math.Box) (Object) this).maxZ;
    }

}
