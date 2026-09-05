package io.github.solclient.client.mod.impl;

import com.google.gson.annotations.Expose;

import io.github.solclient.client.Constants;
import io.github.solclient.client.event.EventHandler;
import io.github.solclient.client.event.impl.game.PostTickEvent;
import io.github.solclient.client.mod.Mod;
import io.github.solclient.client.mod.ModCategory;
import io.github.solclient.client.mod.annotation.Option;
import io.github.solclient.client.mod.annotation.Slider;
import io.github.solclient.client.platform.mc.option.KeyBinding;
import io.github.solclient.client.platform.mc.text.Text;
import io.github.solclient.client.platform.mc.text.TextColour;
import io.github.solclient.client.platform.mc.util.Input;

/**
 * Sends one of three configured chat messages from a key binding.
 *
 * <p>The old implementation depended on the legacy keyboard event API. The
 * common implementation polls the same {@link KeyBinding} abstraction used by
 * the rest of the client, which keeps the feature identical on LWJGL 2 and
 * GLFW targets.</p>
 */
public final class ChatHotkeysMod extends Mod {

	public static final ChatHotkeysMod INSTANCE = new ChatHotkeysMod();

	@Expose
	@Option
	@Slider(min = 0, max = 2, step = 0.1F, format = "sol_client.slider.seconds")
	private float cooldown = 0.4F;

	@Option
	private KeyBinding hotkey1 = createHotkey("1");
	@Expose
	@Option
	private String message1 = "";
	@Option
	private KeyBinding hotkey2 = createHotkey("2");
	@Expose
	@Option
	private String message2 = "";
	@Option
	private KeyBinding hotkey3 = createHotkey("3");
	@Expose
	@Option
	private String message3 = "";

	private long lastSend;

	private static KeyBinding createHotkey(String number) {
		return KeyBinding.create("sol_client.mod.chat_hotkeys.hotkey_" + number,
				Input.UNKNOWN, Constants.KEY_CATEGORY);
	}

	@Override
	public String getId() {
		return "chat_hotkeys";
	}

	@Override
	public ModCategory getCategory() {
		return ModCategory.UTILITY;
	}

	@Override
	public void onRegister() {
		mc.getOptions().addKey(hotkey1);
		mc.getOptions().addKey(hotkey2);
		mc.getOptions().addKey(hotkey3);
		super.onRegister();
	}

	@EventHandler
	public void onTick(PostTickEvent event) {
		long now = System.currentTimeMillis();
		if(triggered(hotkey1, message1, now)
				|| triggered(hotkey2, message2, now)
				|| triggered(hotkey3, message3, now)) {
			lastSend = now;
		}
	}

	private boolean triggered(KeyBinding key, String message, long now) {
		if(!key.consumePress()) {
			return false;
		}

		if(!mc.hasPlayer()) {
			return true;
		}

		if(now - lastSend < cooldown * 1000F) {
			mc.getPlayer().sendSystemMessage(Text.translation("sol_client.mod.chat_hotkeys.cooldown")
					.style(style -> style.withColour(TextColour.RED)));
			return true;
		}

		String value = message == null ? "" : message.trim();
		if(value.isEmpty()) {
			mc.getPlayer().sendSystemMessage(Text.translation("sol_client.mod.chat_hotkeys.empty")
					.style(style -> style.withColour(TextColour.RED)));
			return true;
		}

		mc.getPlayer().chat(value);
		return true;
	}

}
