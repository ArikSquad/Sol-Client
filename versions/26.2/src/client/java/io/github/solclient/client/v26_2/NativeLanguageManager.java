package io.github.solclient.client.v26_2;

import io.github.solclient.client.platform.mc.lang.LanguageManager;

final class NativeLanguageManager implements LanguageManager {

    private final net.minecraft.client.resources.language.LanguageManager manager;

    NativeLanguageManager(net.minecraft.client.resources.language.LanguageManager manager) {
        this.manager = manager;
    }

    @Override
    public String getCode() {
        return manager.getSelected();
    }
}
