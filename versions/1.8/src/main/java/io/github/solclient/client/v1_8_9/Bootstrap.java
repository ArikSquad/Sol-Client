package io.github.solclient.client.v1_8_9;

import io.github.solclient.client.v1_8_9.transformers.ProxyScreenTransformer;
import io.github.solclient.client.wrapper.WrapperClassLoader;
import io.github.solclient.common.ClientBootstrap;
import io.github.solclient.common.ClientPlatform;
import io.github.solclient.common.GameVersions;
import org.spongepowered.asm.mixin.Mixins;

public class Bootstrap {

    public static void init(WrapperClassLoader loader) {
        Mixins.addConfiguration("sol-client-mixins-1.8.9.json");
        loader.preferResourceSource("org.lwjgl.nanovg", "lwjgl-patched");
        loader.preferResourceSource("org.lwjgl.system", "lwjgl-patched");
        loader.preferResourceSource("org.lwjgl3", "lwjgl-patched");
        loader.registerTransformer(new ProxyScreenTransformer());

        LegacyPlatform services = new LegacyPlatform();
        ClientBootstrap.install(GameVersions.LEGACY_1_8, new ClientPlatform() {
            @Override
            public String getName() {
                return "legacy-fabric-1.8.9";
            }

            @Override
            public void log(String message) {
                System.out.println("[Sol Client] " + message);
            }

            @Override
            public void onClientReady(io.github.solclient.common.ClientContext context) {
                services.showMainMenu();
            }
        }, services);
    }

}
