package io.github.solclient.client.v26_2;

import java.io.File;

import io.github.solclient.client.platform.mc.MinecraftClient;
import io.github.solclient.client.platform.mc.MouseHandler;
import io.github.solclient.client.platform.mc.Timer;
import io.github.solclient.client.platform.mc.hud.IngameHud;
import io.github.solclient.client.platform.mc.lang.LanguageManager;
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
import io.github.solclient.client.platform.mc.network.LocalPlayerState;
import io.github.solclient.client.platform.mc.network.ServerData;
import net.minecraft.client.Minecraft;

final class MinecraftClientAdapter implements MinecraftClient {

	private final FabricPlatform platform;
	private final Minecraft minecraft;
	private final NativeWindow window;
	private final NativeFont font;
	private final NativeOptions options;
	private final NativeTextureManager textures;
	private final NativeResourceManager resources;
	private final NativeLanguageManager languages;
	private final NativeLevelRenderer levelRenderer;
	private final NativeItemRenderer itemRenderer;
	private final NativeHud hud;
	private final NativeTimer timer;
	private final NativeEntityRenderDispatcher entityRenderDispatcher;
	private final NativeParticleEngine particleEngine;
	private final NativeMouseHandler mouseHandler;
	private final NativeSoundEngine soundEngine;

	MinecraftClientAdapter(FabricPlatform platform, Minecraft minecraft) {
		this.platform = platform;
		this.minecraft = minecraft;
		this.window = new NativeWindow(minecraft.getWindow());
		this.font = new NativeFont(minecraft.font);
		this.options = new NativeOptions(minecraft.options, minecraft);
		this.textures = new NativeTextureManager(minecraft.getTextureManager(), platform);
		this.resources = new NativeResourceManager(minecraft.getResourceManager());
		this.languages = new NativeLanguageManager(minecraft.getLanguageManager());
		this.levelRenderer = new NativeLevelRenderer(minecraft);
		this.itemRenderer = new NativeItemRenderer();
		this.hud = new NativeHud(minecraft.gui, platform);
		this.timer = new NativeTimer(minecraft);
		this.entityRenderDispatcher = new NativeEntityRenderDispatcher();
		this.particleEngine = new NativeParticleEngine(minecraft);
		this.mouseHandler = new NativeMouseHandler(minecraft);
		this.soundEngine = new NativeSoundEngine(minecraft);
	}

	NativeFont nativeFont() { return font; }
	net.minecraft.world.entity.player.Player nativePlayer() { return minecraft.player; }

	@Override public io.github.solclient.client.platform.mc.Window getWindow() { return window; }
	@Override public ClientLevel getLevel() {
		return minecraft.level == null ? null : new NativeClientLevel(minecraft.level, platform);
	}
	@Override public LevelRenderer getLevelRenderer() { return levelRenderer; }
	@Override public LocalPlayer getPlayer() {
		return minecraft.player == null ? null : (LocalPlayer) NativeEntity.wrap(minecraft.player, platform);
	}
	@Override public LocalPlayerState getPlayerState() {
		return minecraft.player == null ? null : new NativePlayerState(minecraft.player.gameMode());
	}
	@Override public Entity getCameraEntity() { return NativeEntity.wrap(minecraft.getCameraEntity(), platform); }
	@Override public Options getOptions() { return options; }
	@Override public File getDataFolder() { return minecraft.gameDirectory; }
	@Override public File getPackFolder() { return new File(minecraft.gameDirectory, "resourcepacks"); }
	@Override public Font getFont() { return font; }

	@Override
	public Screen getScreen() {
		net.minecraft.client.gui.screens.Screen screen = minecraft.gui.screen();
		if (screen instanceof CommonScreenAdapter) {
			return ((CommonScreenAdapter) screen).delegate();
		}
		if (screen instanceof net.minecraft.client.gui.screens.TitleScreen) {
			return new NativeTitleScreen(screen);
		}
		return screen == null ? null : new NativeScreenHandle(screen);
	}

	@Override
	public void setScreen(Screen screen) {
		if (screen == null) {
			minecraft.gui.setScreen(null);
		}
		else {
			net.minecraft.client.gui.screens.Screen nativeScreen = CommonScreenAdapter.unwrap(screen);
			if (nativeScreen == null) {
				nativeScreen = new CommonScreenAdapter(screen, minecraft, platform);
			}
			minecraft.gui.setScreen(nativeScreen);
		}
	}

	@Override public boolean isInMenu() { return minecraft.gui.screen() != null; }
	@Override public boolean isGamePaused() { return minecraft.isPaused(); }
	@Override public void runSync(Runnable runnable) { minecraft.execute(runnable); }
	@Override public void runSyncLater(Runnable runnable, int ticks) { minecraft.execute(runnable); }
	@Override public TextureManager getTextureManager() { return textures; }
	@Override public ResourceManager getResourceManager() { return resources; }
	@Override public LanguageManager getLanguageManager() { return languages; }
	@Override public boolean hasSingleplayerServer() { return minecraft.hasSingleplayerServer(); }
	@Override public ServerData getCurrentServer() {
		return minecraft.getCurrentServer() == null ? null : new NativeServerData(minecraft.getCurrentServer());
	}
	@Override public ItemRenderer getItemRenderer() { return itemRenderer; }
	@Override public IngameHud getIngameHud() { return hud; }
	@Override public boolean isGameRunning() { return minecraft.isRunning(); }
	@Override public HitResult getHitResult() { return new NativeHitResult(minecraft.hitResult, platform); }
	@Override public Timer getTimer() { return timer; }
	@Override public EntityRenderDispatcher getEntityRenderDispatcher() { return entityRenderDispatcher; }
	@Override public ParticleEngine getParticleEngine() { return particleEngine; }
	@Override public MouseHandler getMouseHandler() { return mouseHandler; }
	@Override public boolean isFullscreen() { return minecraft.getWindow().isFullscreen(); }
	@Override public void toggleFullscreenState() { minecraft.getWindow().toggleFullScreen(); }
	@Override public SoundEngine getSoundEngine() { return soundEngine; }

	@Override
	public TitleScreen getTitleScreen() {
		net.minecraft.client.gui.screens.Screen screen = minecraft.gui.screen();
		return screen instanceof net.minecraft.client.gui.screens.TitleScreen
				? new NativeTitleScreen(screen) : new NativeTitleScreen(platform.titleScreen());
	}

	@Override public void quit() { minecraft.close(); }
}
