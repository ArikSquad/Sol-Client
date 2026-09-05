package io.github.solclient.client.v1_21_11;

import io.github.solclient.client.platform.mc.option.KeyBinding;
import io.github.solclient.client.platform.mc.option.Options;
import io.github.solclient.client.platform.mc.option.Perspective;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;

import java.util.ArrayList;
import java.util.List;

final class NativeOptions implements Options {

    private final net.minecraft.client.Options options;
    private final net.minecraft.client.Minecraft minecraft;
    private final List<KeyBinding> extraKeys = new ArrayList<>();
    private KeyBinding sprintOverride;
    private KeyBinding sneakOverride;

    NativeOptions(net.minecraft.client.Options options, net.minecraft.client.Minecraft minecraft) {
        this.options = options;
        this.minecraft = minecraft;
    }

    private static KeyMappingAdapter key(KeyMapping mapping) {
        return new KeyMappingAdapter(mapping);
    }

    @Override
    public double mouseSensitivity() {
        return options.sensitivity().get();
    }

    @Override
    public void setMouseSensitivity(double sensitivity) {
        options.sensitivity().set(sensitivity);
    }

    @Override
    public boolean invertMouse() {
        return options.invertMouseY().get();
    }

    @Override
    public boolean debugOverlay() {
        return false;
    }

    @Override
    public Perspective perspective() {
        return io.github.solclient.client.platform.basic.BasicPlatform.perspective(options.getCameraType().name());
    }

    @Override
    public int ordinalPerspective() {
        return options.getCameraType().ordinal();
    }

    @Override
    public void setPerspective(Perspective perspective) {
        setOrdinalPerspective(perspective.enumOrdinal());
    }

    @Override
    public void setOrdinalPerspective(int perspective) {
        CameraType[] values = CameraType.values();
        if (perspective >= 0 && perspective < values.length) options.setCameraType(values[perspective]);
    }

    @Override
    public KeyBinding[] keys() {
        List<KeyBinding> keys = new ArrayList<>();
        for (KeyMapping mapping : options.keyMappings) {
            if (sprintOverride != null && mapping == options.keySprint) continue;
            if (sneakOverride != null && mapping == options.keyShift) continue;
            keys.add(key(mapping));
        }
        keys.addAll(extraKeys);
        return keys.toArray(new KeyBinding[0]);
    }

    @Override
    public KeyBinding forwardsKey() {
        return key(options.keyUp);
    }

    @Override
    public KeyBinding backwardsKey() {
        return key(options.keyDown);
    }

    @Override
    public KeyBinding strafeLeftKey() {
        return key(options.keyLeft);
    }

    @Override
    public KeyBinding strafeRightKey() {
        return key(options.keyRight);
    }

    @Override
    public KeyBinding attackKey() {
        return key(options.keyAttack);
    }

    @Override
    public KeyBinding useKey() {
        return key(options.keyUse);
    }

    @Override
    public KeyBinding jumpKey() {
        return key(options.keyJump);
    }

    @Override
    public KeyBinding sprintKey() {
        return sprintOverride == null ? key(options.keySprint) : sprintOverride;
    }

    @Override
    public void overwriteSprintKey(KeyBinding sprint) {
        sprintOverride = sprint;
    }

    @Override
    public KeyBinding sneakKey() {
        return sneakOverride == null ? key(options.keyShift) : sneakOverride;
    }

    @Override
    public void overwriteSneakKey(KeyBinding sneak) {
        sneakOverride = sneak;
    }

    @Override
    public boolean hideGui() {
        return options.hideGui;
    }

    @Override
    public void addKey(KeyBinding key) {
        if (!extraKeys.contains(key)) extraKeys.add(key);
    }

    @Override
    public void removeKey(KeyBinding key) {
        extraKeys.remove(key);
        if (key == sprintOverride) sprintOverride = null;
        if (key == sneakOverride) sneakOverride = null;
    }

    @Override
    public boolean smoothCamera() {
        return options.smoothCamera;
    }

    @Override
    public void setSmoothCamera(boolean camera) {
        options.smoothCamera = camera;
    }

    @Override
    public void setMouseButton(KeyBinding binding, int button) {
        if (binding instanceof KeyMappingAdapter) {
            ((KeyMappingAdapter) binding).mapping.setKey(com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(button));
        }
    }

    @Override
    public void setKey(KeyBinding binding, int code, int scancode) {
        if (binding instanceof KeyMappingAdapter) ((KeyMappingAdapter) binding).setKeyCode(code);
    }

    @Override
    public void unbindKey(KeyBinding binding) {
        if (binding instanceof KeyMappingAdapter) ((KeyMappingAdapter) binding).setKeyCode(-1);
    }

    @Override
    public void saveFile() {
        options.save();
    }

    @Override
    public String languageCode() {
        return options.languageCode;
    }
}
