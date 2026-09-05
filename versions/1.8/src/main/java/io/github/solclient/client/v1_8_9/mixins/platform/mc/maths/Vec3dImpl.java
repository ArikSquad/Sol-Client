package io.github.solclient.client.v1_8_9.mixins.platform.mc.maths;

import io.github.solclient.client.platform.mc.maths.Vec3d;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Makes legacy vectors usable by the common math facade.
 */
@Mixin(net.minecraft.util.math.Vec3d.class)
public abstract class Vec3dImpl implements Vec3d {

    @Override
    public double x() {
        return ((net.minecraft.util.math.Vec3d) (Object) this).x;
    }

    @Override
    public double y() {
        return ((net.minecraft.util.math.Vec3d) (Object) this).y;
    }

    @Override
    public double z() {
        return ((net.minecraft.util.math.Vec3d) (Object) this).z;
    }

    @Override
    public double distanceSquared(Vec3d other) {
        net.minecraft.util.math.Vec3d value = (net.minecraft.util.math.Vec3d) (Object) this;
        return value.squaredDistanceTo((net.minecraft.util.math.Vec3d) (Object) other);
    }

}
