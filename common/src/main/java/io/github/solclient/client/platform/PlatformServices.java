package io.github.solclient.client.platform;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import io.github.solclient.client.platform.mc.MinecraftClient;
import io.github.solclient.client.platform.mc.Window;
import io.github.solclient.client.platform.mc.lang.LanguageManager;
import io.github.solclient.client.platform.mc.maths.*;
import io.github.solclient.client.platform.mc.network.C2SChatMessagePacket;
import io.github.solclient.client.platform.mc.option.*;
import io.github.solclient.client.platform.mc.render.*;
import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.platform.mc.screen.*;
import io.github.solclient.client.platform.mc.shader.ShaderChain;
import io.github.solclient.client.platform.mc.sound.*;
import io.github.solclient.client.platform.mc.text.*;
import io.github.solclient.client.platform.mc.texture.Texture;
import io.github.solclient.client.platform.mc.util.OperatingSystem;
import io.github.solclient.client.platform.mc.world.entity.effect.*;
import io.github.solclient.client.platform.mc.world.item.*;
import io.github.solclient.client.platform.mc.world.level.block.BlockType;
import io.github.solclient.client.platform.mc.world.particle.ParticleType;
import io.github.solclient.client.platform.mc.world.entity.player.GameMode;
import io.github.solclient.client.platform.mc.raycast.HitType;
import io.github.solclient.client.platform.mc.Direction;
import io.github.solclient.client.platform.basic.BasicPlatform;

/**
 * Runtime-installed services for Minecraft APIs which are represented by
 * Minecraft-free interfaces in {@code common}.
 *
 * <p>Static methods in the compatibility interfaces delegate here. A version
 * adapter installs its provider before the common client is started; the
 * default provider keeps the shared layer usable in unit tests and during
 * early loader discovery.</p>
 */
public final class PlatformServices {

	private static volatile Provider provider = new Provider() {};
	private static volatile UiRenderer uiRenderer = UiRenderer.NONE;
	private static final ThreadLocal<RenderBackend> ACTIVE_BACKEND = new ThreadLocal<>();
	private static final ThreadLocal<Boolean> UI_FRAME = new ThreadLocal<>();
	private static final ThreadLocal<RenderState> RENDER_STATE = new ThreadLocal<RenderState>() {
		@Override
		protected RenderState initialValue() {
			return new RenderState();
		}
	};

	private PlatformServices() {}

	public static synchronized void install(Provider next) {
		UiRenderer previousRenderer = uiRenderer;
		provider = next == null ? new Provider() {} : next;
		UiRenderer nextRenderer = provider.uiRenderer();
		uiRenderer = nextRenderer == null ? UiRenderer.NONE : nextRenderer;
		if (previousRenderer != null && previousRenderer != UiRenderer.NONE
				&& previousRenderer != uiRenderer) {
			previousRenderer.close();
		}
	}

	public static Provider get() {
		return provider;
	}

	public static void enterRenderBackend(RenderBackend backend) {
		ACTIVE_BACKEND.set(backend == null ? RenderBackend.NONE : backend);
	}

	public static void leaveRenderBackend() {
		ACTIVE_BACKEND.remove();
	}

	public static RenderBackend renderBackend() {
		RenderBackend active = ACTIVE_BACKEND.get();
		return active == null ? provider.renderBackend() : active;
	}

	public static UiRenderer uiRenderer() {
		return uiRenderer;
	}

	public static synchronized void closeUiRenderer() {
		UiRenderer renderer = uiRenderer;
		uiRenderer = UiRenderer.NONE;
		if (renderer != null && renderer != UiRenderer.NONE) {
			renderer.close();
		}
	}

	public static boolean isUiFrameActive() {
		return Boolean.TRUE.equals(UI_FRAME.get());
	}

	public static void enterUiFrame() {
		UI_FRAME.set(Boolean.TRUE);
	}

	public static void leaveUiFrame() {
		UI_FRAME.remove();
	}

	/**
	 * The old client expressed world colours and line widths through global GL
	 * state.  Keep that tiny bit of state at the common boundary so modern
	 * renderers, which submit immutable render commands, can consume it too.
	 */
	public static void rememberColour(float red, float green, float blue, float alpha) {
		RenderState state = RENDER_STATE.get();
		state.colour = ((channel(alpha) << 24) | (channel(red) << 16)
				| (channel(green) << 8) | channel(blue));
	}

	public static int currentColour() {
		return RENDER_STATE.get().colour;
	}

	public static void rememberLineWidth(float width) {
		RENDER_STATE.get().lineWidth = width;
	}

	public static float currentLineWidth() {
		return RENDER_STATE.get().lineWidth;
	}

	private static int channel(float value) {
		return Math.max(0, Math.min(255, Math.round(value * 255.0F)));
	}

	private static final class RenderState {
		private int colour = 0xFFFFFFFF;
		private float lineWidth = 2.0F;
	}

	public interface Provider {

		default MinecraftClient minecraftClient() { return BasicPlatform.client(); }
		default int fps() { return 0; }
		default Window window() { return minecraftClient().getWindow(); }

		default MutableText literal(String text) { return BasicPlatform.literal(text); }
		default MutableText format(String format, Object... args) { return literal(String.format(format, args)); }
		default MutableText translation(String key, Object... args) { return literal(translate(key, args)); }
		default String translate(String key, Object... args) { return BasicPlatform.translate(key, args); }
		default Optional<String> translateOptional(String key, Object... args) {
			return Optional.of(translate(key, args));
		}

		default Identifier parseIdentifier(String path) { return BasicPlatform.identifier(path); }
		default Identifier createIdentifier(String namespace, String path) {
			return BasicPlatform.identifier(namespace, path);
		}

		default KeyBinding createKeyBinding(String name, int initialKey, String category) {
			return BasicPlatform.keyBinding(name, initialKey, category);
		}
		default void reloadKeyBindings() {}
		default ToggleKeyBinding createToggleKeyBinding(String name, int initialKey, String category,
				BooleanSupplier toggleMode, int maxHoldTime) {
			return BasicPlatform.toggleKeyBinding(name, initialKey, category, toggleMode, maxHoldTime);
		}

		default OptionsScreen createOptionsScreen(Screen parent, Options options) {
			return BasicPlatform.optionsScreen(parent);
		}
		default SingleplayerScreen createSingleplayerScreen(Screen parent) {
			return BasicPlatform.singleplayerScreen(parent);
		}
		default MultiplayerScreen createMultiplayerScreen(Screen parent) {
			return BasicPlatform.multiplayerScreen(parent);
		}
		default LanguageScreen createLanguageScreen(Screen parent, Options options, LanguageManager languages) {
			return BasicPlatform.languageScreen(parent);
		}

		default TextFormatting textFormatting(String name) { return BasicPlatform.textFormatting(name); }
		default String stripFormatting(String message) { return BasicPlatform.stripFormatting(message); }
		default ClickEvent createClickEvent(ClickEvent.Action action, String value) {
			return BasicPlatform.clickEvent(action, value);
		}
		default ClickEvent.Action clickAction(String name) { return BasicPlatform.clickAction(name); }

		default OperatingSystem operatingSystem(String name) { return BasicPlatform.operatingSystem(name); }
		default Object environment(String key) { return BasicPlatform.environment(key); }

		default Vec3i vec3i(int x, int y, int z) { return BasicPlatform.vec3i(x, y, z); }
		default Vec3d vec3d(double x, double y, double z) { return BasicPlatform.vec3d(x, y, z); }
		default Box box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
			return BasicPlatform.box(minX, minY, minZ, maxX, maxY, maxZ);
		}
		default BlockPosFactory blockPos() { return BasicPlatform::blockPos; }
		default ChunkPosFactory chunkPos() { return BasicPlatform::chunkPos; }
		default BlockType blockType(String name) { return BasicPlatform.blockType(name); }
		default HitType hitType(String name) { return BasicPlatform.hitType(name); }
		default GameMode gameMode(String name, int numericId) { return BasicPlatform.gameMode(name, numericId); }
		default ParticleType particleType(String name) { return BasicPlatform.particleType(name); }
		default Direction direction(String name) { return BasicPlatform.direction(name); }

		default ItemType itemType(String name) { return BasicPlatform.itemType(name); }
		default ItemStack itemStack(ItemType type, int quantity) { return BasicPlatform.itemStack(type, quantity); }
		default ItemStack blockItemStack(io.github.solclient.client.platform.mc.world.level.block.BlockType type, int quantity) {
			return BasicPlatform.blockItemStack(type, quantity);
		}
		default io.github.solclient.client.platform.mc.model.BakedModel bakedModel(boolean gui3d) {
			return BasicPlatform.bakedModel(gui3d);
		}
		default StatusEffectType statusEffectType(String name) { return BasicPlatform.statusEffectType(name); }
		default StatusEffect statusEffect(StatusEffectType type, int duration, int amplifier) {
			return BasicPlatform.statusEffect(type, duration, amplifier);
		}

		default SoundType soundType(String name) { return BasicPlatform.soundType(name); }
		default SoundInstance uiSound(SoundType sound, float pitch) { return BasicPlatform.uiSound(sound, pitch); }
		default Texture texture(String name) { return BasicPlatform.texture(name); }
		default Identifier textureIdentifier(String name) { return BasicPlatform.textureIdentifier(name); }
		default C2SChatMessagePacket chatPacket(String message) { return BasicPlatform.chatPacket(message); }
		default BufferBuilder bufferBuilder() { return BasicPlatform.bufferBuilder(); }
		default Tessellator tessellator() { return BasicPlatform.tessellator(); }
		default void strokeBox(Box box, int red, int green, int blue, int alpha) {}
		default void strokeLine(Vec3d start, Vec3d end, int red, int green, int blue, int alpha) {}
		default void setBorderlessFullscreen(boolean state) {}
		default void renderBlockBox(BlockType block, boolean fill) {}

		default RenderBackend renderBackend() { return RenderBackend.NONE; }
		default UiRenderer uiRenderer() { return new NanoVGUiRenderer(); }
		default ShaderChain createShaderChain(String content) throws java.io.IOException {
			return ShaderChain.none();
		}
		default boolean isKeyDown(int code) { return false; }
		default boolean isMouseButtonDown(int button) { return false; }
		default void enableRepeatEvents(boolean value) {}
		default void copyToClipboard(String text) {}
		default String getClipboardContent() { return ""; }
		default boolean isAllowedInTextBox(char character) { return character >= 32 && character != 127; }
		default String filterTextBoxInput(String text) { return text; }
		default double cos(double value) { return Math.cos(value); }
		default double sin(double value) { return Math.sin(value); }

		default void fillRect(float left, float top, float right, float bottom, int colour) {
			PlatformServices.renderBackend().fillRect(left, top, right, bottom, colour);
		}
		default void fillRoundedRect(float left, float top, float right, float bottom, float radius, int colour) {
			PlatformServices.renderBackend().fillRoundedRect(left, top, right, bottom, radius, colour);
		}
		default void fillGradientRect(int left, int top, int right, int bottom, int topColour, int bottomColour) {
			PlatformServices.renderBackend().fillGradientRect(left, top, right, bottom, topColour, bottomColour);
		}
		default void strokeRect(int left, int top, int right, int bottom, int colour) {
			PlatformServices.renderBackend().strokeRect(left, top, right, bottom, colour);
		}
		default void fillTexturedRect(int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
			PlatformServices.renderBackend().fillTexturedRect(x, y, u, v, width, height, textureWidth, textureHeight);
		}
		default void renderHorizontalLine(int startX, int endX, int y, int colour) {
			PlatformServices.renderBackend().renderHorizontalLine(startX, endX, y, colour);
		}
		default void renderVerticalLine(int x, int startY, int endY, int colour) {
			PlatformServices.renderBackend().renderVerticalLine(x, startY, endY, colour);
		}

		default void enableBlend() { PlatformServices.renderBackend().enableBlend(); }
		default void disableBlend() { PlatformServices.renderBackend().disableBlend(); }
		default void enableDepth() { PlatformServices.renderBackend().enableDepth(); }
		default void disableDepth() { PlatformServices.renderBackend().disableDepth(); }
		default void enableCull() { PlatformServices.renderBackend().enableCull(); }
		default void disableCull() { PlatformServices.renderBackend().disableCull(); }
		default void pushMatrix() { PlatformServices.renderBackend().pushMatrix(); }
		default void popMatrix() { PlatformServices.renderBackend().popMatrix(); }
		default void disableTexture() { PlatformServices.renderBackend().disableTexture(); }
		default void enableTexture() { PlatformServices.renderBackend().enableTexture(); }
		default void depthMask(boolean mask) { PlatformServices.renderBackend().depthMask(mask); }
		default void scale(float x, float y, float z) { PlatformServices.renderBackend().scale(x, y, z); }
		default void scale(double x, double y, double z) { PlatformServices.renderBackend().scale(x, y, z); }
		default void translate(float x, float y, float z) { PlatformServices.renderBackend().translate(x, y, z); }
		default void translate(double x, double y, double z) { PlatformServices.renderBackend().translate(x, y, z); }
		default void colour(float r, float g, float b, float a) { PlatformServices.renderBackend().colour(r, g, b, a); }
		default void blendFunction(int sourceRgb, int destinationRgb, int sourceAlpha, int destinationAlpha) {
			PlatformServices.renderBackend().blendFunction(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
		}
		default void blendFunction(int source, int destination) { PlatformServices.renderBackend().blendFunction(source, destination); }
		default void lineWidth(float width) { PlatformServices.renderBackend().lineWidth(width); }
		default void resetLineWidth() { PlatformServices.renderBackend().resetLineWidth(); }
		default void rotate(float angle, float x, float y, float z) { PlatformServices.renderBackend().rotate(angle, x, y, z); }
		default void bindTexture(int id) { PlatformServices.renderBackend().bindTexture(id); }
		default void defaultBackground() { PlatformServices.renderBackend().defaultBackground(); }
		default void renderPanorama(int width, int height, float tickDelta) {
			PlatformServices.renderBackend().renderPanorama(width, height, tickDelta);
		}
	}

	@FunctionalInterface
	public interface BlockPosFactory {
		io.github.solclient.client.platform.mc.world.level.block.BlockPos create(double x, double y, double z);
	}

	@FunctionalInterface
	public interface ChunkPosFactory {
		io.github.solclient.client.platform.mc.world.level.chunk.ChunkPos create(int x, int z);
	}

}
