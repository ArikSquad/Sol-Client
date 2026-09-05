package io.github.solclient.client.platform.mc.world.level.chunk;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.PlatformServices;

public interface ChunkPos {

	static @NotNull ChunkPos create(int x, int z) {
		return PlatformServices.get().chunkPos().create(x, z);
	}

	int x();

	int z();

}
