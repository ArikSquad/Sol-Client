package io.github.solclient.client.v1_8_9.mixins.platform.mc.world.entity;

import io.github.solclient.client.platform.mc.world.entity.item.ItemEntity;
import io.github.solclient.client.platform.mc.world.item.ItemStack;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Adds the small common item-entity contract to the legacy native entity.
 */
@Mixin(net.minecraft.entity.ItemEntity.class)
@Implements(@Interface(iface = ItemEntity.class, prefix = "platform$"))
public abstract class ItemEntityImpl {

    public ItemStack platform$getItem() {
        return (ItemStack) (Object) getItemStack();
    }

    @Shadow
    public abstract net.minecraft.item.ItemStack getItemStack();
}
