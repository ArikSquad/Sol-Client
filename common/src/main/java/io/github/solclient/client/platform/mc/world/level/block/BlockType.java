package io.github.solclient.client.platform.mc.world.level.block;

import org.jetbrains.annotations.NotNull;

import io.github.solclient.client.platform.Helper;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.platform.mc.world.item.ItemType;

public interface BlockType {

	BlockType AIR = get("AIR");
	BlockType ANVIL = get("ANVIL");
	BlockType BEACON = get("BEACON");
	BlockType CRAFTING_TABLE = get("CRAFTING_TABLE");
	BlockType DARK_OAK_DOOR = get("DARK_OAK_DOOR");
	BlockType DIRT = get("DIRT");
	BlockType GRASS_BLCOK = get("GRASS_BLOCK");
	BlockType IRON_BARS = get("IRON_BARS");
	BlockType JUKEBOX = get("JUKEBOX");
	BlockType OAK_DOOR = get("OAK_DOOR");
	BlockType SOUL_SAND = get("SOUL_SAND");
	BlockType TNT = get("TNT");

	static BlockType get(String name) {
		return PlatformServices.get().blockType(name);
	}

	@NotNull Identifier getId();

	@NotNull ItemType toItem();

	@Helper
	void fillBox();

	@Helper
	void strokeBox();

}
