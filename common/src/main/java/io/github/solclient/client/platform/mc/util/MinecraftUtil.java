package io.github.solclient.client.platform.mc.util;

import org.jetbrains.annotations.*;

import io.github.solclient.client.platform.Helper;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.maths.Vec3d;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MinecraftUtil {

	public @NotNull OperatingSystem getOperatingSystem() {
		return PlatformServices.get().operatingSystem(System.getProperty("os.name", "UNKNOWN"));
	}

	public @Nullable Vec3d getCameraPos() {
		io.github.solclient.client.platform.mc.MinecraftClient client = io.github.solclient.client.platform.mc.MinecraftClient.getInstance();
		io.github.solclient.client.platform.mc.world.entity.Entity camera = client.getCameraEntity();
		return camera == null ? null : camera.getPosition();
	}

	public void copy(@Nullable String text) {
		PlatformServices.get().copyToClipboard(text == null ? "" : text);
	}

	public @NotNull String getClipboardContent() {
		return PlatformServices.get().getClipboardContent();
	}

	@Helper
	public boolean isMac() {
		return getOperatingSystem() == OperatingSystem.OSX;
	}

	public boolean isAllowedInTextBox(char character) {
		return PlatformServices.get().isAllowedInTextBox(character);
	}

	public static @NotNull String filterTextBoxInput(@NotNull String text) {
		return PlatformServices.get().filterTextBoxInput(text);
	}
}
