package io.github.solclient.client.v1_8_9.mixins.platform.mc.world.entity;

import io.github.solclient.client.platform.mc.world.entity.LivingEntity;
import io.github.solclient.client.platform.mc.world.entity.LivingEntityType;
import io.github.solclient.client.platform.mc.world.entity.effect.StatusEffect;
import io.github.solclient.client.platform.mc.world.entity.effect.StatusEffectType;
import io.github.solclient.client.platform.mc.world.item.ItemStack;
import net.minecraft.entity.EntityGroup;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;

@Mixin(net.minecraft.entity.LivingEntity.class)
@Implements(@Interface(iface = LivingEntity.class, prefix = "platform$"))
public abstract class LivingEntityImpl {

    public Collection<StatusEffect> platform$getEntityStatusEffects() {
        return getStatusEffectInstances();
    }

    @Shadow
    public abstract Collection<StatusEffect> getStatusEffectInstances();

    public boolean platform$hasStatusEffect(StatusEffectType effect) {
        return hasStatusEffect((net.minecraft.entity.effect.StatusEffect) effect);
    }

    @Shadow
    public abstract boolean hasStatusEffect(net.minecraft.entity.effect.StatusEffect effect);

    public boolean platform$isEntityClimbing() {
        return isClimbing();
    }

    @Shadow
    public abstract boolean isClimbing();

    public ItemStack platform$getMainHandItem() {
        return (ItemStack) (Object) getStackInHand();
    }

    @Shadow
    public abstract net.minecraft.item.ItemStack getStackInHand();

    public LivingEntityType platform$getLivingEntityType() {
        return LegacyLivingEntityType.INSTANCE;
    }

    @Shadow
    public abstract EntityGroup getGroup();

}

final class LegacyLivingEntityType implements LivingEntityType {
    static final LegacyLivingEntityType INSTANCE = new LegacyLivingEntityType();

    private LegacyLivingEntityType() {
    }
}
