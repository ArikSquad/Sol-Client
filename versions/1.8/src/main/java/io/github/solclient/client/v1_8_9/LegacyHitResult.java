/*
 * Sol Client - an open source Minecraft client
 * Copyright (C) 2021-2026 TheKodeToad and Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package io.github.solclient.client.v1_8_9;

import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.raycast.HitResult;
import io.github.solclient.client.platform.mc.world.entity.Entity;

public final class LegacyHitResult implements HitResult {

    private final net.minecraft.util.hit.BlockHitResult result;

    public LegacyHitResult(net.minecraft.util.hit.BlockHitResult result) {
        this.result = result;
    }

    @Override
    public io.github.solclient.client.platform.mc.raycast.HitType getType() {
        return PlatformServices.get().hitType(result == null ? "MISS" : result.type.name());
    }

    @Override
    public Entity getEntity() {
        return result == null || result.entity == null ? null : (Entity) (Object) result.entity;
    }

    @Override
    public io.github.solclient.client.platform.mc.world.level.block.BlockPos getBlockPos() {
        return result == null || result.type != net.minecraft.util.hit.BlockHitResult.Type.BLOCK
                ? null : (io.github.solclient.client.platform.mc.world.level.block.BlockPos) (Object) result.getBlockPos();
    }

    @Override
    public io.github.solclient.client.platform.mc.Direction getBlockSide() {
        return result == null || result.direction == null ? null
                : PlatformServices.get().direction(result.direction.name());
    }
}
