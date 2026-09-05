package io.github.solclient.client.v1_8_9.mixins.platform.mc.world.entity.player;

import io.github.solclient.client.platform.mc.network.Connection;
import io.github.solclient.client.platform.mc.text.Text;
import io.github.solclient.client.platform.mc.world.entity.player.LocalPlayer;
import io.github.solclient.common.ClientEvents;
import net.minecraft.entity.player.ClientPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
@Implements(@Interface(iface = LocalPlayer.class, prefix = "platform$"))
public abstract class LocalPlayerImpl {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void solClient$outgoing(String message, CallbackInfo callback) {
        if (!ClientEvents.allowOutgoing(message)) {
            callback.cancel();
        }
    }

    public @NotNull Connection platform$getConnection() {
        return (Connection) (Object) networkHandler;
    }

    @Shadow
    public net.minecraft.client.network.ClientPlayNetworkHandler networkHandler;

    public void platform$sendSystemMessage(@NotNull String text) {
        addMessage(new net.minecraft.text.LiteralText(text));
    }

    @Shadow
    public abstract void addMessage(net.minecraft.text.Text text);

    public void platform$sendSystemMessage(@NotNull Text text) {
        addMessage((net.minecraft.text.Text) (Object) text);
    }

    public void platform$chat(@NotNull String text) {
        sendChatMessage(text);
    }

    @Shadow
    public abstract void sendChatMessage(String text);

    public boolean platform$isSpectatorMode() {
        // 1.8.9 declares this method on PlayerEntity and only inherits it from
        // ClientPlayerEntity.  Calling through the native type avoids a shadow
        // lookup that incorrectly requires it to be declared on the target.
        return ((net.minecraft.entity.player.PlayerEntity) (Object) this).isSpectator();
    }

    public void platform$clientSwing() {
        swingHand();
    }

    @Shadow
    public abstract void swingHand();

}
