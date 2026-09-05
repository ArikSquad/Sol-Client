package io.github.solclient.client.v1_8_9.platform.mc.option;

import io.github.solclient.client.platform.mc.option.KeyBinding;
import io.github.solclient.client.platform.mc.option.ToggleKeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LegacyToggleKeyBinding extends net.minecraft.client.options.KeyBinding
        implements ToggleKeyBinding {

    private final BooleanSupplier toggleMode;
    private final int maxHoldTime;
    private boolean toggled;
    private boolean previousHeld;
    private int heldTicks;

    public LegacyToggleKeyBinding(String name, int initialKey, String category,
                                  BooleanSupplier toggleMode, int maxHoldTime) {
        super(name, initialKey, category);
        this.toggleMode = toggleMode;
        this.maxHoldTime = maxHoldTime;
    }

    @Override
    public String getKeyCategory() {
        return getCategory();
    }

    @Override
    public String getName() {
        return getTranslationKey();
    }

    @Override
    public int getKeyCode() {
        return getCode();
    }

    @Override
    public String getBoundKeyName() {
        return GameOptions.getFormattedNameForKeyCode(getCode());
    }

    private void updateToggleState() {
        boolean held = isPressed();
        if (held && !previousHeld) {
            toggled = !toggled;
            heldTicks = 0;
        }
        if (held && heldTicks < maxHoldTime) {
            heldTicks++;
        }
        if (!held) {
            heldTicks = 0;
        }
        previousHeld = held;
    }

    @Override
    public boolean isHeld() {
        updateToggleState();
        return toggleMode.getAsBoolean() ? isPressed() : toggled;
    }

    @Override
    public boolean consumePress() {
        updateToggleState();
        return wasPressed();
    }

    @Override
    public boolean realHeld() {
        return isPressed();
    }

    private Stream<net.minecraft.client.options.KeyBinding> conflictingStream() {
        return Arrays.stream(MinecraftClient.getInstance().options.keysAll)
                .filter(key -> key != (Object) this && key.getCode() == getCode());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<KeyBinding> getConflictingKeys() {
        if (getCode() == 0) {
            return Collections.emptyList();
        }
        return (List) conflictingStream().collect(Collectors.toList());
    }

    @Override
    public boolean conflicts() {
        return conflictingStream().findAny().isPresent();
    }
}
