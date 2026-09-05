package io.github.solclient.client.platform.mc.maths;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.PlatformServices;

public interface Vec3i {

	static @NotNull Vec3i create(int x, int y, int z) {
		return PlatformServices.get().vec3i(x, y, z);
	}

	int x();

	int y();

	int z();

	double distanceSquared(double x, double y, double z);

	double distanceSquared(@NotNull Vec3i other);

}
