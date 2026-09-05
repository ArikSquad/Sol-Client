package io.github.solclient.client.platform.mc.raycast;

import io.github.solclient.client.platform.VirtualEnum;
import io.github.solclient.client.platform.PlatformServices;

public interface HitType extends VirtualEnum {

	HitType MISS = get("MISS"),
			ENTITY = get("ENTITY"),
			BLOCK = get("BLOCK");

	static HitType get(String name) {
		return PlatformServices.get().hitType(name);
	}

}
