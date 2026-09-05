package io.github.solclient.client.platform.mc.world.level;

import io.github.solclient.client.platform.mc.maths.Box;
import io.github.solclient.client.platform.mc.maths.Vec3d;
import io.github.solclient.client.platform.PlatformServices;

public interface LevelRenderer {

	static void strokeBox(Box offsetBox, int red, int green, int blue, int alpha) {
		PlatformServices.get().strokeBox(offsetBox, red, green, blue, alpha);
	}

	static void strokeLine(Vec3d start, Vec3d end, int red, int green, int blue, int alpha) {
		PlatformServices.get().strokeLine(start, end, red, green, blue, alpha);
	}

	void scheduleUpdate();

}
