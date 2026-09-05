package io.github.solclient.client.v1_8_9.mixins.platform.mc;

import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.mc.MinecraftClient;
import io.github.solclient.client.platform.mc.MouseHandler;
import io.github.solclient.client.platform.mc.Timer;
import io.github.solclient.client.platform.mc.Window;
import io.github.solclient.client.platform.mc.hud.IngameHud;
import io.github.solclient.client.platform.mc.lang.LanguageManager;
import io.github.solclient.client.platform.mc.network.LocalPlayerState;
import io.github.solclient.client.platform.mc.network.ServerData;
import io.github.solclient.client.platform.mc.option.Options;
import io.github.solclient.client.platform.mc.raycast.HitResult;
import io.github.solclient.client.platform.mc.resource.ResourceManager;
import io.github.solclient.client.platform.mc.screen.Screen;
import io.github.solclient.client.platform.mc.screen.TitleScreen;
import io.github.solclient.client.platform.mc.sound.SoundEngine;
import io.github.solclient.client.platform.mc.text.Font;
import io.github.solclient.client.platform.mc.texture.TextureManager;
import io.github.solclient.client.platform.mc.world.entity.Entity;
import io.github.solclient.client.platform.mc.world.entity.EntityRenderDispatcher;
import io.github.solclient.client.platform.mc.world.entity.player.LocalPlayer;
import io.github.solclient.client.platform.mc.world.item.ItemRenderer;
import io.github.solclient.client.platform.mc.world.level.ClientLevel;
import io.github.solclient.client.platform.mc.world.level.LevelRenderer;
import io.github.solclient.client.platform.mc.world.particle.ParticleEngine;
import io.github.solclient.client.v1_8_9.LegacyHitResult;
import io.github.solclient.client.v1_8_9.LegacyNativeScreenHandle;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.ClientTickTracker;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.resource.ReloadableResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(net.minecraft.client.MinecraftClient.class)
@Implements(@Interface(iface = MinecraftClient.class, prefix = "platform$"))
public abstract class MinecraftClientImpl {

    public @NotNull Window platform$getWindow() {
        return (Window) window;
    }

    @Inject(method = {"onResolutionChanged", "initializeGame"}, at = @At("RETURN"))
    public void updateWindow(CallbackInfo callback) {
        window = new net.minecraft.client.util.Window((net.minecraft.client.MinecraftClient) (Object) this);
    }

    private net.minecraft.client.util.Window window;

    public @Nullable ClientLevel platform$getLevel() {
        return (ClientLevel) world;
    }

    @Shadow
    public ClientWorld world;

    public @NotNull LevelRenderer platform$getLevelRenderer() {
        return (LevelRenderer) worldRenderer;
    }

    @Shadow
    public WorldRenderer worldRenderer;

    public @Nullable LocalPlayer platform$getPlayer() {
        return (LocalPlayer) player;
    }

    @Shadow
    public ClientPlayerEntity player;

    public @Nullable LocalPlayerState platform$getPlayerState() {
        return interactionManager == null || interactionManager.getCurrentGameMode() == null ? null
                : new LegacyPlayerState(interactionManager.getCurrentGameMode());
    }

    @Shadow
    public ClientPlayerInteractionManager interactionManager;

    public @Nullable Entity platform$getCameraEntity() {
        Entity result = (Entity) getCameraEntity();

        if (result == null) {
            return platform$getPlayer();
        }

        return result;
    }

    @Shadow
    public abstract net.minecraft.entity.Entity getCameraEntity();

    public @NotNull Options platform$getOptions() {
        return (Options) options;
    }

    @Shadow
    public @Final GameOptions options;

    public @NotNull File platform$getDataFolder() {
        return runDirectory;
    }

    @Shadow
    public @Final File runDirectory;

    public @NotNull File platform$getPackFolder() {
        return new File(runDirectory, "resourcepacks");
    }

    public @NotNull Font platform$getFont() {
        return (Font) textRenderer;
    }

    @Shadow
    public TextRenderer textRenderer;

    public @Nullable Screen platform$getScreen() {
        return (Screen) currentScreen;
    }

    @Shadow
    public net.minecraft.client.gui.screen.Screen currentScreen;

    public void platform$setScreen(@Nullable Screen screen) {
        openScreen(LegacyNativeScreenHandle.unwrap(screen));
    }

    @Shadow
    public abstract void openScreen(net.minecraft.client.gui.screen.Screen screen);

    public boolean platform$isInMenu() {
        return currentScreen != null;
    }

    public boolean platform$isGamePaused() {
        return isPaused();
    }

    @Shadow
    public abstract boolean isPaused();

    public void platform$runSync(@NotNull Runnable runnable) {
        if (isOnThread()) runnable.run();
        else submit(runnable);
    }

    public void platform$runSyncLater(@NotNull Runnable runnable, int ticks) {
        // The old client has no public delayed executor. Queueing through its
        // thread executor preserves thread safety; common callers only require
        // ordering after the current callback.
        platform$runSync(runnable);
    }

    @Shadow
    public abstract boolean isOnThread();

    @Shadow
    public abstract com.google.common.util.concurrent.ListenableFuture<Object> submit(Runnable runnable);

    public @NotNull TextureManager platform$getTextureManager() {
        return (TextureManager) textureManager;
    }

    @Shadow
    private net.minecraft.client.texture.TextureManager textureManager;

    public @NotNull LanguageManager platform$getLanguageManager() {
        return (LanguageManager) languageManager;
    }

    @Shadow
    private net.minecraft.client.resource.language.LanguageManager languageManager;

    public @NotNull ResourceManager platform$getResourceManager() {
        return (ResourceManager) resourceManager;
    }

    @Shadow
    private @Final ReloadableResourceManager resourceManager;

    public boolean platform$hasSingleplayerServer() {
        return isIntegratedServerRunning();
    }

    @Shadow
    public abstract boolean isIntegratedServerRunning();

    public @Nullable ServerData platform$getCurrentServer() {
        net.minecraft.client.network.ServerInfo server = getCurrentServerEntry();
        return server == null ? null : new LegacyServerData(server);
    }

    @Shadow
    public abstract net.minecraft.client.network.ServerInfo getCurrentServerEntry();

    public @NotNull ItemRenderer platform$getItemRenderer() {
        return (ItemRenderer) getItemRenderer();
    }

    @Shadow
    public abstract net.minecraft.client.render.item.ItemRenderer getItemRenderer();

    public @NotNull IngameHud platform$getIngameHud() {
        return (IngameHud) inGameHud;
    }

    @Shadow
    public InGameHud inGameHud;

    public boolean platform$isGameRunning() {
        return running;
    }

    @Shadow
    public volatile boolean running;

    public @NotNull HitResult platform$getHitResult() {
        return new LegacyHitResult(result);
    }

    @Shadow
    public net.minecraft.util.hit.BlockHitResult result;

    public @NotNull Timer platform$getTimer() {
        return (Timer) tricker;
    }

    // :P tongue twister
    @Shadow
    private ClientTickTracker tricker;

    public @NotNull EntityRenderDispatcher platform$getEntityRenderDispatcher() {
        return (EntityRenderDispatcher) (Object) getEntityRenderManager();
    }

    @Shadow
    public abstract net.minecraft.client.render.entity.EntityRenderDispatcher getEntityRenderManager();

    public @NotNull ParticleEngine platform$getParticleEngine() {
        return new LegacyParticleEngine(particleManager);
    }

    @Shadow
    public net.minecraft.client.particle.ParticleManager particleManager;

    public @NotNull MouseHandler platform$getMouseHandler() {
        return new LegacyMouseHandler(mouse);
    }

    @Shadow
    public net.minecraft.client.MouseInput mouse;

    public boolean platform$isFullscreen() {
        return options.fullscreen;
    }

    public void platform$toggleFullscreenState() {
        toggleFullscreen();
    }

    @Shadow
    public abstract void toggleFullscreen();

    public @NotNull SoundEngine platform$getSoundEngine() {
        return (SoundEngine) getSoundManager();
    }

    @Shadow
    public abstract SoundManager getSoundManager();

    public @Nullable TitleScreen platform$getTitleScreen() {
        if (titleScreen == null) {
            titleScreen = (TitleScreen) (Object) new net.minecraft.client.gui.screen.TitleScreen();
        }
        return titleScreen;
    }

    private TitleScreen titleScreen;

    @Inject(method = "openScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"))
    public void setTitleScreen(net.minecraft.client.gui.screen.Screen screen, CallbackInfo callback) {
        if (screen instanceof net.minecraft.client.gui.screen.TitleScreen) {
            titleScreen = (TitleScreen) screen;
        }
    }

    public void platform$quit() {
        scheduleStop();
    }

    @Shadow
    public abstract void scheduleStop();

}

final class LegacyPlayerState implements LocalPlayerState {
    private final net.minecraft.world.level.LevelInfo.GameMode gameMode;

    LegacyPlayerState(net.minecraft.world.level.LevelInfo.GameMode gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public io.github.solclient.client.platform.mc.world.entity.player.GameMode getGameMode() {
        return PlatformServices.get().gameMode(gameMode.getName().toUpperCase(), gameMode.getId());
    }
}

final class LegacyServerData implements ServerData {
    private final net.minecraft.client.network.ServerInfo server;

    LegacyServerData(net.minecraft.client.network.ServerInfo server) {
        this.server = server;
    }

    @Override
    public String getName() {
        return server.name;
    }

    @Override
    public String getIp() {
        return server.address;
    }

    @Override
    public java.util.concurrent.CompletableFuture<Integer> ping() {
        return java.util.concurrent.CompletableFuture.completedFuture((int) server.ping);
    }
}

final class LegacyParticleEngine implements ParticleEngine {
    private final net.minecraft.client.particle.ParticleManager particles;

    LegacyParticleEngine(net.minecraft.client.particle.ParticleManager particles) {
        this.particles = particles;
    }

    @Override
    public void emit(Entity entity, io.github.solclient.client.platform.mc.world.particle.ParticleType particle) {
        if (particles == null || entity == null) return;
        net.minecraft.client.particle.ParticleType nativeParticle;
        switch (String.valueOf(particle).toUpperCase()) {
            case "MAGIC":
                nativeParticle = net.minecraft.client.particle.ParticleType.CRIT_MAGIC;
                break;
            case "SNOWBALL":
                nativeParticle = net.minecraft.client.particle.ParticleType.SNOWBALL;
                break;
            case "SLIME":
                nativeParticle = net.minecraft.client.particle.ParticleType.SLIME;
                break;
            case "FLAME":
                nativeParticle = net.minecraft.client.particle.ParticleType.FIRE;
                break;
            default:
                nativeParticle = net.minecraft.client.particle.ParticleType.CRIT;
                break;
        }
        particles.addEmitter((net.minecraft.entity.Entity) (Object) entity, nativeParticle);
    }

    @Override
    public void emitDestruction(io.github.solclient.client.platform.mc.world.level.block.BlockPos blockPos,
                                io.github.solclient.client.platform.mc.Direction side) {
        if (particles == null || blockPos == null) return;
        net.minecraft.util.math.Direction direction = side == null ? net.minecraft.util.math.Direction.UP
                : net.minecraft.util.math.Direction.valueOf(String.valueOf(side).toUpperCase());
        particles.addBlockBreakingParticles((net.minecraft.util.math.BlockPos) (Object) blockPos, direction);
    }
}

final class LegacyMouseHandler implements MouseHandler {
    private final net.minecraft.client.MouseInput mouse;

    LegacyMouseHandler(net.minecraft.client.MouseInput mouse) {
        this.mouse = mouse;
    }

    @Override
    public void grabCursor() {
        if (mouse != null) mouse.grabMouse();
    }
}

@Mixin(MinecraftClient.class)
interface MinecraftClientImpl$Static {

    @Overwrite(remap = false)
    static MinecraftClient getInstance() {
        return (MinecraftClient) net.minecraft.client.MinecraftClient.getInstance();
    }

    @Overwrite(remap = false)
    static int getFps() {
        return net.minecraft.client.MinecraftClient.getCurrentFps();
    }

}
