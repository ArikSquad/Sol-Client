package io.github.solclient.client.platform.mc.render;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.PlatformServices;

public interface Tessellator {

	static @NotNull Tessellator getInstance() {
		return PlatformServices.get().tessellator();
	}

	@NotNull BufferBuilder getBufferBuilder();

	void end();

}
