package io.github.solclient.client.v1_8_9.mixins.platform.mc.world.entity;

import io.github.solclient.client.platform.mc.maths.Box;
import io.github.solclient.client.platform.mc.maths.Vec3d;
import io.github.solclient.client.platform.mc.world.entity.Entity;
import io.github.solclient.client.platform.mc.world.entity.player.Player;
import io.github.solclient.client.platform.mc.world.level.chunk.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(net.minecraft.entity.Entity.class)
@Implements(@Interface(iface = Entity.class, prefix = "platform$"))
public abstract class EntityImpl {

    public double platform$x() {
        return x;
    }

    @Shadow
    public double x;

    public double platform$y() {
        return y;
    }

    @Shadow
    public double y;

    public double platform$z() {
        return z;
    }

    @Shadow
    public double z;

    public double platform$previousX() {
        return prevX;
    }

    @Shadow
    public double prevX;

    public double platform$previousY() {
        return prevY;
    }

    @Shadow
    public double prevY;

    public double platform$previousZ() {
        return prevZ;
    }

    @Shadow
    public double prevZ;

    public float platform$yaw() {
        return yaw;
    }

    @Shadow
    public float yaw;

    public float platform$pitch() {
        return pitch;
    }

    @Shadow
    public float pitch;

    public Vec3d platform$getPosition() {
        return (Vec3d) getPos();
    }

    @Shadow
    public abstract net.minecraft.util.math.Vec3d getPos();

    public int platform$getNumericId() {
        return getEntityId();
    }

    @Shadow
    public abstract int getEntityId();

    public UUID platform$getId() {
        return getUuid();
    }

    @Shadow
    public abstract UUID getUuid();

    public Box platform$getBounds() {
        return (Box) (Object) getBox();
    }

    @Shadow
    public abstract net.minecraft.util.math.Box getBox();

    public boolean platform$isGloballyInvisible() {
        return isInvisible();
    }

    @Shadow
    public abstract boolean isInvisible();

    public boolean platform$isInvisibleTo(@NotNull Player player) {
        return isInvisibleTo((net.minecraft.entity.player.PlayerEntity) (Object) player);
    }

    @Shadow
    public abstract boolean isInvisibleTo(net.minecraft.entity.player.PlayerEntity player);

    public Vec3d platform$getEyePosition() {
        return (Vec3d) (Object) getCameraPosVec(0);
    }

    @Shadow
    public abstract net.minecraft.util.math.Vec3d getCameraPosVec(float tickDelta);

    public Vec3d platform$getEyePosition(float tickDelta) {
        return (Vec3d) (Object) getCameraPosVec(tickDelta);
    }

    public float platform$getEntityWidth() {
        return width;
    }

    @Shadow
    public float width;

    public float platform$getEntityEyeHeight() {
        return getEyeHeight();
    }

    @Shadow
    public abstract float getEyeHeight();

    public Vec3d platform$getView(float tickDelta) {
        return (Vec3d) (Object) getRotationVector(tickDelta);
    }

    @Shadow
    public abstract net.minecraft.util.math.Vec3d getRotationVector(float tickDelta);

    public int platform$getChunkX() {
        return chunkX;
    }

    @Shadow
    public int chunkX;

    public int platform$getChunkZ() {
        return chunkZ;
    }

    @Shadow
    public int chunkZ;

    public ChunkPos platform$getChunkPos() {
        return ChunkPos.create(chunkX, chunkZ);
    }

    public boolean platform$isPassenger() {
        return hasVehicle();
    }

    @Shadow
    public abstract boolean hasVehicle();

    public boolean platform$isEntityOnGround() {
        return onGround;
    }

    @Shadow
    public boolean onGround;

    public boolean platform$isInWeb() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean platform$isInWater() {
        return isTouchingWater();
    }

    @Shadow
    public abstract boolean isTouchingWater();

    public boolean platform$isEntitySprinting() {
        return isSprinting();
    }

    @Shadow
    public abstract boolean isSprinting();

    public float platform$getFallDistance() {
        return fallDistance;
    }

    @Shadow
    public float fallDistance;

}
