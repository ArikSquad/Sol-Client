package io.github.solclient.client.v26_2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.solclient.client.chat.ChatButton;
import io.github.solclient.client.culling.Cullable;
import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.basic.BasicPlatform;
import io.github.solclient.client.platform.mc.Direction;
import io.github.solclient.client.platform.mc.MinecraftClient;
import io.github.solclient.client.platform.mc.hud.IngameHud;
import io.github.solclient.client.platform.mc.hud.chat.Chat;
import io.github.solclient.client.platform.mc.hud.chat.ChatMessage;
import io.github.solclient.client.platform.mc.maths.Box;
import io.github.solclient.client.platform.mc.maths.Vec3d;
import io.github.solclient.client.platform.mc.network.Connection;
import io.github.solclient.client.platform.mc.network.LocalPlayerState;
import io.github.solclient.client.platform.mc.network.Packet;
import io.github.solclient.client.platform.mc.network.PlayerListEntry;
import io.github.solclient.client.platform.mc.network.ServerData;
import io.github.solclient.client.platform.mc.raycast.HitResult;
import io.github.solclient.client.platform.mc.raycast.HitType;
import io.github.solclient.client.platform.mc.screen.Screen;
import io.github.solclient.client.platform.mc.sound.SoundEngine;
import io.github.solclient.client.platform.mc.sound.SoundInstance;
import io.github.solclient.client.platform.mc.sound.SoundType;
import io.github.solclient.client.platform.mc.text.MutableText;
import io.github.solclient.client.platform.mc.text.OrderedText;
import io.github.solclient.client.platform.mc.text.Text;
import io.github.solclient.client.platform.mc.world.entity.Entity;
import io.github.solclient.client.platform.mc.world.entity.EntityRenderDispatcher;
import io.github.solclient.client.platform.mc.world.entity.LivingEntity;
import io.github.solclient.client.platform.mc.world.entity.LivingEntityType;
import io.github.solclient.client.platform.mc.world.entity.PrimedTnt;
import io.github.solclient.client.platform.mc.world.entity.decoration.ArmourStand;
import io.github.solclient.client.platform.mc.world.entity.effect.StatusEffect;
import io.github.solclient.client.platform.mc.world.entity.effect.StatusEffectType;
import io.github.solclient.client.platform.mc.world.entity.item.ItemEntity;
import io.github.solclient.client.platform.mc.world.entity.player.Abilities;
import io.github.solclient.client.platform.mc.world.entity.player.GameMode;
import io.github.solclient.client.platform.mc.world.entity.player.LocalPlayer;
import io.github.solclient.client.platform.mc.world.entity.player.Player;
import io.github.solclient.client.platform.mc.world.inventory.Inventory;
import io.github.solclient.client.platform.mc.world.item.ItemRenderer;
import io.github.solclient.client.platform.mc.world.item.ItemStack;
import io.github.solclient.client.platform.mc.world.item.ItemType;
import io.github.solclient.client.platform.mc.world.level.ClientLevel;
import io.github.solclient.client.platform.mc.world.level.Level;
import io.github.solclient.client.platform.mc.world.level.LevelRenderer;
import io.github.solclient.client.platform.mc.world.level.WorldBorder;
import io.github.solclient.client.platform.mc.world.level.block.BlockEntity;
import io.github.solclient.client.platform.mc.world.level.block.BlockPos;
import io.github.solclient.client.platform.mc.world.level.block.BlockState;
import io.github.solclient.client.platform.mc.world.level.block.BlockType;
import io.github.solclient.client.platform.mc.world.level.chunk.Chunk;
import io.github.solclient.client.platform.mc.world.level.chunk.ChunkPos;
import io.github.solclient.client.platform.mc.world.particle.ParticleEngine;
import io.github.solclient.client.platform.mc.world.particle.ParticleType;
import io.github.solclient.client.platform.mc.world.scoreboard.Objective;
import io.github.solclient.client.platform.mc.world.scoreboard.PlayerTeam;
import io.github.solclient.client.platform.mc.world.scoreboard.Score;
import io.github.solclient.client.platform.mc.world.scoreboard.Scoreboard;

final class NativeAdapters {

	private NativeAdapters() {}

	static net.minecraft.core.BlockPos nativePos(BlockPos pos) {
		return new net.minecraft.core.BlockPos(pos.x(), pos.y(), pos.z());
	}

	static BlockPos commonPos(net.minecraft.core.BlockPos pos) {
		return BlockPos.create(pos.getX(), pos.getY(), pos.getZ());
	}

	static Vec3d commonVec(net.minecraft.world.phys.Vec3 vec) {
		return Vec3d.create(vec.x, vec.y, vec.z);
	}

	static Box commonBox(net.minecraft.world.phys.AABB box) {
		return Box.create(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
	}

	static String id(net.minecraft.resources.Identifier id) {
		return id == null ? "minecraft:air" : id.toString();
	}

	static String holderId(net.minecraft.core.Holder<?> holder) {
		return holder == null ? "minecraft:air" : holder.unwrapKey()
				.map(key -> key.identifier().toString()).orElse("minecraft:air");
	}

	static MutableText text(net.minecraft.network.chat.Component component) {
		return BasicPlatform.literal(component == null ? "" : component.getString());
	}

	static net.minecraft.network.chat.Component nativeText(Text text) {
		return net.minecraft.network.chat.Component.literal(text == null ? "" : text.getPlain());
	}
}

class NativeEntity implements Entity, Cullable {

	protected final net.minecraft.world.entity.Entity entity;
	protected final FabricPlatform platform;
	private boolean localCulled;

	NativeEntity(net.minecraft.world.entity.Entity entity, FabricPlatform platform) {
		this.entity = entity;
		this.platform = platform;
	}

	static Entity wrap(net.minecraft.world.entity.Entity entity, FabricPlatform platform) {
		if (entity == null) return null;
		if (entity instanceof net.minecraft.client.player.LocalPlayer) return new NativeLocalPlayer(
				(net.minecraft.client.player.LocalPlayer) entity, platform);
		if (entity instanceof net.minecraft.world.entity.player.Player) return new NativePlayer(
				(net.minecraft.world.entity.player.Player) entity, platform);
		if (entity instanceof net.minecraft.world.entity.decoration.ArmorStand) return new NativeArmorStand(
				(net.minecraft.world.entity.decoration.ArmorStand) entity, platform);
		if (entity instanceof net.minecraft.world.entity.item.ItemEntity) return new NativeItemEntity(
				(net.minecraft.world.entity.item.ItemEntity) entity, platform);
		if (entity instanceof net.minecraft.world.entity.item.PrimedTnt) return new NativePrimedTnt(
				(net.minecraft.world.entity.item.PrimedTnt) entity, platform);
		if (entity instanceof net.minecraft.world.entity.LivingEntity) return new NativeLivingEntity(
				(net.minecraft.world.entity.LivingEntity) entity, platform);
		return new NativeEntity(entity, platform);
	}

	static net.minecraft.world.entity.Entity unwrap(Entity entity) {
		return entity instanceof NativeEntity ? ((NativeEntity) entity).entity : null;
	}

	net.minecraft.world.entity.Entity nativeEntity() { return entity; }

	@Override public double x() { return entity.getX(); }
	@Override public double y() { return entity.getY(); }
	@Override public double z() { return entity.getZ(); }
	@Override public double previousX() { return entity.xo; }
	@Override public double previousY() { return entity.yo; }
	@Override public double previousZ() { return entity.zo; }
	@Override public float yaw() { return entity.getYRot(); }
	@Override public float pitch() { return entity.getXRot(); }
	@Override public Vec3d getPosition() { return NativeAdapters.commonVec(entity.position()); }
	@Override public int getNumericId() { return entity.getId(); }
	@Override public UUID getId() { return entity.getUUID(); }
	@Override public Box getBounds() { return NativeAdapters.commonBox(entity.getBoundingBox()); }
	@Override public boolean isGloballyInvisible() { return entity.isInvisible(); }
	@Override public boolean isInvisibleTo(Player player) {
		net.minecraft.world.entity.Entity nativePlayer = NativeEntity.unwrap(player);
		return nativePlayer instanceof net.minecraft.world.entity.player.Player
				&& entity.isInvisibleTo((net.minecraft.world.entity.player.Player) nativePlayer);
	}
	@Override public Vec3d getEyePosition() { return NativeAdapters.commonVec(entity.getEyePosition()); }
	@Override public Vec3d getEyePosition(float tickDelta) {
		return NativeAdapters.commonVec(entity.getEyePosition(tickDelta));
	}
	@Override public float getEntityWidth() { return entity.getBbWidth(); }
	@Override public float getEntityEyeHeight() { return entity.getEyeHeight(); }
	@Override public Vec3d getView(float tickDelta) {
		return NativeAdapters.commonVec(entity.getViewVector(tickDelta));
	}
	@Override public int getChunkX() { return entity.getBlockX() >> 4; }
	@Override public int getChunkZ() { return entity.getBlockZ() >> 4; }
	@Override public ChunkPos getChunkPos() { return ChunkPos.create(getChunkX(), getChunkZ()); }
	@Override public boolean isPassenger() { return entity.isPassenger(); }
	@Override public boolean isEntityOnGround() { return entity.onGround(); }
	@Override public boolean isInWeb() { return false; }
	@Override public boolean isInWater() { return entity.isInWater(); }
	@Override public boolean isEntitySprinting() { return entity.isSprinting(); }
	@Override public float getFallDistance() { return (float) entity.fallDistance; }
	@Override public boolean isCulled() {
		return entity instanceof Cullable ? ((Cullable) (Object) entity).isCulled() : localCulled;
	}
	@Override public void setCulled(boolean culled) {
		if (entity instanceof Cullable) ((Cullable) (Object) entity).setCulled(culled);
		localCulled = culled;
	}
}

class NativeLivingEntity extends NativeEntity implements LivingEntity {

	NativeLivingEntity(net.minecraft.world.entity.LivingEntity entity, FabricPlatform platform) {
		super(entity, platform);
	}

	private net.minecraft.world.entity.LivingEntity living() {
		return (net.minecraft.world.entity.LivingEntity) entity;
	}

	@Override public Collection<StatusEffect> getEntityStatusEffects() {
		List<StatusEffect> effects = new ArrayList<>();
		for (net.minecraft.world.effect.MobEffectInstance effect : living().getActiveEffects()) {
			effects.add(new NativeStatusEffect(effect, platform));
		}
		return effects;
	}

	@Override public boolean hasStatusEffect(StatusEffectType effect) {
		net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> holder =
				NativeStatusEffectType.resolve(platform, effect == null ? "" : effect.getName());
		return holder != null && living().hasEffect(holder);
	}

	@Override public boolean isEntityClimbing() { return living().onClimbable(); }
	@Override public ItemStack getMainHandItem() { return new NativeItemStack(living().getMainHandItem(), platform); }
	@Override public LivingEntityType getLivingEntityType() { return NativeLivingEntityType.INSTANCE; }
	@Override public boolean isEntityUsingItem() { return living().isUsingItem(); }
	@Override public int getItemUsageRemaining() { return living().getUseItemRemainingTicks(); }
}

class NativePlayer extends NativeLivingEntity implements Player {

	private final net.minecraft.world.entity.player.Player player;
	private final NativeInventory inventory;
	private final NativeAbilities abilities;

	NativePlayer(net.minecraft.world.entity.player.Player player, FabricPlatform platform) {
		super(player, platform);
		this.player = player;
		this.inventory = new NativeInventory(player, platform);
		this.abilities = new NativeAbilities(player);
	}

	@Override public Inventory getInventory() { return inventory; }
	@Override public Abilities getAbilities() { return abilities; }
	net.minecraft.world.entity.player.Player nativePlayer() { return player; }
}

class NativeLocalPlayer extends NativePlayer implements LocalPlayer {

	private final net.minecraft.client.player.LocalPlayer local;

	NativeLocalPlayer(net.minecraft.client.player.LocalPlayer player, FabricPlatform platform) {
		super(player, platform);
		this.local = player;
	}

	@Override public Connection getConnection() { return new NativeConnection(local.connection, platform); }
	@Override public void sendSystemMessage(String text) {
		local.sendSystemMessage(net.minecraft.network.chat.Component.literal(text));
	}
	@Override public void sendSystemMessage(Text text) {
		local.sendSystemMessage(net.minecraft.network.chat.Component.literal(text == null ? "" : text.getPlain()));
	}
	@Override public void chat(String text) {
		if (text == null) return;
		if (text.startsWith("/")) local.connection.sendCommand(text.substring(1));
		else local.connection.sendChat(text);
	}
	@Override public boolean isSpectatorMode() { return local.isSpectator(); }
	@Override public void clientSwing() { local.swing(net.minecraft.world.InteractionHand.MAIN_HAND); }
	net.minecraft.client.player.LocalPlayer nativeLocalPlayer() { return local; }
}

class NativeArmorStand extends NativeLivingEntity implements ArmourStand {
	NativeArmorStand(net.minecraft.world.entity.decoration.ArmorStand entity, FabricPlatform platform) {
		super(entity, platform);
	}
	@Override public boolean isMarker() { return ((net.minecraft.world.entity.decoration.ArmorStand) entity).isMarker(); }
}

class NativeItemEntity extends NativeEntity implements ItemEntity {
	NativeItemEntity(net.minecraft.world.entity.item.ItemEntity entity, FabricPlatform platform) {
		super(entity, platform);
	}
	@Override public ItemStack getItem() {
		return new NativeItemStack(((net.minecraft.world.entity.item.ItemEntity) entity).getItem(), platform);
	}
}

class NativePrimedTnt extends NativeEntity implements PrimedTnt {
	NativePrimedTnt(net.minecraft.world.entity.item.PrimedTnt entity, FabricPlatform platform) {
		super(entity, platform);
	}
	@Override public float getFuse() { return ((net.minecraft.world.entity.item.PrimedTnt) entity).getFuse(); }
}

final class NativeLivingEntityType implements LivingEntityType {
	static final NativeLivingEntityType INSTANCE = new NativeLivingEntityType();
	private NativeLivingEntityType() {}
}

final class NativeAbilities implements Abilities {
	private final net.minecraft.world.entity.player.Player player;
	NativeAbilities(net.minecraft.world.entity.player.Player player) { this.player = player; }
	@Override public boolean canBuild() { return player.getAbilities().mayBuild; }
}

class NativeInventory implements Inventory {
	private final net.minecraft.world.entity.player.Player player;
	private final net.minecraft.world.entity.player.Inventory inventory;
	private final FabricPlatform platform;

	NativeInventory(net.minecraft.world.entity.player.Player player, FabricPlatform platform) {
		this.player = player;
		this.inventory = player.getInventory();
		this.platform = platform;
	}

	private ItemStack stack(net.minecraft.world.item.ItemStack stack) {
		return stack == null || stack.isEmpty() ? null : new NativeItemStack(stack, platform);
	}

	@Override public ItemStack getArmour(int slot) {
		if (slot < 0 || slot > 3) return null;
		net.minecraft.world.entity.EquipmentSlot[] slots = {
			net.minecraft.world.entity.EquipmentSlot.FEET,
			net.minecraft.world.entity.EquipmentSlot.LEGS,
			net.minecraft.world.entity.EquipmentSlot.CHEST,
			net.minecraft.world.entity.EquipmentSlot.HEAD
		};
		return stack(player.getItemBySlot(slots[slot]));
	}

	@Override public ItemStack getMainHand() { return new NativeItemStack(player.getMainHandItem(), platform); }
	@Override public ItemStack getItem(int slot) {
		if (slot < 0 || slot >= inventory.getContainerSize()) return null;
		return stack(inventory.getItem(slot));
	}
}

class NativeItemType implements ItemType {
	final net.minecraft.world.item.Item item;
	final FabricPlatform platform;

	NativeItemType(net.minecraft.world.item.Item item, FabricPlatform platform) {
		this.item = item;
		this.platform = platform;
	}

	@Override public String toString() {
		return NativeAdapters.id(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item));
	}
}

class NativeItemStack implements ItemStack {
	final net.minecraft.world.item.ItemStack stack;
	final FabricPlatform platform;

	NativeItemStack(net.minecraft.world.item.ItemStack stack, FabricPlatform platform) {
		this.stack = stack == null ? net.minecraft.world.item.ItemStack.EMPTY : stack;
		this.platform = platform;
	}

	static net.minecraft.world.item.ItemStack unwrap(ItemStack stack) {
		return stack instanceof NativeItemStack ? ((NativeItemStack) stack).stack : net.minecraft.world.item.ItemStack.EMPTY;
	}

	@Override public int getQuantity() { return stack.getCount(); }
	@Override public void setQuantity(int quantity) { stack.setCount(quantity); }
	@Override public ItemType getType() { return platform.itemType(stack.getItem()); }
	@Override public int getDamageValue() { return stack.getDamageValue(); }
	@Override public int getMaxDamageValue() { return stack.getMaxDamage(); }
	@Override public Text getDisplayName() { return NativeAdapters.text(stack.getHoverName()); }
	@Override public boolean canDestroy(Level level, BlockPos pos) {
		if (!(level instanceof NativeLevel) || pos == null) return false;
		NativeLevel nativeLevel = (NativeLevel) level;
		return stack.canDestroyBlock(nativeLevel.level.getBlockState(NativeAdapters.nativePos(pos)),
				nativeLevel.level, NativeAdapters.nativePos(pos), platform.clientAdapter().nativePlayer());
	}
	@Override public boolean canPlaceOn(Level level, BlockPos pos) {
		if (!(level instanceof NativeLevel) || pos == null) return false;
		NativeLevel nativeLevel = (NativeLevel) level;
		return stack.canPlaceOnBlockInAdventureMode(new net.minecraft.world.level.block.state.pattern.BlockInWorld(
				nativeLevel.level, NativeAdapters.nativePos(pos), true));
	}
	@Override public int getMaxItemUseTime() {
		try { return stack.getUseDuration(null); }
		catch (RuntimeException ignored) { return 0; }
	}
}

class NativeBlockType implements BlockType {
	final net.minecraft.world.level.block.Block block;
	final FabricPlatform platform;

	NativeBlockType(net.minecraft.world.level.block.Block block, FabricPlatform platform) {
		this.block = block;
		this.platform = platform;
	}

	@Override public io.github.solclient.client.platform.mc.resource.Identifier getId() {
		return platform.parseIdentifier(NativeAdapters.id(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block)));
	}
	@Override public ItemType toItem() { return platform.itemType(block.asItem()); }
	@Override public void fillBox() { platform.renderBlockBox(this, true); }
	@Override public void strokeBox() { platform.renderBlockBox(this, false); }
	@Override public String toString() { return getId().toString(); }
}

class NativeBlockState implements BlockState {
	final net.minecraft.world.level.block.state.BlockState state;
	final FabricPlatform platform;
	NativeBlockState(net.minecraft.world.level.block.state.BlockState state, FabricPlatform platform) {
		this.state = state;
		this.platform = platform;
	}
	@Override public BlockType getType() { return platform.blockType(state.getBlock()); }
	@Override public boolean hasMenu(Level level, BlockPos pos) { return state.hasBlockEntity(); }
}

class NativeBlockEntity implements BlockEntity, Cullable {
	final net.minecraft.world.level.block.entity.BlockEntity entity;
	final FabricPlatform platform;
	private boolean localCulled;
	NativeBlockEntity(net.minecraft.world.level.block.entity.BlockEntity entity, FabricPlatform platform) {
		this.entity = entity;
		this.platform = platform;
	}
	@Override public BlockType getBlockType() { return platform.blockType(entity.getBlockState().getBlock()); }
	@Override public boolean isCulled() {
		return entity instanceof Cullable ? ((Cullable) (Object) entity).isCulled() : localCulled;
	}
	@Override public void setCulled(boolean culled) {
		if (entity instanceof Cullable) ((Cullable) (Object) entity).setCulled(culled);
		localCulled = culled;
	}
}

class NativeChunk implements Chunk {
	final net.minecraft.world.level.chunk.LevelChunk chunk;
	final FabricPlatform platform;
	NativeChunk(net.minecraft.world.level.chunk.LevelChunk chunk, FabricPlatform platform) {
		this.chunk = chunk;
		this.platform = platform;
	}
	@Override public Map<BlockPos, BlockEntity> getBlockEntityMap() {
		Map<BlockPos, BlockEntity> result = new HashMap<>();
		for (Map.Entry<net.minecraft.core.BlockPos, net.minecraft.world.level.block.entity.BlockEntity> entry
				: chunk.getBlockEntities().entrySet()) {
			result.put(NativeAdapters.commonPos(entry.getKey()), new NativeBlockEntity(entry.getValue(), platform));
		}
		return result;
	}
}

class NativeWorldBorder implements WorldBorder {
	final net.minecraft.world.level.border.WorldBorder border;
	NativeWorldBorder(net.minecraft.world.level.border.WorldBorder border) { this.border = border; }
	@Override public boolean contains(BlockPos pos) { return border != null && border.isWithinBounds(NativeAdapters.nativePos(pos)); }
}

class NativeLevel implements Level {
	final net.minecraft.world.level.Level level;
	final FabricPlatform platform;
	NativeLevel(net.minecraft.world.level.Level level, FabricPlatform platform) {
		this.level = level;
		this.platform = platform;
	}
	@Override public boolean isOpaqueFullCube(BlockPos pos) {
		net.minecraft.core.BlockPos nativePos = NativeAdapters.nativePos(pos);
		net.minecraft.world.level.block.state.BlockState state = level.getBlockState(nativePos);
		return state.isCollisionShapeFullBlock(level, nativePos);
	}
	@Override public BlockState getBlockState(BlockPos pos) {
		return new NativeBlockState(level.getBlockState(NativeAdapters.nativePos(pos)), platform);
	}
	@Override public Chunk getChunk(ChunkPos pos) { return getChunk(pos.x(), pos.z()); }
	@Override public Chunk getChunk(int x, int z) { return new NativeChunk(level.getChunk(x, z), platform); }
	@Override public WorldBorder getWorldBorder() { return new NativeWorldBorder(level.getWorldBorder()); }
	@Override public Text getScoreboardTitle() {
		net.minecraft.world.scores.Objective objective = level.getScoreboard()
				.getDisplayObjective(net.minecraft.world.scores.DisplaySlot.SIDEBAR);
		return objective == null ? null : NativeAdapters.text(objective.getDisplayName());
	}
}

class NativeClientLevel extends NativeLevel implements ClientLevel {
	private final net.minecraft.client.multiplayer.ClientLevel clientLevel;
	NativeClientLevel(net.minecraft.client.multiplayer.ClientLevel level, FabricPlatform platform) {
		super(level, platform);
		this.clientLevel = level;
	}
	@Override public Iterable<Entity> getRenderedEntities() {
		List<Entity> result = new ArrayList<>();
		for (net.minecraft.world.entity.Entity entity : clientLevel.entitiesForRendering()) {
			result.add(NativeEntity.wrap(entity, platform));
		}
		return result;
	}
}

class NativeHitResult implements HitResult {
	final net.minecraft.world.phys.HitResult result;
	final FabricPlatform platform;
	NativeHitResult(net.minecraft.world.phys.HitResult result, FabricPlatform platform) {
		this.result = result;
		this.platform = platform;
	}
	@Override public HitType getType() {
		if (result == null) return HitType.MISS;
		switch (result.getType()) {
			case BLOCK: return HitType.BLOCK;
			case ENTITY: return HitType.ENTITY;
			default: return HitType.MISS;
		}
	}
	@Override public Entity getEntity() {
		return result instanceof net.minecraft.world.phys.EntityHitResult
				? NativeEntity.wrap(((net.minecraft.world.phys.EntityHitResult) result).getEntity(), platform) : null;
	}
	@Override public BlockPos getBlockPos() {
		return result instanceof net.minecraft.world.phys.BlockHitResult
				? NativeAdapters.commonPos(((net.minecraft.world.phys.BlockHitResult) result).getBlockPos()) : null;
	}
	@Override public Direction getBlockSide() {
		return result instanceof net.minecraft.world.phys.BlockHitResult
				? PlatformServices.get().direction(((net.minecraft.world.phys.BlockHitResult) result).getDirection().name()) : null;
	}
}

class NativePlayerState implements LocalPlayerState {
	private final net.minecraft.world.level.GameType gameType;
	NativePlayerState(net.minecraft.world.level.GameType gameType) { this.gameType = gameType; }
	@Override public GameMode getGameMode() { return PlatformServices.get().gameMode(gameType.getName().toUpperCase(), gameType.getId()); }
}

class NativeLevelRenderer implements LevelRenderer {
	private final net.minecraft.client.Minecraft minecraft;
	NativeLevelRenderer(net.minecraft.client.Minecraft minecraft) { this.minecraft = minecraft; }
	@Override public void scheduleUpdate() {
		if (minecraft.level != null && minecraft.player != null) {
			net.minecraft.core.BlockPos pos = minecraft.player.blockPosition();
			minecraft.levelRenderer.clearVisibleSections();
		}
	}
}

class NativeItemRenderer implements ItemRenderer {
	@Override public void render(ItemStack item, int x, int y) {
		if (item instanceof NativeItemStack) {
			PlatformServices.renderBackend().renderItem(((NativeItemStack) item).stack, x, y);
			PlatformServices.renderBackend().renderItemDecorations(((NativeItemStack) item).stack, x, y);
		}
	}
}

class NativeEntityRenderDispatcher implements EntityRenderDispatcher {
	private boolean hitboxes;
	@Override public void setHitboxes(boolean hitboxes) { this.hitboxes = hitboxes; }
	@Override public boolean getHitboxes() { return hitboxes; }
}

class NativeTimer implements io.github.solclient.client.platform.mc.Timer {
	private final net.minecraft.client.Minecraft minecraft;
	NativeTimer(net.minecraft.client.Minecraft minecraft) { this.minecraft = minecraft; }
	@Override public float getTickDelta() { return minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false); }
}

class NativeMouseHandler implements io.github.solclient.client.platform.mc.MouseHandler {
	private final net.minecraft.client.Minecraft minecraft;
	NativeMouseHandler(net.minecraft.client.Minecraft minecraft) { this.minecraft = minecraft; }
	@Override public void grabCursor() { minecraft.mouseHandler.grabMouse(); }
}

class NativeParticleEngine implements ParticleEngine {
	private final net.minecraft.client.Minecraft minecraft;
	NativeParticleEngine(net.minecraft.client.Minecraft minecraft) { this.minecraft = minecraft; }
	private net.minecraft.core.particles.ParticleOptions particle(ParticleType type) {
		String name = String.valueOf(type).toUpperCase();
		switch (name) {
			case "CRIT": return net.minecraft.core.particles.ParticleTypes.CRIT;
			case "MAGIC": return net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT;
			case "SNOWBALL": return net.minecraft.core.particles.ParticleTypes.ITEM_SNOWBALL;
			case "SLIME": return net.minecraft.core.particles.ParticleTypes.ITEM_SLIME;
			case "FLAME": return net.minecraft.core.particles.ParticleTypes.FLAME;
			default: return net.minecraft.core.particles.ParticleTypes.CRIT;
		}
	}
	@Override public void emit(Entity entity, ParticleType particle) {
		net.minecraft.world.entity.Entity nativeEntity = NativeEntity.unwrap(entity);
		if (nativeEntity != null) minecraft.particleEngine.createTrackingEmitter(nativeEntity, particle(particle));
	}
	@Override public void emitDestruction(BlockPos pos, Direction side) {
		if (minecraft.level != null && pos != null) {
			net.minecraft.core.BlockPos nativePos = NativeAdapters.nativePos(pos);
			minecraft.level.addDestroyBlockEffect(nativePos, minecraft.level.getBlockState(nativePos));
		}
	}
}

class NativeHud implements IngameHud {
	private final net.minecraft.client.gui.Gui gui;
	private final NativeChat chat;
	NativeHud(net.minecraft.client.gui.Gui gui, FabricPlatform platform) {
		this.gui = gui;
		this.chat = new NativeChat(gui.hud.getChat(), platform);
	}
	@Override public Chat getChat() { return chat; }
	@Override public int getTickCounter() { return gui.hud.getGuiTicks(); }
}

class NativeChat implements Chat {
	private final net.minecraft.client.gui.components.ChatComponent chat;
	private final FabricPlatform platform;
	private final Map<net.minecraft.client.multiplayer.chat.GuiMessage, NativeChatMessage> messageViews = new IdentityHashMap<>();
	private ChatButton selected;
	NativeChat(net.minecraft.client.gui.components.ChatComponent chat, FabricPlatform platform) {
		this.chat = chat;
		this.platform = platform;
	}
	@Override public void resetChat() { chat.clearMessages(true); messageViews.clear(); }
	@Override public void scrollToStart() { chat.resetChatScroll(); }
	@Override public int getLineCount() { return chat.getLinesPerPage(); }
	@Override public List<ChatMessage> getVisibleMessages() {
		List<ChatMessage> result = new ArrayList<>();
		List<net.minecraft.client.multiplayer.chat.GuiMessage> messages = ((io.github.solclient.client.v26_2.mixins.ChatComponentAccessor) (Object) chat)
				.solClient$allMessages();
		for (int index = messages.size() - 1; index >= 0; index--) {
			net.minecraft.client.multiplayer.chat.GuiMessage message = messages.get(index);
			result.add(messageViews.computeIfAbsent(message, NativeChatMessage::new));
		}
		return result;
	}
	@Override public boolean isOpen() {
		return chat.isChatFocused() || platform.nativeClient().gui.screen() instanceof net.minecraft.client.gui.screens.ChatScreen;
	}
	@Override public int getChatWidth() { return net.minecraft.client.gui.components.ChatComponent.getWidth(1.0); }
	@Override public int getScroll() {
		return ((io.github.solclient.client.v26_2.mixins.ChatComponentAccessor) (Object) chat)
				.solClient$scrollbarPosition();
	}
	@Override public void scrollChat(int amount) { chat.scrollChat(amount); }
	@Override public boolean isScrolled() { return getScroll() > 0; }
	@Override public void addMessage(String text) { chat.addClientSystemMessage(net.minecraft.network.chat.Component.literal(text)); }
	@Override public void addMessage(Text text) { chat.addClientSystemMessage(net.minecraft.network.chat.Component.literal(text == null ? "" : text.getPlain())); }
	@Override public void setSelectedButton(ChatButton button) { selected = button; }
	@Override public ChatButton getSelectedButton() { return selected; }
}

final class NativeChatMessage implements ChatMessage, io.github.solclient.client.mod.impl.hud.chat.ChatAnimationData {
	private final int creationTick;
	private final OrderedText message;
	private float transparency = 1;
	private float lastTransparency = 1;

	NativeChatMessage(net.minecraft.client.multiplayer.chat.GuiMessage message) {
		creationTick = message.addedTime();
		this.message = (OrderedText) (Object) BasicPlatform.literal(message.content().getString());
	}

	@Override public int getMessageCreationTick() { return creationTick; }
	@Override public OrderedText getMessage() { return message; }
	@Override public float getTransparency() { return transparency; }
	@Override public void setTransparency(float transparency) { this.transparency = transparency; }
	@Override public float getLastTransparency() { return lastTransparency; }
	@Override public void setLastTransparency(float transparency) { this.lastTransparency = transparency; }
}

class NativeConnection implements Connection {
	private final net.minecraft.client.multiplayer.ClientPacketListener listener;
	private final FabricPlatform platform;
	NativeConnection(net.minecraft.client.multiplayer.ClientPacketListener listener, FabricPlatform platform) {
		this.listener = listener;
		this.platform = platform;
	}
	@Override public void sendPacket(Packet packet) {
		if (listener != null && packet instanceof io.github.solclient.client.platform.mc.network.C2SChatMessagePacket) {
			String message = ((io.github.solclient.client.platform.mc.network.C2SChatMessagePacket) packet).message();
			if (message.startsWith("/")) listener.sendCommand(message.substring(1));
			else listener.sendChat(message);
		}
	}
	@Override public PlayerListEntry getPlayerListEntry(UUID uuid) {
		net.minecraft.client.multiplayer.PlayerInfo info = listener == null ? null : listener.getPlayerInfo(uuid);
		return info == null ? null : new NativePlayerListEntry(info);
	}
}

class NativePlayerListEntry implements PlayerListEntry {
	private final net.minecraft.client.multiplayer.PlayerInfo info;
	NativePlayerListEntry(net.minecraft.client.multiplayer.PlayerInfo info) { this.info = info; }
	@Override public int getPing() { return info.getLatency(); }
}

class NativeServerData implements ServerData {
	private final net.minecraft.client.multiplayer.ServerData data;
	NativeServerData(net.minecraft.client.multiplayer.ServerData data) { this.data = data; }
	@Override public String getName() { return data == null ? null : data.name; }
	@Override public String getIp() { return data == null ? "" : data.ip; }
	@Override public CompletableFuture<Integer> ping() {
		return CompletableFuture.completedFuture(data == null ? 0 : (int) data.ping);
	}
}

class NativeScoreboard implements Scoreboard {
	private final net.minecraft.world.scores.Scoreboard scoreboard;
	private final FabricPlatform platform;
	NativeScoreboard(net.minecraft.world.scores.Scoreboard scoreboard, FabricPlatform platform) {
		this.scoreboard = scoreboard;
		this.platform = platform;
	}
	@Override public Collection<Score> getScores(Objective objective) {
		if (!(objective instanceof NativeObjective)) return Collections.emptyList();
		List<Score> result = new ArrayList<>();
		for (net.minecraft.world.scores.PlayerScoreEntry score : scoreboard.listPlayerScores(
				((NativeObjective) objective).objective)) result.add(new NativeScore(score));
		return result;
	}
	@Override public PlayerTeam getPlayersTeam(String playerName) {
		net.minecraft.world.scores.PlayerTeam team = scoreboard.getPlayersTeam(playerName);
		return new NativePlayerTeam(team);
	}
}

class NativeObjective implements Objective {
	final net.minecraft.world.scores.Objective objective;
	final FabricPlatform platform;
	NativeObjective(net.minecraft.world.scores.Objective objective, FabricPlatform platform) {
		this.objective = objective;
		this.platform = platform;
	}
	@Override public Scoreboard getScoreboard() { return new NativeScoreboard(objective.getScoreboard(), platform); }
	@Override public Text getDisplayName() { return NativeAdapters.text(objective.getDisplayName()); }
}

class NativeScore implements Score {
	private final net.minecraft.world.scores.PlayerScoreEntry score;
	NativeScore(net.minecraft.world.scores.PlayerScoreEntry score) { this.score = score; }
	@Override public String getOwner() { return score.owner(); }
	@Override public int getValue() { return score.value(); }
	@Override public String getPlayerName() { return score.owner(); }
}

class NativePlayerTeam implements PlayerTeam {
	private final net.minecraft.world.scores.PlayerTeam team;
	NativePlayerTeam(net.minecraft.world.scores.PlayerTeam team) { this.team = team; }
	@Override public Text formatText(String text, boolean numbers) {
		return NativeAdapters.text(team == null ? net.minecraft.network.chat.Component.literal(text)
				: team.getFormattedName(net.minecraft.network.chat.Component.literal(text)));
	}
}

class NativeStatusEffectType implements StatusEffectType {
	final net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> holder;
	final FabricPlatform platform;
	NativeStatusEffectType(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> holder, FabricPlatform platform) {
		this.holder = holder;
		this.platform = platform;
	}
	static net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> resolve(FabricPlatform platform, String name) {
		if (name == null) return null;
		String normalized = name.toLowerCase().contains(":") ? name.toLowerCase() : "minecraft:" + name.toLowerCase();
		return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(
				platform.nativeIdentifier(platform.parseIdentifier(normalized))).orElse(null);
	}
	@Override public void render(int x, int y) {}
	@Override public String getName() { return NativeAdapters.holderId(holder); }
}

class NativeStatusEffect implements StatusEffect {
	private final net.minecraft.world.effect.MobEffectInstance effect;
	private final FabricPlatform platform;
	NativeStatusEffect(net.minecraft.world.effect.MobEffectInstance effect, FabricPlatform platform) {
		this.effect = effect;
		this.platform = platform;
	}
	@Override public StatusEffectType getType() { return platform.statusEffectType(effect.getEffect()); }
	@Override public int getEffectDuration() { return effect.getDuration(); }
	@Override public String getDurationText() { return effect.isInfiniteDuration() ? "∞" : effect.getDuration() / 20 + "s"; }
	@Override public int getEffectAmplifier() { return effect.getAmplifier(); }
	@Override public boolean showIcon() { return effect.showIcon(); }
	@Override public boolean showAmplifier() { return effect.getAmplifier() > 0; }
	@Override public String getAmplifierName() { return Integer.toString(effect.getAmplifier() + 1); }
}

class NativeSoundType implements SoundType {
	final net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound;
	NativeSoundType(net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound) { this.sound = sound; }
	@Override public io.github.solclient.client.platform.mc.resource.Identifier getId() {
		return PlatformServices.get().parseIdentifier(NativeAdapters.holderId(sound));
	}
}

class NativeSoundInstance implements SoundInstance {
	final net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound;
	final float pitch;
	NativeSoundInstance(net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound, float pitch) {
		this.sound = sound;
		this.pitch = pitch;
	}
}

class NativeSoundEngine implements SoundEngine {
	private final net.minecraft.client.Minecraft minecraft;
	NativeSoundEngine(net.minecraft.client.Minecraft minecraft) { this.minecraft = minecraft; }
	@Override public void play(SoundInstance sound) {
		if (sound instanceof NativeSoundInstance) {
			NativeSoundInstance nativeSound = (NativeSoundInstance) sound;
			minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
					nativeSound.sound, nativeSound.pitch));
		}
	}
}
