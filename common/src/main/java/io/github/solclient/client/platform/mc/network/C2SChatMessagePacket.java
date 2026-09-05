package io.github.solclient.client.platform.mc.network;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.PlatformServices;

public interface C2SChatMessagePacket extends Packet {

	static @NotNull C2SChatMessagePacket create(String message) {
		return PlatformServices.get().chatPacket(message);
	}

	String message();

}
