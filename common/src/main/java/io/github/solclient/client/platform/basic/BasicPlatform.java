package io.github.solclient.client.platform.basic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

import io.github.solclient.client.chat.ChatButton;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.RenderBackend;
import io.github.solclient.client.platform.VirtualEnum;
import io.github.solclient.client.platform.mc.*;
import io.github.solclient.client.platform.mc.hud.IngameHud;
import io.github.solclient.client.platform.mc.hud.chat.*;
import io.github.solclient.client.platform.mc.lang.LanguageManager;
import io.github.solclient.client.platform.mc.maths.*;
import io.github.solclient.client.platform.mc.network.*;
import io.github.solclient.client.platform.mc.option.*;
import io.github.solclient.client.platform.mc.raycast.*;
import io.github.solclient.client.platform.mc.resource.*;
import io.github.solclient.client.platform.mc.screen.*;
import io.github.solclient.client.platform.mc.sound.*;
import io.github.solclient.client.platform.mc.text.*;
import io.github.solclient.client.platform.mc.texture.*;
import io.github.solclient.client.platform.mc.util.OperatingSystem;
import io.github.solclient.client.platform.mc.world.entity.*;
import io.github.solclient.client.platform.mc.world.entity.effect.*;
import io.github.solclient.client.platform.mc.world.entity.player.*;
import io.github.solclient.client.platform.mc.world.item.*;
import io.github.solclient.client.platform.mc.world.level.*;
import io.github.solclient.client.platform.mc.world.level.block.*;
import io.github.solclient.client.platform.mc.world.level.chunk.*;
import io.github.solclient.client.platform.mc.world.particle.*;
import io.github.solclient.client.platform.mc.render.*;

/**
 * Safe Minecraft-free implementations used before a version adapter installs
 * its native provider. They are also useful for common-layer tests.
 */
public class BasicPlatform {

	private static final BasicClient CLIENT = new BasicClient();
	private static final BasicWindow WINDOW = new BasicWindow();
	private static final BasicOptions OPTIONS = new BasicOptions();
	private static final BasicFont FONT = new BasicFont();
	private static final BasicTextureManager TEXTURES = new BasicTextureManager();
	private static final BasicResourceManager RESOURCES = new BasicResourceManager();
	private static final BasicLanguageManager LANGUAGES = new BasicLanguageManager();
	private static final BasicSoundEngine SOUNDS = new BasicSoundEngine();
	private static final BasicTimer TIMER = new BasicTimer();
	private static final BasicLevelRenderer LEVEL_RENDERER = new BasicLevelRenderer();
	private static final BasicItemRenderer ITEMS = new BasicItemRenderer();
	private static final BasicHud HUD = new BasicHud();
	private static final BasicHitResult HIT_RESULT = new BasicHitResult();
	private static final BasicEntityRenderDispatcher ENTITIES = new BasicEntityRenderDispatcher();
	private static final BasicParticleEngine PARTICLES = new BasicParticleEngine();
	private static final BasicMouseHandler MOUSE = new BasicMouseHandler();
	private static final BasicBufferBuilder BUFFER = new BasicBufferBuilder();
	private static final BasicTessellator TESSELLATOR = new BasicTessellator(BUFFER);
	private static final Map<String, BasicTextFormatting> FORMATTING = new HashMap<>();
	private static final Map<String, BasicItemType> ITEMS_BY_NAME = new HashMap<>();
	private static final Map<String, BasicStatusEffectType> EFFECTS = new HashMap<>();

	public static MinecraftClient client() {
		return CLIENT;
	}

	public static Window window() {
		return WINDOW;
	}

	public static Options options() {
		return OPTIONS;
	}

	public static Font font() {
		return FONT;
	}

	public static MutableText literal(String text) {
		return new BasicText(text == null ? "" : text);
	}

	public static String translate(String key, Object... args) {
		String value = key;
		try {
			Class<?> provider = Class.forName("io.github.solclient.client.util.TranslationProvider");
			value = (String) provider.getMethod("translate", String.class).invoke(null, key);
		}
		catch (ReflectiveOperationException ignored) {
			// The common runtime is still usable without its optional translation loader.
		}
		return args.length == 0 ? value : String.format(value, args);
	}

	public static Identifier identifier(String path) {
		int separator = path.indexOf(':');
		return separator < 0 ? identifier("minecraft", path)
				: identifier(path.substring(0, separator), path.substring(separator + 1));
	}

	public static Identifier identifier(String namespace, String path) {
		return new BasicIdentifier(namespace, path);
	}

	public static KeyBinding keyBinding(String name, int key, String category) {
		return new BasicKeyBinding(name, key, category);
	}

	public static ToggleKeyBinding toggleKeyBinding(String name, int key, String category,
			BooleanSupplier toggleMode, int maxHoldTime) {
		return new BasicToggleKeyBinding(name, key, category, toggleMode, maxHoldTime);
	}

	public static OptionsScreen optionsScreen(Screen parent) {
		return new BasicScreen(Text.literal("Options"));
	}

	public static SingleplayerScreen singleplayerScreen(Screen parent) {
		return new BasicScreen(Text.literal("Singleplayer"));
	}

	public static MultiplayerScreen multiplayerScreen(Screen parent) {
		return new BasicScreen(Text.literal("Multiplayer"));
	}

	public static LanguageScreen languageScreen(Screen parent) {
		return new BasicScreen(Text.literal("Language"));
	}

	public static TextFormatting textFormatting(String name) {
		String normalized = name.equals("GREY") ? "GRAY" : name;
		return FORMATTING.computeIfAbsent(normalized, BasicTextFormatting::new);
	}

	public static String stripFormatting(String message) {
		return message == null ? "" : message.replaceAll("§.", "");
	}

	public static ClickEvent clickEvent(ClickEvent.Action action, String value) {
		return new BasicClickEvent(action, value);
	}

	public static ClickEvent.Action clickAction(String name) {
		return new BasicClickAction(name);
	}

	public static OperatingSystem operatingSystem(String name) {
		return new BasicOperatingSystem(name);
	}

	public static Object environment(String key) {
		switch(key) {
			case "CLASS_LOADER": return BasicPlatform.class.getClassLoader();
			case "SODIUM":
			case "OPTIFINE":
			case "BLOCKING": return false;
			case "OPTIMISATION_ENGINE": return OptimisationEngine.VANILLA;
			case "OFFHAND":
			case "LWJGL3":
			case "PROPER_PLUGIN_MESSAGE_IDS": return true;
			case "TARGET_VERSION": return "unknown";
			case "VERSION_ID": return "unknown";
			case "MAJOR_RELEASE":
			case "MINOR_RELEASE":
			case "PATCH_RELEASE":
			case "PROTOCOL_VERSION": return 0;
			case "MOJANG": return "Mojang Studios";
			default: throw new IllegalArgumentException(key);
		}
	}

	public static Vec3i vec3i(int x, int y, int z) {
		return new BasicVec3i(x, y, z);
	}

	public static Vec3d vec3d(double x, double y, double z) {
		return new BasicVec3d(x, y, z);
	}

	public static Box box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return new BasicBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public static BlockPos blockPos(double x, double y, double z) {
		return new BasicBlockPos((int) x, (int) y, (int) z);
	}

	public static ChunkPos chunkPos(int x, int z) {
		return new BasicChunkPos(x, z);
	}

	public static Direction direction(String name) {
		return new BasicDirection(name);
	}

	public static Perspective perspective(String name) {
		return new BasicPerspective(name);
	}

	public static BlockType blockType(String name) {
		return new BasicBlockType(name);
	}

	public static ParticleType particleType(String name) {
		return new BasicParticleType(name);
	}

	public static HitType hitType(String name) {
		return new BasicHitType(name);
	}

	public static GameMode gameMode(String name, int numericId) {
		return new BasicGameMode(name, numericId);
	}

	public static VertexFormat vertexFormat(String name) {
		return new BasicVertexFormat(name);
	}

	public static ItemType itemType(String name) {
		return ITEMS_BY_NAME.computeIfAbsent(name, BasicItemType::new);
	}

	public static ItemStack itemStack(ItemType type, int quantity) {
		return new BasicItemStack(type == null ? itemType("AIR") : type, quantity);
	}

	public static ItemStack blockItemStack(BlockType type, int quantity) {
		return itemStack(type == null ? itemType("AIR") : type.toItem(), quantity);
	}

	public static io.github.solclient.client.platform.mc.model.BakedModel bakedModel(boolean gui3d) {
		return new BasicBakedModel(gui3d);
	}

	public static StatusEffectType statusEffectType(String name) {
		return EFFECTS.computeIfAbsent(name, BasicStatusEffectType::new);
	}

	public static StatusEffect statusEffect(StatusEffectType type, int duration, int amplifier) {
		return new BasicStatusEffect(type, duration, amplifier);
	}

	public static SoundType soundType(String name) {
		return new BasicSoundType(name);
	}

	public static SoundInstance uiSound(SoundType sound, float pitch) {
		return new BasicSoundInstance(sound, pitch);
	}

	public static Texture texture(String name) {
		return new BasicTexture(identifier("minecraft", name.toLowerCase(Locale.ROOT)));
	}

	public static Identifier textureIdentifier(String name) {
		switch (name) {
			case "ICONS_ID": return identifier("minecraft", "textures/gui/icons.png");
			case "INVENTORY_ID": return identifier("minecraft", "textures/gui/container/inventory.png");
			case "MOB_EFFECTS_ATLAS_ID": return identifier("minecraft", "textures/atlas/mob_effects.png");
			default: return identifier(name);
		}
	}

	public static C2SChatMessagePacket chatPacket(String message) {
		return new BasicChatPacket(message);
	}

	public static BufferBuilder bufferBuilder() {
		return BUFFER;
	}

	public static Tessellator tessellator() {
		return TESSELLATOR;
	}

	public static class BasicWindow implements Window {
		@Override public int width() { return 854; }
		@Override public int height() { return 480; }
		@Override public int scaleFactor() { return 1; }
		@Override public int scaledWidth() { return width(); }
		@Override public int scaledHeight() { return height(); }
		@Override public double scaledWidthD() { return width(); }
		@Override public double scaledHeightD() { return height(); }
	}

	public static class BasicFont implements Font {
		private RenderBackend backend() { return PlatformServices.renderBackend(); }
		@Override public int render(String text, int x, int y, int rgb) { return backend().render(text, x, y, rgb, false); }
		@Override public int render(String text, int x, int y, int rgb, boolean shadow) { return backend().render(text, x, y, rgb, shadow); }
		@Override public int renderWithShadow(String text, int x, int y, int rgb) { return backend().render(text, x, y, rgb, true); }
		@Override public int render(Text text, int x, int y, int rgb) { return backend().render(text, x, y, rgb, false); }
		@Override public int render(Text text, int x, int y, int rgb, boolean shadow) { return backend().render(text, x, y, rgb, shadow); }
		@Override public int renderWithShadow(Text text, int x, int y, int rgb) { return backend().render(text, x, y, rgb, true); }
		@Override public int getCharacterWidth(char character) { return backend().getCharacterWidth(character); }
		@Override public int getTextWidth(String text) { return backend().getTextWidth(text); }
		@Override public int getTextWidth(Text text) { return backend().getTextWidth(text); }
		@Override public int getHeight() { return backend().getHeight(); }
	}

	public static class BasicIdentifier implements Identifier {
		private final String namespace, path;
		public BasicIdentifier(String namespace, String path) {
			this.namespace = namespace;
			this.path = path;
		}
		@Override public String namespace() { return namespace; }
		@Override public String path() { return path; }
		@Override public String toString() { return namespace + ":" + path; }
		@Override public int hashCode() { return Objects.hash(namespace, path); }
		@Override public boolean equals(Object object) {
			return object instanceof Identifier && namespace.equals(((Identifier) object).namespace()) && path.equals(((Identifier) object).path());
		}
	}

	public static class BasicText implements MutableText, OrderedText {
		private String value;
		private BasicStyle style = new BasicStyle();
		private final List<Text> extra = new ArrayList<>();
		public BasicText(String value) { this.value = value; }
		@Override public List<Text> getExtraText() { return extra; }
		@Override public Text clone() { BasicText copy = new BasicText(value); copy.style = style; copy.extra.addAll(extra); return copy; }
		@Override public String getPlain() { return value + extra.stream().map(Text::getPlain).reduce("", String::concat); }
		@Override public Style getStyle() { return style; }
		@Override public String getLegacy() { return style.getColour() == null ? getPlain() : style.getColour().toString() + getPlain(); }
		@Override public MutableText setStyle(Style style) { this.style = (BasicStyle) style; return this; }
		@Override public MutableText style(java.util.function.UnaryOperator<Style> styler) { this.style = (BasicStyle) styler.apply(style); return this; }
		@Override public String toString() { return getPlain(); }
	}

	public static class BasicStyle implements Style {
		private TextColour colour;
		private boolean bold, italic, underlined, strikethrough, obfuscated;
		private ClickEvent clickEvent;
		@Override public TextColour getColour() { return colour; }
		@Override public Style withColour(TextColour colour) { BasicStyle copy = copy(); copy.colour = colour; return copy; }
		@Override public boolean boldFlag() { return bold; }
		@Override public Style withBold(Boolean value) { BasicStyle copy = copy(); copy.bold = Boolean.TRUE.equals(value); return copy; }
		@Override public boolean italicFlag() { return italic; }
		@Override public Style withItalic(Boolean value) { BasicStyle copy = copy(); copy.italic = Boolean.TRUE.equals(value); return copy; }
		@Override public boolean underlinedFlag() { return underlined; }
		@Override public Style withUnderlined(Boolean value) { BasicStyle copy = copy(); copy.underlined = Boolean.TRUE.equals(value); return copy; }
		@Override public boolean strikethroughFlag() { return strikethrough; }
		@Override public Style withStrikethrough(Boolean value) { BasicStyle copy = copy(); copy.strikethrough = Boolean.TRUE.equals(value); return copy; }
		@Override public boolean obfuscatedFlag() { return obfuscated; }
		@Override public Style withObfuscated(Boolean value) { BasicStyle copy = copy(); copy.obfuscated = Boolean.TRUE.equals(value); return copy; }
		@Override public ClickEvent getClickEvent() { return clickEvent; }
		@Override public Style withClickEvent(ClickEvent event) { BasicStyle copy = copy(); copy.clickEvent = event; return copy; }
		private BasicStyle copy() { BasicStyle copy = new BasicStyle(); copy.colour = colour; copy.bold = bold; copy.italic = italic; copy.underlined = underlined; copy.strikethrough = strikethrough; copy.obfuscated = obfuscated; copy.clickEvent = clickEvent; return copy; }
	}

	public static class BasicTextFormatting implements TextFormatting, TextColour {
		private final String name;
		private BasicTextFormatting(String name) { this.name = name; }
		@Override public String enumName() { return name; }
		@Override public int enumOrdinal() { return Arrays.asList("BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE").indexOf(name); }
		@Override public Enum<?> toEnum() { return null; }
		@Override public VirtualEnum[] getValues() { return FORMATTING.values().toArray(new VirtualEnum[0]); }
		@Override public Enum<?>[] getEnumValues() { return new Enum<?>[0]; }
		@Override public TextColour getColour() { return enumOrdinal() >= 0 ? this : null; }
		@Override public String toString() { return "§" + "0123456789abcdefklmnor".charAt(Math.max(0, enumOrdinal())); }
	}

	public static class BasicClickEvent implements ClickEvent {
		private final Action action;
		private final String value;
		public BasicClickEvent(Action action, String value) { this.action = action; this.value = value; }
		@Override public Action action() { return action; }
		@Override public String value() { return value; }
	}

	public static class BasicClickAction implements ClickEvent.Action {
		private final String name;
		public BasicClickAction(String name) { this.name = name; }
		@Override public String enumName() { return name; }
		@Override public int enumOrdinal() { return 0; }
		@Override public Enum<?> toEnum() { return null; }
		@Override public VirtualEnum[] getValues() { return new VirtualEnum[0]; }
		@Override public Enum<?>[] getEnumValues() { return new Enum<?>[0]; }
		@Override public String toString() { return name; }
	}

	public static class BasicOperatingSystem implements OperatingSystem {
		private final String name;
		public BasicOperatingSystem(String name) { this.name = name; }
		@Override public String enumName() { return name; }
		@Override public int enumOrdinal() { return Arrays.asList("LINUX", "SOLARIS", "WINDOWS", "OSX", "UNKNOWN").indexOf(name); }
		@Override public Enum<?> toEnum() { return null; }
		@Override public VirtualEnum[] getValues() { return new VirtualEnum[0]; }
		@Override public Enum<?>[] getEnumValues() { return new Enum<?>[0]; }
		@Override public String toString() { return name; }
	}

	public static class BasicVec3i implements Vec3i {
		private final int x, y, z;
		public BasicVec3i(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
		@Override public int x() { return x; }
		@Override public int y() { return y; }
		@Override public int z() { return z; }
		@Override public double distanceSquared(double x, double y, double z) { double dx = this.x - x, dy = this.y - y, dz = this.z - z; return dx * dx + dy * dy + dz * dz; }
		@Override public double distanceSquared(Vec3i other) { return distanceSquared(other.x(), other.y(), other.z()); }
	}

	public static class BasicVec3d implements Vec3d {
		private final double x, y, z;
		public BasicVec3d(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
		@Override public double x() { return x; }
		@Override public double y() { return y; }
		@Override public double z() { return z; }
		@Override public double distanceSquared(Vec3d other) { double dx = x - other.x(), dy = y - other.y(), dz = z - other.z(); return dx * dx + dy * dy + dz * dz; }
	}

	public static class BasicBox implements Box {
		private final double minX, minY, minZ, maxX, maxY, maxZ;
		public BasicBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) { this.minX = minX; this.minY = minY; this.minZ = minZ; this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ; }
		@Override public double minX() { return minX; }
		@Override public double minY() { return minY; }
		@Override public double minZ() { return minZ; }
		@Override public double maxX() { return maxX; }
		@Override public double maxY() { return maxY; }
		@Override public double maxZ() { return maxZ; }
	}

	public static class BasicBlockPos extends BasicVec3i implements BlockPos {
		public BasicBlockPos(int x, int y, int z) { super(x, y, z); }
	}

	public static class BasicChunkPos implements ChunkPos {
		private final int x, z;
		public BasicChunkPos(int x, int z) { this.x = x; this.z = z; }
		@Override public int x() { return x; }
		@Override public int z() { return z; }
	}

	public static class BasicDirection implements Direction {
		private final String name;
		public BasicDirection(String name) { this.name = name; }
		@Override public String enumName() { return name; }
		@Override public int enumOrdinal() { return Arrays.asList("DOWN", "UP", "NORTH", "SOUTH", "WEST", "EAST").indexOf(name); }
		@Override public Enum<?> toEnum() { return null; }
		@Override public VirtualEnum[] getValues() { return new VirtualEnum[0]; }
		@Override public Enum<?>[] getEnumValues() { return new Enum<?>[0]; }
		@Override public String toString() { return name; }
	}

	public static class BasicKeyBinding implements KeyBinding {
		private final String name, category;
		private int keyCode;
		private boolean held;
		private int presses;
		public BasicKeyBinding(String name, int keyCode, String category) { this.name = name; this.keyCode = keyCode; this.category = category; }
		public void setPressed(boolean pressed) { held = pressed; if(pressed) presses++; }
		public void setKeyCode(int keyCode) { this.keyCode = keyCode; }
		@Override public String getKeyCategory() { return category; }
		@Override public String getName() { return name; }
		@Override public int getKeyCode() { return keyCode; }
		@Override public String getBoundKeyName() { return keyCode < 0 ? "Unbound" : "KEY_" + keyCode; }
		@Override public boolean isHeld() { return held; }
		@Override public boolean consumePress() { if(presses <= 0) return false; presses--; return true; }
		@Override public List<KeyBinding> getConflictingKeys() { return Collections.emptyList(); }
		@Override public boolean conflicts() { return false; }
	}

	public static class BasicToggleKeyBinding extends BasicKeyBinding implements ToggleKeyBinding {
		private final BooleanSupplier toggleMode;
		private final int maxHoldTime;
		private boolean toggled;
		private int heldTicks;
		public BasicToggleKeyBinding(String name, int keyCode, String category, BooleanSupplier toggleMode, int maxHoldTime) { super(name, keyCode, category); this.toggleMode = toggleMode; this.maxHoldTime = maxHoldTime; }
		@Override public boolean isHeld() { return toggleMode.getAsBoolean() ? super.isHeld() : toggled; }
		@Override public boolean realHeld() { return super.isHeld(); }
	}

	public static class BasicPerspective implements Perspective {
		private final String name;
		private BasicPerspective(String name) { this.name = name; }
		@Override public boolean isFirstPerson() { return name.equals("FIRST_PERSON"); }
		@Override public boolean isThirdPerson() { return !isFirstPerson(); }
		@Override public boolean isReversed() { return name.equals("THIRD_PERSON_FRONT"); }
		@Override public String enumName() { return name; }
		@Override public int enumOrdinal() { return isFirstPerson() ? 0 : isReversed() ? 2 : 1; }
		@Override public Enum<?> toEnum() { return null; }
		@Override public VirtualEnum[] getValues() { return new VirtualEnum[0]; }
		@Override public Enum<?>[] getEnumValues() { return new Enum<?>[0]; }
		@Override public String toString() { return name; }
	}

	public static class BasicOptions implements Options {
		private final List<KeyBinding> keys = new ArrayList<>();
		private final KeyBinding forward = keyBinding("key.forward", 87, "key.categories.movement");
		private final KeyBinding back = keyBinding("key.back", 83, "key.categories.movement");
		private final KeyBinding left = keyBinding("key.left", 65, "key.categories.movement");
		private final KeyBinding right = keyBinding("key.right", 68, "key.categories.movement");
		private final KeyBinding attack = keyBinding("key.attack", 0, "key.categories.gameplay");
		private final KeyBinding use = keyBinding("key.use", 1, "key.categories.gameplay");
		private final KeyBinding jump = keyBinding("key.jump", 32, "key.categories.movement");
		private KeyBinding sneak = keyBinding("key.sneak", 340, "key.categories.movement");
		private KeyBinding sprint = keyBinding("key.sprint", 341, "key.categories.movement");
		private Perspective perspective = new BasicPerspective("FIRST_PERSON");
		private boolean smoothCamera;
		private BasicOptions() { Collections.addAll(keys, forward, back, left, right, attack, use, jump, sneak, sprint); }
		@Override public double mouseSensitivity() { return 0.5; }
		@Override public void setMouseSensitivity(double sensitivity) {}
		@Override public boolean invertMouse() { return false; }
		@Override public boolean debugOverlay() { return false; }
		@Override public Perspective perspective() { return perspective; }
		@Override public int ordinalPerspective() { return perspective.enumOrdinal(); }
		@Override public void setPerspective(Perspective perspective) { this.perspective = perspective; }
		@Override public void setOrdinalPerspective(int perspective) { this.perspective = new BasicPerspective(perspective == 2 ? "THIRD_PERSON_FRONT" : perspective == 1 ? "THIRD_PERSON_BACK" : "FIRST_PERSON"); }
		@Override public KeyBinding[] keys() { return keys.toArray(new KeyBinding[0]); }
		@Override public KeyBinding forwardsKey() { return forward; }
		@Override public KeyBinding backwardsKey() { return back; }
		@Override public KeyBinding strafeLeftKey() { return left; }
		@Override public KeyBinding strafeRightKey() { return right; }
		@Override public KeyBinding attackKey() { return attack; }
		@Override public KeyBinding useKey() { return use; }
		@Override public KeyBinding jumpKey() { return jump; }
		@Override public KeyBinding sneakKey() { return sneak; }
		@Override public void overwriteSneakKey(KeyBinding sneak) { this.sneak = sneak; }
		@Override public KeyBinding sprintKey() { return sprint; }
		@Override public void overwriteSprintKey(KeyBinding sprint) { this.sprint = sprint; }
		@Override public boolean hideGui() { return false; }
		@Override public void addKey(KeyBinding key) { if(!keys.contains(key)) keys.add(key); }
		@Override public void removeKey(KeyBinding key) { keys.remove(key); }
		@Override public boolean smoothCamera() { return smoothCamera; }
		@Override public void setSmoothCamera(boolean camera) { smoothCamera = camera; }
		@Override public void setMouseButton(KeyBinding binding, int button) { if(binding instanceof BasicKeyBinding) ((BasicKeyBinding) binding).setKeyCode(-button - 1); }
		@Override public void setKey(KeyBinding binding, int code, int scancode) { if(binding instanceof BasicKeyBinding) ((BasicKeyBinding) binding).setKeyCode(code); }
		@Override public void unbindKey(KeyBinding binding) { if(binding instanceof BasicKeyBinding) ((BasicKeyBinding) binding).setKeyCode(-1); }
		@Override public void saveFile() {}
		@Override public String languageCode() { return "en_us"; }
	}

	public static class BasicScreen extends ProxyScreen implements OptionsScreen, SingleplayerScreen, MultiplayerScreen, LanguageScreen {
		public BasicScreen(Text title) { super(title); }
		@Override public void initScreen() {}
	}

	public static class BasicTitleScreen extends BasicScreen implements TitleScreen {
		public BasicTitleScreen() { super(Text.literal("Title")); }
		@Override public void renderTitlePanorama(int x, int y, float tickDelta) { PlatformServices.renderBackend().renderPanorama(getWidth(), getHeight(), tickDelta); }
	}

	public static class BasicLanguageManager implements LanguageManager {
		@Override public String getCode() { return "en_us"; }
	}

	public static class BasicResourceManager implements ResourceManager {
		@Override public List<Resource> getResources(Identifier id) {
			InputStream stream = BasicPlatform.class.getClassLoader().getResourceAsStream("assets/" + id.namespace() + "/" + id.path());
			return stream == null ? Collections.emptyList() : Collections.singletonList(() -> stream);
		}
	}

	public static class BasicTextureManager implements TextureManager {
		@Override public CompletableFuture<Texture> download(String url) { return CompletableFuture.completedFuture(new BasicTexture(identifier(url))); }
		@Override public void bind(Texture texture) {}
		@Override public void bind(Identifier id) {}
		@Override public void delete(Texture texture) {}
		@Override public Texture getTexture(Identifier id) { return new BasicTexture(id); }
	}

	public static class BasicTexture implements Texture {
		private final Identifier id;
		public BasicTexture(Identifier id) { this.id = id; }
		public Identifier id() { return id; }
	}

	public static class BasicBufferBuilder implements BufferBuilder {
		@Override public void begin(int mode, VertexFormat format) {}
		@Override public Vertex vertex(double x, double y, double z) { return new BasicVertex(this); }
	}

	public static class BasicVertex implements Vertex {
		private final BufferBuilder builder;
		public BasicVertex(BufferBuilder builder) { this.builder = builder; }
		@Override public Vertex color(int r, int g, int b, int a) { return this; }
		@Override public BufferBuilder endVertex() { return builder; }
	}

	public static class BasicTessellator implements Tessellator {
		private final BufferBuilder builder;
		public BasicTessellator(BufferBuilder builder) { this.builder = builder; }
		@Override public BufferBuilder getBufferBuilder() { return builder; }
		@Override public void end() {}
	}

	public static class BasicTimer implements io.github.solclient.client.platform.mc.Timer {
		@Override public float getTickDelta() { return 0; }
	}

	public static class BasicSoundType implements SoundType {
		private final Identifier id;
		public BasicSoundType(String name) { id = identifier("minecraft", "sounds/" + name.toLowerCase(Locale.ROOT)); }
		@Override public Identifier getId() { return id; }
	}

	public static class BasicSoundInstance implements SoundInstance {
		private final SoundType sound;
		private final float pitch;
		public BasicSoundInstance(SoundType sound, float pitch) { this.sound = sound; this.pitch = pitch; }
	}

	public static class BasicSoundEngine implements SoundEngine {
		@Override public void play(SoundInstance sound) {}
	}

	public static class BasicLevelRenderer implements LevelRenderer {
		@Override public void scheduleUpdate() {}
	}

	public static class BasicItemRenderer implements ItemRenderer {
		@Override public void render(ItemStack item, int x, int y) {}
	}

	public static class BasicHud implements IngameHud {
		private final BasicChat chat = new BasicChat();
		@Override public Chat getChat() { return chat; }
		@Override public int getTickCounter() { return 0; }
	}

	public static class BasicChat implements Chat {
		private final List<ChatMessage> messages = new ArrayList<>();
		private ChatButton selected;
		@Override public void resetChat() { messages.clear(); }
		@Override public void scrollToStart() {}
		@Override public int getLineCount() { return messages.size(); }
		@Override public List<ChatMessage> getVisibleMessages() { return messages; }
		@Override public boolean isOpen() { return false; }
		@Override public int getChatWidth() { return 320; }
		@Override public int getScroll() { return 0; }
		@Override public void scrollChat(int amount) {}
		@Override public boolean isScrolled() { return false; }
		@Override public void addMessage(String text) {}
		@Override public void addMessage(Text text) {}
		@Override public void setSelectedButton(ChatButton button) { selected = button; }
		@Override public ChatButton getSelectedButton() { return selected; }
	}

	public static class BasicHitResult implements HitResult {
		@Override public HitType getType() { return new BasicHitType("MISS"); }
		@Override public Entity getEntity() { return null; }
		@Override public BlockPos getBlockPos() { return null; }
		@Override public Direction getBlockSide() { return null; }
	}

	public static class BasicHitType implements HitType {
		private final String name;
		public BasicHitType(String name) { this.name = name; }
		@Override public String enumName() { return name; }
		@Override public int enumOrdinal() { return name.equals("MISS") ? 0 : name.equals("ENTITY") ? 1 : 2; }
		@Override public Enum<?> toEnum() { return null; }
		@Override public VirtualEnum[] getValues() { return new VirtualEnum[0]; }
		@Override public Enum<?>[] getEnumValues() { return new Enum<?>[0]; }
		@Override public String toString() { return name; }
	}

	public static class BasicGameMode implements GameMode {
		private final String name;
		private final int numericId;
		public BasicGameMode(String name, int numericId) { this.name = name; this.numericId = numericId; }
		@Override public String getId() { return name.toLowerCase(Locale.ROOT); }
		@Override public int getNumericId() { return numericId; }
		@Override public String enumName() { return name; }
		@Override public int enumOrdinal() { return numericId; }
		@Override public Enum<?> toEnum() { return null; }
		@Override public VirtualEnum[] getValues() { return new VirtualEnum[0]; }
		@Override public Enum<?>[] getEnumValues() { return new Enum<?>[0]; }
	}

	public static class BasicParticleType implements ParticleType {
		private final String name;
		public BasicParticleType(String name) { this.name = name; }
		@Override public String toString() { return name; }
	}

	public static class BasicVertexFormat implements VertexFormat {
		private final String name;
		public BasicVertexFormat(String name) { this.name = name; }
		@Override public String toString() { return name; }
	}

	public static class BasicEntityRenderDispatcher implements EntityRenderDispatcher {
		private boolean hitboxes;
		@Override public void setHitboxes(boolean hitboxes) { this.hitboxes = hitboxes; }
		@Override public boolean getHitboxes() { return hitboxes; }
	}

	public static class BasicParticleEngine implements ParticleEngine {
		@Override public void emit(Entity entity, ParticleType particle) {}
		@Override public void emitDestruction(BlockPos blockPos, Direction side) {}
	}

	public static class BasicMouseHandler implements MouseHandler {
		@Override public void grabCursor() {}
	}

	public static class BasicItemType implements ItemType {
		private final String name;
		public BasicItemType(String name) { this.name = name; }
		@Override public String toString() { return name; }
	}

	public static class BasicBlockType implements BlockType {
		private final Identifier id;
		public BasicBlockType(String name) { id = identifier("minecraft", name.toLowerCase(Locale.ROOT)); }
		@Override public Identifier getId() { return id; }
		@Override public ItemType toItem() { return itemType(id.path()); }
		@Override public void fillBox() {}
		@Override public void strokeBox() {}
	}

	public static class BasicItemStack implements ItemStack {
		private final ItemType type;
		private int quantity;
		public BasicItemStack(ItemType type, int quantity) { this.type = type; this.quantity = quantity; }
		@Override public int getQuantity() { return quantity; }
		@Override public void setQuantity(int quantity) { this.quantity = quantity; }
		@Override public ItemType getType() { return type; }
		@Override public int getDamageValue() { return 0; }
		@Override public int getMaxDamageValue() { return 0; }
		@Override public Text getDisplayName() { return literal(type.toString()); }
		@Override public boolean canDestroy(Level level, BlockPos pos) { return false; }
		@Override public boolean canPlaceOn(Level level, BlockPos pos) { return false; }
		@Override public int getMaxItemUseTime() { return 0; }
	}

	public static class BasicBakedModel implements io.github.solclient.client.platform.mc.model.BakedModel {
		private final boolean gui3d;
		public BasicBakedModel(boolean gui3d) { this.gui3d = gui3d; }
		@Override public boolean isGui3d() { return gui3d; }
	}

	public static class BasicStatusEffectType implements StatusEffectType {
		private final String name;
		public BasicStatusEffectType(String name) { this.name = name; }
		@Override public void render(int x, int y) {}
		@Override public String getName() { return name; }
	}

	public static class BasicStatusEffect implements StatusEffect {
		private final StatusEffectType type;
		private final int duration, amplifier;
		public BasicStatusEffect(StatusEffectType type, int duration, int amplifier) { this.type = type; this.duration = duration; this.amplifier = amplifier; }
		@Override public StatusEffectType getType() { return type; }
		@Override public int getEffectDuration() { return duration; }
		@Override public String getDurationText() { return duration / 20 + "s"; }
		@Override public int getEffectAmplifier() { return amplifier; }
		@Override public boolean showIcon() { return true; }
		@Override public boolean showAmplifier() { return amplifier > 0; }
		@Override public String getAmplifierName() { return "" + (amplifier + 1); }
	}

	public static class BasicChatPacket implements C2SChatMessagePacket {
		private final String message;
		public BasicChatPacket(String message) { this.message = message; }
		@Override public String message() { return message; }
	}

	public static class BasicClient implements MinecraftClient {
		private boolean running = true;
		private Screen screen;
		private final TitleScreen titleScreen = new BasicTitleScreen();
		@Override public Window getWindow() { return WINDOW; }
		@Override public ClientLevel getLevel() { return null; }
		@Override public LevelRenderer getLevelRenderer() { return LEVEL_RENDERER; }
		@Override public LocalPlayer getPlayer() { return null; }
		@Override public LocalPlayerState getPlayerState() { return null; }
		@Override public Entity getCameraEntity() { return null; }
		@Override public Options getOptions() { return OPTIONS; }
		@Override public File getDataFolder() { return new File("sol-client-data"); }
		@Override public File getPackFolder() { return new File(getDataFolder(), "resourcepacks"); }
		@Override public Font getFont() { return FONT; }
		@Override public Screen getScreen() { return screen; }
		@Override public void setScreen(Screen screen) { this.screen = screen; if(screen != null) { screen.update(this, WINDOW.scaledWidth(), WINDOW.scaledHeight()); screen.initScreen(); } }
		@Override public boolean isInMenu() { return screen != null; }
		@Override public boolean isGamePaused() { return false; }
		@Override public void runSync(Runnable runnable) { runnable.run(); }
		@Override public void runSyncLater(Runnable runnable, int ticks) { runnable.run(); }
		@Override public TextureManager getTextureManager() { return TEXTURES; }
		@Override public ResourceManager getResourceManager() { return RESOURCES; }
		@Override public LanguageManager getLanguageManager() { return LANGUAGES; }
		@Override public boolean hasSingleplayerServer() { return false; }
		@Override public ServerData getCurrentServer() { return null; }
		@Override public ItemRenderer getItemRenderer() { return ITEMS; }
		@Override public IngameHud getIngameHud() { return HUD; }
		@Override public boolean isGameRunning() { return running; }
		@Override public HitResult getHitResult() { return HIT_RESULT; }
		@Override public io.github.solclient.client.platform.mc.Timer getTimer() { return TIMER; }
		@Override public EntityRenderDispatcher getEntityRenderDispatcher() { return ENTITIES; }
		@Override public ParticleEngine getParticleEngine() { return PARTICLES; }
		@Override public MouseHandler getMouseHandler() { return MOUSE; }
		@Override public boolean isFullscreen() { return false; }
		@Override public void toggleFullscreenState() {}
		@Override public SoundEngine getSoundEngine() { return SOUNDS; }
		@Override public TitleScreen getTitleScreen() { return titleScreen; }
		@Override public void quit() { running = false; }
	}
}
