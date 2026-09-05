package io.github.solclient.client.v1_21_11;

import io.github.solclient.client.platform.mc.option.KeyBinding;
import net.minecraft.client.KeyMapping;

import java.util.Collections;
import java.util.List;

class KeyMappingAdapter implements KeyBinding {

    protected final KeyMapping mapping;

    KeyMappingAdapter(KeyMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public String getKeyCategory() {
        return mapping.getCategory().id().toString();
    }

    @Override
    public String getName() {
        return mapping.getName();
    }

    @Override
    public int getKeyCode() {
        return mapping.getDefaultKey().getValue();
    }

    @Override
    public String getBoundKeyName() {
        return mapping.getTranslatedKeyMessage().getString();
    }

    @Override
    public boolean isHeld() {
        return mapping.isDown();
    }

    @Override
    public boolean consumePress() {
        return mapping.consumeClick();
    }

    @Override
    public List<KeyBinding> getConflictingKeys() {
        return Collections.emptyList();
    }

    @Override
    public boolean conflicts() {
        return false;
    }

    void setKeyCode(int code) {
        mapping.setKey(com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(code));
    }
}
