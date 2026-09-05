package io.github.solclient.client.v1_21_11;

import io.github.solclient.client.platform.NanoVGUiRenderer;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.RenderBackend;
import io.github.solclient.client.platform.UiRenderer;
import io.github.solclient.client.platform.mc.maths.Box;
import io.github.solclient.client.platform.mc.option.KeyBinding;
import io.github.solclient.client.platform.mc.option.ToggleKeyBinding;
import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.platform.mc.screen.LanguageScreen;
import io.github.solclient.client.platform.mc.screen.MultiplayerScreen;
import io.github.solclient.client.platform.mc.screen.OptionsScreen;
import io.github.solclient.client.platform.mc.screen.Screen;
import io.github.solclient.client.platform.mc.screen.SingleplayerScreen;
import io.github.solclient.client.platform.mc.sound.SoundInstance;
import io.github.solclient.client.platform.mc.sound.SoundType;
import io.github.solclient.client.platform.mc.text.MutableText;
import io.github.solclient.client.platform.mc.world.entity.effect.StatusEffect;
import io.github.solclient.client.platform.mc.world.entity.effect.StatusEffectType;
import io.github.solclient.client.platform.mc.world.item.ItemStack;
import io.github.solclient.client.platform.mc.world.item.ItemType;
import io.github.solclient.client.platform.mc.world.level.block.BlockType;
import io.github.solclient.client.ui.screen.SolClientMainMenu;
import io.github.solclient.client.util.TranslationProvider;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * The 1.21.11 implementation of the common service provider.
 *
 * <p>Only this package knows the current Minecraft names. Common feature and
 * UI code depends on {@link PlatformServices.Provider} instead.</p>
 */
public final class FabricPlatform implements PlatformServices.Provider {

    @Override
    public UiRenderer uiRenderer() {
        // 1.21.11 runs a GL 3 context and therefore uses the matching NanoVG
        // binding without compatibility-profile attribute calls.
        return new NanoVGUiRenderer(false, true);
    }

    private MinecraftClientAdapter client;
    private net.minecraft.client.gui.screens.TitleScreen titleScreen;
    private final Map<String, KeyMapping.Category> keyCategories = new HashMap<>();
    private final Map<String, ItemType> itemTypes = new HashMap<>();
    private final Map<net.minecraft.world.item.Item, NativeItemType> nativeItemTypes = new HashMap<>();
    private final Map<String, BlockType> blockTypes = new HashMap<>();
    private final Map<net.minecraft.world.level.block.Block, NativeBlockType> nativeBlockTypes = new HashMap<>();
    private final Map<String, StatusEffectType> statusEffects = new HashMap<>();
    private final Map<String, io.github.solclient.client.platform.mc.world.entity.player.GameMode> gameModes = new HashMap<>();
    private final Map<String, io.github.solclient.client.platform.mc.raycast.HitType> hitTypes = new HashMap<>();
    private final Map<String, io.github.solclient.client.platform.mc.Direction> directions = new HashMap<>();
    private final Map<String, io.github.solclient.client.platform.mc.world.particle.ParticleType> particles = new HashMap<>();
    private final Map<String, SoundType> sounds = new HashMap<>();
    private int borderlessX;
    private int borderlessY;
    private int borderlessWidth;
    private int borderlessHeight;
    private boolean borderlessWindowSaved;

    Minecraft nativeClient() {
        return Minecraft.getInstance();
    }

    MinecraftClientAdapter clientAdapter() {
        if (client == null) {
            client = new MinecraftClientAdapter(this, nativeClient());
        }
        return client;
    }

    /**
     * Converts a live native entity at a version boundary for common render events.
     */
    public io.github.solclient.client.platform.mc.world.entity.Entity commonEntity(
            net.minecraft.world.entity.Entity entity) {
        return NativeEntity.wrap(entity, this);
    }

    /**
     * Wraps a live native stack without copying it, so common transforms see current state.
     */
    public io.github.solclient.client.platform.mc.world.item.ItemStack commonItemStack(
            net.minecraft.world.item.ItemStack stack) {
        return new NativeItemStack(stack, this);
    }

    @Override
    public io.github.solclient.client.platform.mc.MinecraftClient minecraftClient() {
        return clientAdapter();
    }

    @Override
    public int fps() {
        return nativeClient().getFps();
    }

    @Override
    public MutableText literal(String text) {
        return io.github.solclient.client.platform.basic.BasicPlatform.literal(text);
    }

    @Override
    public String translate(String key, Object... args) {
        if (TranslationProvider.hasTranslation(key)) {
            String value = TranslationProvider.translate(key);
            return args.length == 0 ? value : String.format(value, args);
        }
        return net.minecraft.client.resources.language.I18n.get(key, args);
    }

    @Override
    public KeyBinding createKeyBinding(String name, int initialKey, String category) {
        KeyMapping.Category keyCategory = keyCategory(category);
        return new KeyMappingAdapter(new KeyMapping(name, initialKey,
                keyCategory));
    }

    @Override
    public ToggleKeyBinding createToggleKeyBinding(String name, int initialKey, String category,
                                                   BooleanSupplier toggleMode, int maxHoldTime) {
        KeyMapping.Category keyCategory = keyCategory(category);
        return new ToggleKeyMappingAdapter(new KeyMapping(name, initialKey,
                keyCategory),
                toggleMode, maxHoldTime);
    }

    /**
     * Resolve the category returned by the common options API without trying to
     * register a second copy of a vanilla category.  Older Minecraft versions
     * expose translated category keys while modern versions expose identifiers,
     * so custom categories must also tolerate either representation.
     */
    private KeyMapping.Category keyCategory(String category) {
        String value = category == null || category.isEmpty()
                ? "sol_client:key"
                : category.toLowerCase(Locale.ROOT);
        switch (value) {
            case "minecraft:movement":
                return KeyMapping.Category.MOVEMENT;
            case "minecraft:gameplay":
                return KeyMapping.Category.GAMEPLAY;
            case "minecraft:misc":
                return KeyMapping.Category.MISC;
            case "minecraft:multiplayer":
                return KeyMapping.Category.MULTIPLAYER;
            case "minecraft:inventory":
                return KeyMapping.Category.INVENTORY;
            case "minecraft:creative":
                return KeyMapping.Category.CREATIVE;
            case "minecraft:spectator":
                return KeyMapping.Category.SPECTATOR;
            case "minecraft:debug":
                return KeyMapping.Category.DEBUG;
            default:
                break;
        }

        String[] parts = value.split(":", 2);
        final String namespace = parts.length == 2 && !parts[0].isEmpty() ? parts[0] : "sol_client";
        final String path = parts.length == 2
                ? (parts[1].isEmpty() ? "key" : parts[1])
                : value;
        final String cacheKey = namespace + ":" + path;
        return keyCategories.computeIfAbsent(cacheKey, ignored -> KeyMapping.Category.register(
                nativeIdentifier(Identifier.create(namespace, path))));
    }

    @Override
    public void reloadKeyBindings() {
        KeyMapping.resetMapping();
    }

    @Override
    public ItemType itemType(String name) {
        String key = normalize(name);
        return itemTypes.computeIfAbsent(key, ignored -> itemType(nativeItem(key)));
    }

    ItemType itemType(net.minecraft.world.item.Item item) {
        if (item == null) item = net.minecraft.world.item.Items.AIR;
        final net.minecraft.world.item.Item value = item;
        return nativeItemTypes.computeIfAbsent(value, ignored -> new NativeItemType(value, this));
    }

    @Override
    public ItemStack itemStack(ItemType type, int quantity) {
        return new NativeItemStack(new net.minecraft.world.item.ItemStack(
                type instanceof NativeItemType ? ((NativeItemType) type).item : net.minecraft.world.item.Items.AIR,
                quantity), this);
    }

    @Override
    public BlockType blockType(String name) {
        String key = normalize(name);
        return blockTypes.computeIfAbsent(key, ignored -> blockType(nativeBlock(key)));
    }

    BlockType blockType(net.minecraft.world.level.block.Block block) {
        if (block == null) block = net.minecraft.world.level.block.Blocks.AIR;
        final net.minecraft.world.level.block.Block value = block;
        return nativeBlockTypes.computeIfAbsent(value, ignored -> new NativeBlockType(value, this));
    }

    @Override
    public ItemStack blockItemStack(BlockType type, int quantity) {
        return itemStack(type == null ? itemType("minecraft:air") : type.toItem(), quantity);
    }

    @Override
    public StatusEffectType statusEffectType(String name) {
        String key = normalize(name);
        return statusEffects.computeIfAbsent(key, ignored -> new NativeStatusEffectType(
                NativeStatusEffectType.resolve(this, key), this));
    }

    StatusEffectType statusEffectType(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> holder) {
        String key = NativeAdapters.holderId(holder).toLowerCase(Locale.ROOT);
        return statusEffects.computeIfAbsent(key, ignored -> new NativeStatusEffectType(holder, this));
    }

    @Override
    public StatusEffect statusEffect(StatusEffectType type, int duration, int amplifier) {
        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> holder = type instanceof NativeStatusEffectType
                ? ((NativeStatusEffectType) type).holder : NativeStatusEffectType.resolve(this, type.getName());
        if (holder == null) holder = net.minecraft.world.effect.MobEffects.SPEED;
        return new NativeStatusEffect(new net.minecraft.world.effect.MobEffectInstance(holder, duration, amplifier), this);
    }

    @Override
    public io.github.solclient.client.platform.mc.world.entity.player.GameMode gameMode(String name, int numericId) {
        String key = name.toUpperCase(Locale.ROOT);
        return gameModes.computeIfAbsent(key, ignored -> new io.github.solclient.client.platform.basic.BasicPlatform.BasicGameMode(key, numericId));
    }

    @Override
    public io.github.solclient.client.platform.mc.raycast.HitType hitType(String name) {
        String key = name.toUpperCase(Locale.ROOT);
        return hitTypes.computeIfAbsent(key, io.github.solclient.client.platform.basic.BasicPlatform.BasicHitType::new);
    }

    @Override
    public io.github.solclient.client.platform.mc.Direction direction(String name) {
        String key = name.toUpperCase(Locale.ROOT);
        return directions.computeIfAbsent(key, io.github.solclient.client.platform.basic.BasicPlatform.BasicDirection::new);
    }

    @Override
    public io.github.solclient.client.platform.mc.world.particle.ParticleType particleType(String name) {
        String key = name.toUpperCase(Locale.ROOT);
        return particles.computeIfAbsent(key, io.github.solclient.client.platform.basic.BasicPlatform.BasicParticleType::new);
    }

    @Override
    public SoundType soundType(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        return sounds.computeIfAbsent(key, ignored -> {
            net.minecraft.resources.Identifier id = nativeIdentifier(parseIdentifier(key));
            return new NativeSoundType(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT
                    .get(id).orElse(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK));
        });
    }

    @Override
    public SoundInstance uiSound(SoundType sound, float pitch) {
        net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> holder = sound instanceof NativeSoundType
                ? ((NativeSoundType) sound).sound : net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK;
        return new NativeSoundInstance(holder, pitch);
    }

    private net.minecraft.world.item.Item nativeItem(String key) {
        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(nativeIdentifier(parseIdentifier(key)));
        return item == null ? net.minecraft.world.item.Items.AIR : item;
    }

    private net.minecraft.world.level.block.Block nativeBlock(String key) {
        net.minecraft.world.level.block.Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(nativeIdentifier(parseIdentifier(key)));
        return block == null ? net.minecraft.world.level.block.Blocks.AIR : block;
    }

    private static String normalize(String value) {
        if (value == null || value.isEmpty()) return "minecraft:air";
        String key = value.toLowerCase(Locale.ROOT);
        return key.indexOf(':') < 0 ? "minecraft:" + key : key;
    }

    @Override
    public OptionsScreen createOptionsScreen(Screen parent,
                                             io.github.solclient.client.platform.mc.option.Options options) {
        return new NativeScreenHandle(new net.minecraft.client.gui.screens.options.OptionsScreen(
                nativeParent(parent), nativeClient().options));
    }

    @Override
    public SingleplayerScreen createSingleplayerScreen(Screen parent) {
        return new NativeScreenHandle(new net.minecraft.client.gui.screens.worldselection.SelectWorldScreen(
                nativeParent(parent)));
    }

    @Override
    public MultiplayerScreen createMultiplayerScreen(Screen parent) {
        return new NativeScreenHandle(new net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen(
                nativeParent(parent)));
    }

    @Override
    public LanguageScreen createLanguageScreen(Screen parent,
                                               io.github.solclient.client.platform.mc.option.Options options,
                                               io.github.solclient.client.platform.mc.lang.LanguageManager languages) {
        // Use the native language screen on modern targets.
        return new NativeScreenHandle(new net.minecraft.client.gui.screens.options.LanguageSelectScreen(
                nativeParent(parent), nativeClient().options, nativeClient().getLanguageManager()));
    }

    public io.github.solclient.client.platform.mc.world.scoreboard.Objective objective(
            net.minecraft.world.scores.Objective objective) {
        return objective == null ? null : new NativeObjective(objective, this);
    }

    @Override
    public RenderBackend renderBackend() {
        return RenderBackend.NONE;
    }

    /**
     * Modern clients collect debug geometry during the world pass.  Gizmos is
     * the stable submission API shared by both modern targets, so the common
     * block-selection and hitbox code can keep its old Box vocabulary.
     */
    @Override
    public void strokeBox(Box box, int red, int green, int blue, int alpha) {
        if (box == null) return;
        net.minecraft.world.phys.Vec3 camera = nativeClient().gameRenderer.getMainCamera().position();
        gizmo(new net.minecraft.world.phys.AABB(
                        box.minX() + camera.x, box.minY() + camera.y, box.minZ() + camera.z,
                        box.maxX() + camera.x, box.maxY() + camera.y, box.maxZ() + camera.z),
                false, (alpha << 24) | (red << 16) | (green << 8) | blue);
    }

    @Override
    public void strokeLine(io.github.solclient.client.platform.mc.maths.Vec3d start,
                           io.github.solclient.client.platform.mc.maths.Vec3d end,
                           int red, int green, int blue, int alpha) {
        if (start == null || end == null) return;
        net.minecraft.world.phys.Vec3 camera = nativeClient().gameRenderer.getMainCamera().position();
        net.minecraft.world.phys.Vec3 from = new net.minecraft.world.phys.Vec3(
                start.x() + camera.x, start.y() + camera.y, start.z() + camera.z);
        net.minecraft.world.phys.Vec3 to = new net.minecraft.world.phys.Vec3(
                end.x() + camera.x, end.y() + camera.y, end.z() + camera.z);
        int colour = (alpha << 24) | (red << 16) | (green << 8) | blue;
        try {
            net.minecraft.gizmos.Gizmos.line(from, to, colour,
                    Math.max(1.0F, PlatformServices.currentLineWidth()));
        } catch (IllegalStateException ignored) {
            // World geometry is optional outside the active gizmo collector.
        }
    }

    @Override
    public void setBorderlessFullscreen(boolean state) {
        net.minecraft.client.Minecraft minecraft = nativeClient();
        long handle = minecraft.getWindow().handle();
        try {
            if (state) {
                if (!borderlessWindowSaved) {
                    try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
                        java.nio.IntBuffer x = stack.mallocInt(1);
                        java.nio.IntBuffer y = stack.mallocInt(1);
                        java.nio.IntBuffer width = stack.mallocInt(1);
                        java.nio.IntBuffer height = stack.mallocInt(1);
                        org.lwjgl.glfw.GLFW.glfwGetWindowPos(handle, x, y);
                        org.lwjgl.glfw.GLFW.glfwGetWindowSize(handle, width, height);
                        borderlessX = x.get(0);
                        borderlessY = y.get(0);
                        borderlessWidth = width.get(0);
                        borderlessHeight = height.get(0);
                    }
                    borderlessWindowSaved = true;
                }
                org.lwjgl.glfw.GLFW.glfwSetWindowAttrib(handle,
                        org.lwjgl.glfw.GLFW.GLFW_DECORATED, org.lwjgl.glfw.GLFW.GLFW_FALSE);
                org.lwjgl.glfw.GLFWVidMode mode = org.lwjgl.glfw.GLFW.glfwGetVideoMode(
                        org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor());
                if (mode != null) {
                    org.lwjgl.glfw.GLFW.glfwSetWindowPos(handle, 0, 0);
                    org.lwjgl.glfw.GLFW.glfwSetWindowSize(handle, mode.width(), mode.height());
                }
            } else {
                org.lwjgl.glfw.GLFW.glfwSetWindowAttrib(handle,
                        org.lwjgl.glfw.GLFW.GLFW_DECORATED, org.lwjgl.glfw.GLFW.GLFW_TRUE);
                if (borderlessWindowSaved) {
                    org.lwjgl.glfw.GLFW.glfwSetWindowPos(handle, borderlessX, borderlessY);
                    org.lwjgl.glfw.GLFW.glfwSetWindowSize(handle, borderlessWidth, borderlessHeight);
                    borderlessWindowSaved = false;
                }
            }
            minecraft.resizeDisplay();
        } catch (RuntimeException error) {
            System.err.println("[Sol Client] Could not toggle borderless fullscreen: " + error);
        }
    }

    @Override
    public void renderBlockBox(BlockType block, boolean fill) {
        if (block == null || nativeClient().level == null) return;
        io.github.solclient.client.platform.mc.world.level.block.BlockPos commonPos =
                minecraftClient().getHitResult().getBlockPos();
        if (commonPos == null) return;
        net.minecraft.core.BlockPos pos = NativeAdapters.nativePos(commonPos);
        net.minecraft.world.level.block.state.BlockState state = nativeClient().level.getBlockState(pos);
        net.minecraft.world.phys.shapes.VoxelShape shape = state.getShape(nativeClient().level, pos);
        if (shape == null || shape.isEmpty()) return;
        gizmo(shape.bounds().move(pos).inflate(0.002D), fill, PlatformServices.currentColour());
    }

    private void gizmo(net.minecraft.world.phys.AABB box, boolean fill, int colour) {
        try {
            if (fill) {
                net.minecraft.gizmos.Gizmos.cuboid(box, net.minecraft.gizmos.GizmoStyle.fill(colour));
            } else {
                net.minecraft.gizmos.Gizmos.cuboid(box,
                        net.minecraft.gizmos.GizmoStyle.stroke(colour,
                                Math.max(1.0F, PlatformServices.currentLineWidth())));
            }
        } catch (IllegalStateException ignored) {
            // A renderer hook can also run outside the temporary gizmo collector
            // during loading screens. Rendering must remain optional there.
        }
    }

    @Override
    public boolean isKeyDown(int code) {
        if (code < 0) return false;
        return com.mojang.blaze3d.platform.InputConstants.isKeyDown(nativeClient().getWindow(), code);
    }

    @Override
    public boolean isMouseButtonDown(int button) {
        if (button < 0) return false;
        return org.lwjgl.glfw.GLFW.glfwGetMouseButton(nativeClient().getWindow().handle(), button)
                == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    @Override
    public void copyToClipboard(String text) {
        nativeClient().keyboardHandler.setClipboard(text == null ? "" : text);
    }

    @Override
    public String getClipboardContent() {
        return nativeClient().keyboardHandler.getClipboard();
    }

    @Override
    public Object environment(String key) {
        switch (key) {
            case "CLASS_LOADER":
                return FabricPlatform.class.getClassLoader();
            case "SODIUM":
            case "OPTIFINE":
            case "BLOCKING":
                return false;
            case "OPTIMISATION_ENGINE":
                return io.github.solclient.client.platform.mc.OptimisationEngine.VANILLA;
            case "OFFHAND":
            case "LWJGL3":
            case "PROPER_PLUGIN_MESSAGE_IDS":
                return true;
            case "TARGET_VERSION":
                return "1.21.11";
            case "VERSION_ID":
                return "1.21.11";
            case "MAJOR_RELEASE":
                return 1;
            case "MINOR_RELEASE":
                return 21;
            case "PATCH_RELEASE":
                return 11;
            case "PROTOCOL_VERSION":
                return 775;
            case "MOJANG":
                return "Mojang Studios";
            default:
                throw new IllegalArgumentException("Unknown Sol Client environment key: " + key);
        }
    }

    net.minecraft.resources.Identifier nativeIdentifier(Identifier identifier) {
        String namespace = identifier.namespace();
        if ("sol_client".equals(namespace)) {
            namespace = "sol-client";
        } else if ("minecraft".equals(namespace)
                && (identifier.path().startsWith("textures/gui/sol_client_")
                || identifier.path().equals("textures/gui/discord_avatar_generic.png"))) {
            namespace = "sol-client";
        }
        return net.minecraft.resources.Identifier.fromNamespaceAndPath(namespace, identifier.path());
    }

    /**
     * Called by the common bootstrap on the game thread.
     */
    public void showMainMenu() {
        Minecraft minecraft = nativeClient();
        if (minecraft.screen == null || minecraft.screen instanceof net.minecraft.client.gui.screens.TitleScreen) {
            if (minecraft.screen instanceof net.minecraft.client.gui.screens.TitleScreen) {
                titleScreen = (net.minecraft.client.gui.screens.TitleScreen) minecraft.screen;
            }
            minecraft.setScreen(new CommonScreenAdapter(new SolClientMainMenu(), minecraft, this));
        }
    }

    net.minecraft.client.gui.screens.TitleScreen titleScreen() {
        if (titleScreen == null) titleScreen = new net.minecraft.client.gui.screens.TitleScreen();
        return titleScreen;
    }

    private net.minecraft.client.gui.screens.Screen nativeParent(Screen parent) {
        Minecraft minecraft = nativeClient();
        net.minecraft.client.gui.screens.Screen current = minecraft.screen;
        if (current instanceof CommonScreenAdapter && ((CommonScreenAdapter) current).delegate() == parent) {
            return current;
        }
        return CommonScreenAdapter.unwrap(parent);
    }

}
