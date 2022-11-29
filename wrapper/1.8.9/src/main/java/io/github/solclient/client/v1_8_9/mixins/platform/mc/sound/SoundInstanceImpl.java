package io.github.solclient.client.v1_8_9.mixins.platform.mc.sound;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;

import io.github.solclient.client.platform.Helper;
import io.github.solclient.client.platform.mc.sound.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.util.Identifier;

@Mixin(net.minecraft.client.sound.SoundInstance.class)
public interface SoundInstanceImpl extends SoundInstance {

}

@Mixin(SoundInstance.class)
interface SoundInstanceImpl$Static {

	@Helper
	@Overwrite(remap = false)
	static @NotNull SoundInstance ui(SoundType sound, float pitch) {
		return (SoundInstance) PositionedSoundInstance.master(
				(Identifier) ((io.github.solclient.client.v1_8_9.platform.mc.sound.SoundTypeImpl) sound).getId(),
				pitch);
	}

}