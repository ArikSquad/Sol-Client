package io.github.solclient.client.platform.mc.maths;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.PlatformServices;

public interface Vec3d {

	static @NotNull Vec3d create(double x, double y, double z) {
		return PlatformServices.get().vec3d(x, y, z);
	}

	double x();

	double y();

	double z();

	double distanceSquared(Vec3d vec);
}
