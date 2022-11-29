package io.github.solclient.client.mod;

import java.io.IOException;
import java.util.*;

import org.apache.logging.log4j.*;

import com.google.gson.annotations.Expose;

import io.github.solclient.client.Client;
import io.github.solclient.client.event.*;
import io.github.solclient.client.event.impl.hud.PostHudRenderEvent;
import io.github.solclient.client.mod.annotation.*;
import io.github.solclient.client.mod.hud.HudElement;
import io.github.solclient.client.platform.mc.MinecraftClient;
import io.github.solclient.client.platform.mc.lang.I18n;
import io.github.solclient.client.ui.screen.mods.MoveHudsScreen;

@AbstractTranslationKey("sol_client.mod.generic")
public abstract class Mod {

	protected final MinecraftClient mc = MinecraftClient.getInstance();
	private List<ModOption> options;
	private boolean blocked;
	@Expose
	@Option(priority = 2)
	private boolean enabled = isEnabledByDefault();
	protected final Logger logger = LogManager.getLogger();

	protected Mod() {}

	public void postStart() {
	}

	public void onRegister() {
		try {
			options = ModOption.get(this);
		}
		catch(IOException error) {
			throw new IllegalStateException(error);
		}

		if(enabled) {
			tryEnable();
		}
	}

	public String getTranslationKey() {
		return "sol_client.mod." + getId();
	}

	public String getName() {
		return I18n.translate(getTranslationKey() + ".name");
	}

	public abstract String getId();

	public String getDescription() {
		return I18n.translate("sol_client.mod." + getId() + ".description");
	}

	public abstract ModCategory getCategory();

	public boolean isEnabledByDefault() {
		return false;
	}

	public boolean onOptionChange(String key, Object value) {
		if(key.equals("enabled")) {
			if(isLocked()) {
				return false;
			}
			setEnabled((boolean) value);
		}
		return true;
	}

	public void postOptionChange(String key, Object value) {
	}

	public List<ModOption> getOptions() {
		return options;
	}

	public void setEnabled(boolean enabled) {
		if(blocked) return;
		if(isLocked()) return;

		if(enabled != this.enabled) {
			if(enabled) {
				tryEnable();
			}
			else {
				try {
					onDisable();
				}
				catch(Throwable error) {
					logger.error("Error while disabling mod", error);
				}
			}
		}
		this.enabled = enabled;
	}

	private void tryEnable() {
		try {
			onEnable();
		}
		catch(Throwable error) {
			logger.error("Could not enable mod", error);
			setEnabled(false);
		}
	}

	protected void onEnable() {
		EventBus.DEFAULT.register(this);
	}

	protected void onDisable() {
		EventBus.DEFAULT.unregister(this);
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void block() {
		if(enabled && !blocked) {
			onDisable();
		}
		blocked = true;
	}

	public void unblock() {
		if(enabled && blocked) {
			onEnable();
		}
		blocked = false;
	}

	public boolean isEnabled() {
		return enabled && !blocked;
	}

	public void toggle() {
		setEnabled(!isEnabled());
	}

	public void disable() {
		setEnabled(false);
	}

	public void enable() {
		setEnabled(true);
	}

	public int getIndex() {
		return Client.INSTANCE.getMods().indexOf(this);
	}

	public List<HudElement> getHudElements() {
		return Collections.emptyList();
	}

	public boolean isLocked() {
		return false;
	}

	public String getLockMessage() {
		return "";
	}

	public void onFileUpdate(String fieldName) {}

	public void render(boolean editMode) {
		for(HudElement element : getHudElements()) {
			element.render(editMode);
		}
	}

	@EventHandler
	public void onRender(PostHudRenderEvent event) {
		render(mc.getScreen() instanceof MoveHudsScreen);
	}

	@Override
	public String toString() {
		return getId();
	}

}