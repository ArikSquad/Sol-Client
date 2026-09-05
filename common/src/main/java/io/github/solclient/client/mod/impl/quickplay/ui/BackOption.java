package io.github.solclient.client.mod.impl.quickplay.ui;

import io.github.solclient.client.mod.impl.quickplay.QuickPlayMod;
import io.github.solclient.client.platform.mc.lang.I18n;
import io.github.solclient.client.platform.mc.world.item.*;

public final class BackOption implements QuickPlayOption {

	@Override
	public String getText() {
		return I18n.translate("sol_client.quickplay.back");
	}

	@Override
	public void onClick(QuickPlayPalette palette, QuickPlayMod mod) {
		palette.back();
	}

	@Override
	public ItemStack getIcon() {
		return ItemStack.create(ItemType.ARROW);
	}

}
