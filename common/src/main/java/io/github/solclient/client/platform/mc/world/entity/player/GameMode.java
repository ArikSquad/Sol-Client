package io.github.solclient.client.platform.mc.world.entity.player;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.VirtualEnum;
import io.github.solclient.client.platform.PlatformServices;

public interface GameMode extends VirtualEnum {

	GameMode SURVIVAL = get("SURVIVAL", 0);
	GameMode CREATIVE = get("CREATIVE", 1);
	GameMode ADVENTURE = get("ADVENTURE", 2);
	GameMode SPECTATOR = get("SPECTATOR", 3);

	static GameMode get(String name, int numericId) {
		return PlatformServices.get().gameMode(name, numericId);
	}

	@NotNull String getId();

	int getNumericId();

}
