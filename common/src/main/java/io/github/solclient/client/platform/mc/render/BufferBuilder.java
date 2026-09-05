package io.github.solclient.client.platform.mc.render;

import io.github.solclient.client.platform.PlatformServices;

public interface BufferBuilder {

	static BufferBuilder getInstance() {
		return PlatformServices.get().bufferBuilder();
	}

	void begin(int mode, VertexFormat format);

	Vertex vertex(double x, double y, double z);

}
