package io.github.solclient.client.platform.mc;

import io.github.solclient.client.platform.VirtualEnum;
import io.github.solclient.client.platform.basic.BasicPlatform;

public interface Direction extends VirtualEnum {

	Direction UP = BasicPlatform.direction("UP"),
			DOWN = BasicPlatform.direction("DOWN"),
			NORTH = BasicPlatform.direction("NORTH"),
			EAST = BasicPlatform.direction("EAST"),
			SOUTH = BasicPlatform.direction("SOUTH"),
			WEST = BasicPlatform.direction("WEST");

}
