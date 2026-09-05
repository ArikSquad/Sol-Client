package io.github.solclient.client.platform.mc.util;

import io.github.solclient.client.platform.PlatformServices;

public class MathUtil {

	public static double cos(double a) {
		return PlatformServices.get().cos(a);
	}

	public static double sin(double a) {
		return PlatformServices.get().sin(a);
	}

}
