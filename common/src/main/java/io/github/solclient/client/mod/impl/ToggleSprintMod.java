package io.github.solclient.client.mod.impl;

import com.google.gson.annotations.Expose;

import io.github.solclient.client.mod.ModCategory;
import io.github.solclient.client.mod.annotation.Option;
import io.github.solclient.client.mod.hud.SimpleHudMod;
import io.github.solclient.client.platform.mc.lang.I18n;
import io.github.solclient.client.platform.mc.option.ToggleKeyBinding;
import io.github.solclient.client.platform.mc.util.Input;

public final class ToggleSprintMod extends SimpleHudMod {

	public static final ToggleSprintMod INSTANCE = new ToggleSprintMod();

	private static final String TOGGLED = "sol_client.mod.toggle_sprint.toggled";
	private static final String HELD = "sol_client.mod.toggle_sprint.held";

	private ToggleKeyBinding key;
	private ToggleKeyBinding sneak;

	@Expose
	@Option
	private boolean hud;
	@Expose
	@Option
	private boolean toggleSprint = true;
	@Expose
	@Option
	private boolean toggleSneak;

	@Override
	public void onRegister() {
		super.onRegister();
		mc.getOptions().removeKey(mc.getOptions().sprintKey());
		key = ToggleKeyBinding.create(mc.getOptions().sprintKey().getName(), Input.LEFT_CONTROL,
				mc.getOptions().sprintKey().getKeyCategory(), () -> isEnabled() && toggleSprint, 250);
		mc.getOptions().overwriteSprintKey(key);
		mc.getOptions().addKey(key);

		io.github.solclient.client.platform.mc.option.KeyBinding originalSneak = mc.getOptions().sneakKey();
		mc.getOptions().removeKey(originalSneak);
		sneak = ToggleKeyBinding.create(originalSneak.getName(), Input.LEFT_SHIFT,
				originalSneak.getKeyCategory(), () -> isEnabled() && toggleSneak, 250);
		mc.getOptions().overwriteSneakKey(sneak);
		mc.getOptions().addKey(sneak);
	}

	@Override
	public String getId() {
		return "toggle_sprint";
	}

	@Override
	public ModCategory getCategory() {
		return ModCategory.UTILITY;
	}

	@Override
	public boolean isVisible() {
		return hud;
	}

	@Override
	public String getText(boolean editMode) {
		if(!hud) {
			return null;
		}
		if(editMode) {
			return I18n.translate(HELD);
		}
		if(sneak != null && sneak.isHeld()) {
			return sneak.realHeld() ? I18n.translate("sol_client.mod.toggle_sprint.sneak_held")
					: I18n.translate("sol_client.mod.toggle_sprint.sneak_toggled");
		}
		return key != null && key.isHeld()
				? (key.realHeld() ? I18n.translate(HELD) : I18n.translate(TOGGLED)) : null;
	}

}
