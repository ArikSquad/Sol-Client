package io.github.solclient.client.v1_21_11.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.client.gui.components.ChatComponent.class)
public interface ChatComponentAccessor {

	@Accessor("allMessages")
	List<net.minecraft.client.GuiMessage> solClient$allMessages();

	@Accessor("chatScrollbarPos")
	int solClient$scrollbarPosition();

}
