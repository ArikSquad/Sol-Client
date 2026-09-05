package io.github.solclient.client.platform.mc.shader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import io.github.solclient.client.platform.Helper;
import io.github.solclient.client.platform.PlatformServices;

public interface ShaderChain {

	@Helper
	static ShaderChain create(String content) throws IOException {
		return PlatformServices.get().createShaderChain(content);
	}

	static ShaderChain none() {
		return new ShaderChain() {
			@Override public void updateWindowSize(int width, int height) {}
			@Override public List<Shader> getShaders() { return Collections.emptyList(); }
		};
	}

	void updateWindowSize(int width, int height);

	List<Shader> getShaders();

}
