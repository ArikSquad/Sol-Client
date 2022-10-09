package io.github.solclient.client.v1_8_9.mixins.platform.mc.world.entity.effect;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import io.github.solclient.client.platform.mc.world.entity.effect.StatusEffectType;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.entity.effect.StatusEffect;

@Mixin(StatusEffect.class)
@Implements(@Interface(iface = StatusEffectType.class, prefix = "platform$"))
public abstract class StatusEffectTypeImpl {

	public void platform$render(int x, int y) {
		DrawableHelper.drawTexture(x, y, getAtlasU(), getAtlasV(), 8, 8, 8, 8);
	}

	private float getAtlasU() {
		return method_2444() % 8 * 18;
	}

	private float getAtlasV() {
		return 198 + method_2444() / 8 * 18;
	}

	@Shadow
	public abstract int method_2444();

	public @NotNull String platform$getName() {
		return getTranslationKey();
	}

	@Shadow
	public abstract String getTranslationKey();

}

@Mixin(StatusEffectType.class)
interface StatusEffectTypeImpl$Static {

	@Overwrite(remap = false)
	static StatusEffectType get(String name) {
		switch(name) {
			case "SPEED":
				return (StatusEffectType) StatusEffect.SPEED;
			case "STRENGTH":
				return (StatusEffectType) StatusEffect.STRENGTH;
			case "BLINDNESS":
				return (StatusEffectType) StatusEffect.BLINDNESS;
		}

		throw new IllegalArgumentException(name);
	}

}