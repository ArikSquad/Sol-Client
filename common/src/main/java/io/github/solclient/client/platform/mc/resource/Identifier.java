package io.github.solclient.client.platform.mc.resource;

import io.github.solclient.client.platform.Helper;
import io.github.solclient.client.platform.PlatformServices;

public interface Identifier {

	static Identifier parse(String path) {
		return PlatformServices.get().parseIdentifier(path);
	}

	static Identifier create(String namespace, String path) {
		return PlatformServices.get().createIdentifier(namespace, path);
	}

	static Identifier minecraft(String path) {
		return create("minecraft", path);
	}

	@Helper
	static Identifier solClient(String path) {
		return create("sol_client", path);
	}

	@Helper
	static Identifier replayMod(String path) {
		return create("replaymod", path);
	}

	String namespace();

	String path();

}
