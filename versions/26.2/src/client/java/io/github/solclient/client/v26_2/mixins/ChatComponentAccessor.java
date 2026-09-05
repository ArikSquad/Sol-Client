package io.github.solclient.client.v26_2.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.client.gui.components.ChatComponent.class)
public interface ChatComponentAccessor {

	@Accessor("allMessages")
	List<net.minecraft.client.multiplayer.chat.GuiMessage> solClient$allMessages();

	@Accessor("chatScrollbarPos")
	int solClient$scrollbarPosition();

}
