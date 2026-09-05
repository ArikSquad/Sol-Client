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

import io.github.solclient.client.culling.Cullable;
import io.github.solclient.client.platform.mc.world.level.block.BlockEntity;
import io.github.solclient.client.platform.mc.world.level.block.BlockType;

public final class LegacyBlockEntity implements BlockEntity, Cullable {

    private final net.minecraft.block.entity.BlockEntity entity;
    private boolean culled;

    public LegacyBlockEntity(net.minecraft.block.entity.BlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public BlockType getBlockType() {
        return (BlockType) (Object) entity.getBlock();
    }

    @Override
    public boolean isCulled() {
        return entity instanceof Cullable ? ((Cullable) entity).isCulled() : culled;
    }

    @Override
    public void setCulled(boolean value) {
        culled = value;
        if (entity instanceof Cullable) {
            ((Cullable) entity).setCulled(value);
        }
    }
}
