package io.github.solclient.client.packet.action;

import java.util.*;

import com.google.gson.annotations.Expose;

import io.github.solclient.client.Client;
import io.github.solclient.client.mod.Mod;
import io.github.solclient.client.packet.*;

public final class BlockModsAction implements ApiAction {

	@Expose
	private Map<String, Boolean> mods;

	@Override
	public void exec(PacketApi api) {
		if (mods == null)
			throw new ApiUsageError("No mods provided to block");

		mods.forEach((key, value) -> {
			Mod mod = Client.INSTANCE.getModById(key);
			if (mod == null) {
				if (api.isDevMode())
					PacketApi.LOGGER.warn("Server tried to block mod with id " + key);

				return;
			}

			if (value)
				mod.block();
			else
				mod.unblock();
		});
	}

}