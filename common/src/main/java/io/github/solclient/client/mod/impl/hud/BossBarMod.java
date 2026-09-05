package io.github.solclient.client.mod.impl.hud;

import com.google.gson.annotations.Expose;

import io.github.solclient.client.event.EventHandler;
import io.github.solclient.client.event.impl.hud.PreHudElementRenderEvent;
import io.github.solclient.client.mod.Mod;
import io.github.solclient.client.mod.ModCategory;
import io.github.solclient.client.mod.annotation.Option;
import io.github.solclient.client.util.VanillaHudElement;

/**
 * Keeps the boss-bar hook in common code.  The version modules only need to
 * call the hook before their native boss-bar renderer runs.
 */
public final class BossBarMod extends Mod {

	public static final BossBarMod INSTANCE = new BossBarMod();

	@Expose
	@Option
	private boolean hide;

	@Override
	public String getId() {
		return "boss_bar";
	}

	@Override
	public ModCategory getCategory() {
		return ModCategory.HUD;
	}

	@EventHandler
	public void onBossBarRender(PreHudElementRenderEvent event) {
		if(hide && event.getElement() == VanillaHudElement.BOSS_BAR) {
			event.setCancelled(true);
		}
	}

}
