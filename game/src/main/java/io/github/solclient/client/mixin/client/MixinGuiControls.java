package io.github.solclient.client.mixin.client;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.solclient.client.util.data.Modifier;
import io.github.solclient.client.util.extension.KeyBindingExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.settings.*;

@Mixin(GuiControls.class)
public class MixinGuiControls extends GuiScreen {

	@Override
	public void handleKeyboardInput() throws IOException {
		if (!Keyboard.getEventKeyState())
			keyRelease(Keyboard.getEventCharacter(), Keyboard.getEventKey());

		super.handleKeyboardInput();
	}

	@Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
	public void preventInstantModifier(char typedChar, int keyCode, CallbackInfo callback) {
		if (!Modifier.isModifier(keyCode)) {
			boolean control = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
			boolean alt = Keyboard.isKeyDown(Keyboard.KEY_LMENU);
			boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

			if ((control || alt || shift) && keyCode > 1 && buttonId != null) {
				int mods = 0;
				if (control)
					mods |= Modifier.CTRL;
				if (alt)
					mods |= Modifier.ALT;
				if (shift)
					mods |= Modifier.SHIFT;

				((KeyBindingExtension) buttonId).setMods(mods);

				options.setOptionKeyBinding(buttonId, keyCode);
				buttonId = null;
				time = Minecraft.getSystemTime();
				KeyBinding.resetKeyBindingArrayAndHash();
			} else {
				if (buttonId != null)
					// clear mods - none are held
					((KeyBindingExtension) buttonId).setMods(0);

				return;
			}
		}

		callback.cancel();
	}

	private void keyRelease(char character, int key) {
		if (!Modifier.isModifier(key))
			return;

		if (buttonId != null) {
			// clear mods - this is a mod key on its own
			((KeyBindingExtension) buttonId).setMods(0);
			options.setOptionKeyBinding(buttonId, key);
			buttonId = null;
			time = Minecraft.getSystemTime();
			KeyBinding.resetKeyBindingArrayAndHash();
		}
	}

	@Shadow
	private GameSettings options;

	@Shadow
	public KeyBinding buttonId;

	@Shadow
	private long time;

}