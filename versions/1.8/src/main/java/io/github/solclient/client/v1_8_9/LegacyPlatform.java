package io.github.solclient.client.v1_8_9;

import io.github.solclient.client.platform.NanoVGUiRenderer;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.UiRenderer;
import io.github.solclient.client.platform.basic.BasicPlatform;
import io.github.solclient.client.platform.mc.OptimisationEngine;
import io.github.solclient.client.platform.mc.lang.LanguageManager;
import io.github.solclient.client.platform.mc.maths.Box;
import io.github.solclient.client.platform.mc.option.KeyBinding;
import io.github.solclient.client.platform.mc.option.Options;
import io.github.solclient.client.platform.mc.option.ToggleKeyBinding;
import io.github.solclient.client.platform.mc.screen.LanguageScreen;
import io.github.solclient.client.platform.mc.screen.MultiplayerScreen;
import io.github.solclient.client.platform.mc.screen.OptionsScreen;
import io.github.solclient.client.platform.mc.screen.Screen;
import io.github.solclient.client.platform.mc.screen.SingleplayerScreen;
import io.github.solclient.client.platform.mc.world.entity.effect.StatusEffectType;
import io.github.solclient.client.platform.mc.world.item.ItemType;
import io.github.solclient.client.platform.mc.world.level.block.BlockType;
import io.github.solclient.client.ui.screen.SolClientMainMenu;
import io.github.solclient.client.v1_8_9.platform.mc.option.LegacyToggleKeyBinding;
import io.github.solclient.client.wrapper.WrapperClassLoader;
import io.github.solclient.client.wrapper.transformer.impl.optifine.OptiFineTransformer;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Native services for the 1.8.9 Legacy Fabric target.
 *
 * <p>The common layer deliberately never names these old Minecraft classes;
 * this provider is the one place where registry objects and the legacy title
 * screen cross that boundary.</p>
 */
public final class LegacyPlatform implements PlatformServices.Provider {

    @Override
    public UiRenderer uiRenderer() {
        // Same shared NanoVG renderer as modern targets; only the OpenGL
        // binding/context mode differs because 1.8 owns an LWJGL 2 context.
        return new NanoVGUiRenderer(true, false);
    }

    private final Map<String, ItemType> itemTypes = new HashMap<>();
    private final Map<Item, ItemType> nativeItemTypes = new HashMap<>();
    private final Map<String, BlockType> blockTypes = new HashMap<>();
    private final Map<Block, BlockType> nativeBlockTypes = new HashMap<>();
    private final Map<String, StatusEffectType> statusEffects = new HashMap<>();
    private int borderlessX;
    private int borderlessY;
    private int borderlessWidth;
    private int borderlessHeight;
    private boolean borderlessWindowSaved;

    @Override
    public io.github.solclient.client.platform.mc.MinecraftClient minecraftClient() {
        return (io.github.solclient.client.platform.mc.MinecraftClient) (Object) MinecraftClient.getInstance();
    }

    @Override
    public int fps() {
        return MinecraftClient.getCurrentFps();
    }

    @Override
    public KeyBinding createKeyBinding(String name, int initialKey, String category) {
        return (KeyBinding) (Object) new net.minecraft.client.options.KeyBinding(name, initialKey, category);
    }

    @Override
    public ToggleKeyBinding createToggleKeyBinding(String name, int initialKey, String category,
                                                   java.util.function.BooleanSupplier toggleMode, int maxHoldTime) {
        return (ToggleKeyBinding) (Object) new LegacyToggleKeyBinding(name, initialKey, category,
                toggleMode, maxHoldTime);
    }

    @Override
    public void reloadKeyBindings() {
        net.minecraft.client.options.KeyBinding.updateKeysByCode();
    }

    @Override
    public ItemType itemType(String name) {
        String key = normalize(name);
        return itemTypes.computeIfAbsent(key, ignored -> itemType(nativeItem(key)));
    }

    private ItemType itemType(Item item) {
        if (item == null) item = Item.REGISTRY.get(new net.minecraft.util.Identifier("minecraft:air"));
        final Item value = item;
        return nativeItemTypes.computeIfAbsent(value, ignored -> (ItemType) (Object) value);
    }

    @Override
    public BlockType blockType(String name) {
        String key = normalize(name);
        return blockTypes.computeIfAbsent(key, ignored -> blockType(nativeBlock(key)));
    }

    private BlockType blockType(Block block) {
        if (block == null) block = Block.REGISTRY.get(new net.minecraft.util.Identifier("minecraft:air"));
        final Block value = block;
        return nativeBlockTypes.computeIfAbsent(value, ignored -> (BlockType) (Object) value);
    }

    @Override
    public StatusEffectType statusEffectType(String name) {
        String key = name == null ? "SPEED" : name.toUpperCase(Locale.ROOT);
        return statusEffects.computeIfAbsent(key, ignored -> (StatusEffectType) (Object) nativeStatusEffect(key));
    }

    @Override
    public io.github.solclient.client.platform.mc.world.entity.effect.StatusEffect statusEffect(
            StatusEffectType type, int duration, int amplifier) {
        StatusEffect effect = type instanceof StatusEffect
                ? (StatusEffect) (Object) type
                : nativeStatusEffect(type == null ? "SPEED" : type.getName());
        return (io.github.solclient.client.platform.mc.world.entity.effect.StatusEffect) (Object)
                new net.minecraft.entity.effect.StatusEffectInstance(effect.getId(), duration, amplifier);
    }

    private Item nativeItem(String key) {
        Item item = Item.REGISTRY.get(new net.minecraft.util.Identifier(key));
        return item == null ? Item.REGISTRY.get(new net.minecraft.util.Identifier("minecraft:air")) : item;
    }

    private Block nativeBlock(String key) {
        Block block = Block.REGISTRY.get(new net.minecraft.util.Identifier(key));
        return block == null ? Block.REGISTRY.get(new net.minecraft.util.Identifier("minecraft:air")) : block;
    }

    private StatusEffect nativeStatusEffect(String key) {
        if ("STRENGTH".equals(key)) return StatusEffect.STRENGTH;
        if ("BLINDNESS".equals(key)) return StatusEffect.BLINDNESS;
        return StatusEffect.SPEED;
    }

    @Override
    public OptionsScreen createOptionsScreen(Screen parent, Options options) {
        return new LegacyNativeScreenHandle(new net.minecraft.client.gui.screen.SettingsScreen(
                nativeParent(parent), (net.minecraft.client.options.GameOptions) (Object) options));
    }

    @Override
    public SingleplayerScreen createSingleplayerScreen(Screen parent) {
        return new LegacyNativeScreenHandle(new net.minecraft.client.gui.screen.world.SelectWorldScreen(
                nativeParent(parent)));
    }

    @Override
    public MultiplayerScreen createMultiplayerScreen(Screen parent) {
        return new LegacyNativeScreenHandle(new net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen(
                nativeParent(parent)));
    }

    @Override
    public LanguageScreen createLanguageScreen(Screen parent, Options options, LanguageManager languages) {
        return new LegacyNativeScreenHandle(new net.minecraft.client.gui.screen.options.LanguageOptionsScreen(
                nativeParent(parent), (net.minecraft.client.options.GameOptions) (Object) options,
                (net.minecraft.client.resource.language.LanguageManager) (Object) languages));
    }

    private net.minecraft.client.gui.screen.Screen nativeParent(Screen parent) {
        return LegacyNativeScreenHandle.unwrap(parent);
    }

    private static String normalize(String value) {
        if (value == null || value.isEmpty()) return "minecraft:air";
        String key = value.toLowerCase(Locale.ROOT);
        return key.indexOf(':') < 0 ? "minecraft:" + key : key;
    }

    @Override
    public io.github.solclient.client.platform.mc.world.entity.player.GameMode gameMode(String name, int numericId) {
        return new BasicPlatform.BasicGameMode(name.toUpperCase(Locale.ROOT), numericId);
    }

    @Override
    public io.github.solclient.client.platform.mc.raycast.HitType hitType(String name) {
        return new BasicPlatform.BasicHitType(name.toUpperCase(Locale.ROOT));
    }

    @Override
    public PlatformServices.BlockPosFactory blockPos() {
        return (x, y, z) -> (io.github.solclient.client.platform.mc.world.level.block.BlockPos) (Object)
                new net.minecraft.util.math.BlockPos((int) x, (int) y, (int) z);
    }

    @Override
    public Box box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return (Box) (Object) new net.minecraft.util.math.Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void strokeBox(Box box, int red, int green, int blue, int alpha) {
        if (box == null) return;
        net.minecraft.client.render.WorldRenderer.drawBox(
                new net.minecraft.util.math.Box(box.minX(), box.minY(), box.minZ(),
                        box.maxX(), box.maxY(), box.maxZ()), red, green, blue, alpha);
    }

    @Override
    public void strokeLine(io.github.solclient.client.platform.mc.maths.Vec3d start,
                           io.github.solclient.client.platform.mc.maths.Vec3d end,
                           int red, int green, int blue, int alpha) {
        if (start == null || end == null) return;
        net.minecraft.client.render.Tessellator tessellator = net.minecraft.client.render.Tessellator.getInstance();
        net.minecraft.client.render.BufferBuilder builder = tessellator.getBuffer();
        builder.begin(3, net.minecraft.client.render.VertexFormats.POSITION_COLOR);
        builder.vertex(start.x(), start.y(), start.z()).color(0, 0, 255, 255).next();
        builder.vertex(end.x(), end.y(), end.z()).color(red, green, blue, alpha).next();
        tessellator.draw();
    }

    @Override
    public void setBorderlessFullscreen(boolean state) {
        try {
            if (state) {
                if (!borderlessWindowSaved) {
                    borderlessX = org.lwjgl.opengl.Display.getX();
                    borderlessY = org.lwjgl.opengl.Display.getY();
                    borderlessWidth = org.lwjgl.opengl.Display.getWidth();
                    borderlessHeight = org.lwjgl.opengl.Display.getHeight();
                    borderlessWindowSaved = true;
                }
                System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
                org.lwjgl.opengl.Display.setFullscreen(false);
                org.lwjgl.opengl.Display.setResizable(false);
                org.lwjgl.opengl.Display.setDisplayMode(org.lwjgl.opengl.Display.getDesktopDisplayMode());
                org.lwjgl.opengl.Display.setLocation(0, 0);
            } else {
                System.setProperty("org.lwjgl.opengl.Window.undecorated", "false");
                org.lwjgl.opengl.Display.setFullscreen(false);
                org.lwjgl.opengl.Display.setResizable(true);
                if (borderlessWindowSaved) {
                    org.lwjgl.opengl.Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(
                            borderlessWidth, borderlessHeight));
                    org.lwjgl.opengl.Display.setLocation(borderlessX, borderlessY);
                    borderlessWindowSaved = false;
                }
            }
        } catch (org.lwjgl.LWJGLException | IllegalStateException error) {
            System.err.println("[Sol Client] Could not toggle borderless fullscreen: " + error);
        }
    }

    @Override
    public void renderBlockBox(BlockType block, boolean fill) {
        net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
        if (block == null || minecraft.world == null || minecraft.player == null || minecraft.result == null
                || minecraft.result.type != net.minecraft.util.hit.BlockHitResult.Type.BLOCK) return;

        net.minecraft.block.Block nativeBlock = (net.minecraft.block.Block) (Object) block;
        net.minecraft.util.math.BlockPos pos = minecraft.result.getBlockPos();
        net.minecraft.util.math.Box selection = nativeBlock.getSelectionBox(minecraft.world, pos);
        if (selection == null) return;
        float tickDelta = ((io.github.solclient.client.platform.mc.MinecraftClient) (Object) minecraft)
                .getTimer().getTickDelta();
        double x = minecraft.player.prevTickX + (minecraft.player.x - minecraft.player.prevTickX) * tickDelta;
        double y = minecraft.player.prevTickY + (minecraft.player.y - minecraft.player.prevTickY) * tickDelta;
        double z = minecraft.player.prevTickZ + (minecraft.player.z - minecraft.player.prevTickZ) * tickDelta;
        selection = selection.expand(0.002D, 0.002D, 0.002D).offset(-x, -y, -z);

        int colour = PlatformServices.currentColour();
        int red = (colour >> 16) & 0xFF;
        int green = (colour >> 8) & 0xFF;
        int blue = colour & 0xFF;
        int alpha = (colour >> 24) & 0xFF;
        if (fill) drawFilledBox(selection, red, green, blue, alpha);
        else net.minecraft.client.render.WorldRenderer.drawBox(selection, red, green, blue, alpha);
    }

    private static void drawFilledBox(net.minecraft.util.math.Box box, int red, int green, int blue, int alpha) {
        net.minecraft.client.render.Tessellator tessellator = net.minecraft.client.render.Tessellator.getInstance();
        net.minecraft.client.render.BufferBuilder builder = tessellator.getBuffer();
        builder.begin(7, net.minecraft.client.render.VertexFormats.POSITION_COLOR);
        face(builder, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, red, green, blue, alpha);
        face(builder, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
        face(builder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, red, green, blue, alpha);
        face(builder, box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
        face(builder, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ, red, green, blue, alpha);
        face(builder, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
        tessellator.draw();
    }

    private static void face(net.minecraft.client.render.BufferBuilder builder,
                             double x1, double y1, double z1, double x2, double y2, double z2,
                             int red, int green, int blue, int alpha) {
        if (x1 == x2) {
            builder.vertex(x1, y1, z1).color(red, green, blue, alpha).next();
            builder.vertex(x1, y2, z1).color(red, green, blue, alpha).next();
            builder.vertex(x1, y2, z2).color(red, green, blue, alpha).next();
            builder.vertex(x1, y1, z2).color(red, green, blue, alpha).next();
        } else if (y1 == y2) {
            builder.vertex(x1, y1, z1).color(red, green, blue, alpha).next();
            builder.vertex(x2, y1, z1).color(red, green, blue, alpha).next();
            builder.vertex(x2, y1, z2).color(red, green, blue, alpha).next();
            builder.vertex(x1, y1, z2).color(red, green, blue, alpha).next();
        } else {
            builder.vertex(x1, y1, z1).color(red, green, blue, alpha).next();
            builder.vertex(x2, y1, z1).color(red, green, blue, alpha).next();
            builder.vertex(x2, y2, z1).color(red, green, blue, alpha).next();
            builder.vertex(x1, y2, z1).color(red, green, blue, alpha).next();
        }
    }

    @Override
    public io.github.solclient.client.platform.mc.Direction direction(String name) {
        return new BasicPlatform.BasicDirection(name.toUpperCase(Locale.ROOT));
    }

    @Override
    public io.github.solclient.client.platform.mc.world.particle.ParticleType particleType(String name) {
        return new BasicPlatform.BasicParticleType(name.toUpperCase(Locale.ROOT));
    }

    @Override
    public Object environment(String key) {
        switch (key) {
            case "CLASS_LOADER":
                return WrapperClassLoader.INSTANCE;
            case "SODIUM":
                return false;
            case "OPTIFINE":
                return OptiFineTransformer.ACTIVE;
            case "OPTIMISATION_ENGINE":
                return OptiFineTransformer.ACTIVE
                        ? OptimisationEngine.OPTIFINE : OptimisationEngine.VANILLA;
            case "OFFHAND":
                return false;
            case "BLOCKING":
                return true;
            case "LWJGL3":
                return false;
            case "PROPER_PLUGIN_MESSAGE_IDS":
                return false;
            case "TARGET_VERSION":
            case "VERSION_ID":
                return "1.8.9";
            case "MAJOR_RELEASE":
                return 1;
            case "MINOR_RELEASE":
                return 8;
            case "PATCH_RELEASE":
                return 9;
            case "PROTOCOL_VERSION":
                return 47;
            case "MOJANG":
                return "Mojang AB";
            default:
                throw new IllegalArgumentException("Unknown Sol Client environment key: " + key);
        }
    }

    @Override
    public void copyToClipboard(String text) {
        if (MinecraftClient.getInstance().currentScreen != null) {
            net.minecraft.client.gui.screen.Screen.setClipboard(text == null ? "" : text);
        }
    }

    @Override
    public String getClipboardContent() {
        return net.minecraft.client.gui.screen.Screen.getClipboard();
    }

    /**
     * Installs the shared main menu while retaining the vanilla panorama host.
     */
    public void showMainMenu() {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.currentScreen == null || minecraft.currentScreen instanceof TitleScreen) {
            minecraft.openScreen((net.minecraft.client.gui.screen.Screen) (Object) new SolClientMainMenu());
        }
    }

}
