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

import lombok.Getter;

import java.util.Objects;

@Getter
public final class GameVersion {

	public enum MappingStrategy {
		LEGACY_INTERMEDIARY,
		OFFICIAL_MOJANG
	}

	private final String id;
	private final String minecraftVersion;
	private final String loader;
	private final int javaVersion;
	private final MappingStrategy mappingStrategy;

	public GameVersion(String id, String minecraftVersion, String loader, int javaVersion,
			MappingStrategy mappingStrategy) {
		this.id = Objects.requireNonNull(id, "id");
		this.minecraftVersion = Objects.requireNonNull(minecraftVersion, "minecraftVersion");
		this.loader = Objects.requireNonNull(loader, "loader");
		this.mappingStrategy = Objects.requireNonNull(mappingStrategy, "mappingStrategy");
		if (javaVersion < 8) {
			throw new IllegalArgumentException("Minecraft targets cannot require a Java version below 8");
		}
		this.javaVersion = javaVersion;
	}

    @Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof GameVersion)) {
			return false;
		}
		GameVersion other = (GameVersion) object;
		return id.equals(other.id) && minecraftVersion.equals(other.minecraftVersion)
				&& loader.equals(other.loader) && javaVersion == other.javaVersion
				&& mappingStrategy == other.mappingStrategy;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, minecraftVersion, loader, javaVersion, mappingStrategy);
	}

	@Override
	public String toString() {
		return id + " (Minecraft " + minecraftVersion + ", " + loader + ", Java " + javaVersion + ")";
	}
}
