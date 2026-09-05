package io.github.solclient.client.platform.mc.maths;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.PlatformServices;

public interface Box {

	static @NotNull Box create(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return PlatformServices.get().box(minX, minY, minZ, maxX, maxY, maxZ);
	}

	double minX();

	double minY();

	double minZ();

	double maxX();

	double maxY();

	double maxZ();

}
