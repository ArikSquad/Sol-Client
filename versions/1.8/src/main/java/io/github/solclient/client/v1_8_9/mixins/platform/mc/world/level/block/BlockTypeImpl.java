package io.github.solclient.client.v1_8_9.mixins.platform.mc.world.level.block;

import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.platform.mc.world.item.ItemType;
import io.github.solclient.client.platform.mc.world.level.block.BlockType;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds the common block identity to 1.8.9's registry objects.
 */
@Mixin(Block.class)
@Implements(@Interface(iface = BlockType.class, prefix = "platform$"))
public abstract class BlockTypeImpl {

    public Identifier platform$getId() {
        return (Identifier) (Object) Block.REGISTRY.getIdentifier((Block) (Object) this);
    }

    public ItemType platform$toItem() {
        return (ItemType) (Object) Item.fromBlock((Block) (Object) this);
    }

    public void platform$fillBox() {
        PlatformServices.get().renderBlockBox((BlockType) (Object) this, true);
    }

    public void platform$strokeBox() {
        PlatformServices.get().renderBlockBox((BlockType) (Object) this, false);
    }

}
