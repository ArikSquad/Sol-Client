package io.github.solclient.client.v26_2;

import io.github.solclient.client.platform.mc.option.ToggleKeyBinding;
import net.minecraft.client.KeyMapping;

import java.util.function.BooleanSupplier;

final class ToggleKeyMappingAdapter extends KeyMappingAdapter implements ToggleKeyBinding {

    private final BooleanSupplier toggleMode;
    private final int maxHoldTime;
    private boolean toggled;
    private boolean previousHeld;
    private int heldTicks;

    ToggleKeyMappingAdapter(KeyMapping mapping, BooleanSupplier toggleMode, int maxHoldTime) {
        super(mapping);
        this.toggleMode = toggleMode;
        this.maxHoldTime = maxHoldTime;
    }

    private void updateToggleState() {
        boolean held = super.isHeld();
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
        return toggleMode.getAsBoolean() ? super.isHeld() : toggled;
    }

    @Override
    public boolean consumePress() {
        updateToggleState();
        return super.consumePress();
    }

    @Override
    public boolean realHeld() {
        return super.isHeld();
    }
}
