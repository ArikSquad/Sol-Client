package io.github.solclient.client.platform.mc;

import io.github.solclient.client.platform.PlatformServices;

public interface Window {

	static int displayWidth() {
		return PlatformServices.get().window().width();
	}

	static int displayHeight() {
		return PlatformServices.get().window().height();
	}

	int width();

	int height();

	int scaleFactor();

	int scaledWidth();

	int scaledHeight();

	double scaledWidthD();

	double scaledHeightD();

}
