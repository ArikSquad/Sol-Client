package io.github.solclient.client.platform.mc.model;

import io.github.solclient.client.platform.PlatformServices;

public interface BakedModel {

	static BakedModel create(boolean gui3d) {
		return PlatformServices.get().bakedModel(gui3d);
	}

	boolean isGui3d();

}
