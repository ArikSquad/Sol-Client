/*
 * Sol Client - an open source Minecraft client
 * Copyright (C) 2021-2026 TheKodeToad and Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package io.github.solclient.common;

public final class GameVersions {

	public static final GameVersion LEGACY_1_8 = new GameVersion(
			"1.8", "1.8.9", "legacy-fabric", 8, GameVersion.MappingStrategy.LEGACY_INTERMEDIARY);

	public static final GameVersion FABRIC_1_21_11 = new GameVersion(
			"1.21.11", "1.21.11", "fabric", 21, GameVersion.MappingStrategy.OFFICIAL_MOJANG);

	public static final GameVersion FABRIC_26_2 = new GameVersion(
			"26.2", "26.2", "fabric", 25, GameVersion.MappingStrategy.OFFICIAL_MOJANG);

	private GameVersions() {
	}
}
