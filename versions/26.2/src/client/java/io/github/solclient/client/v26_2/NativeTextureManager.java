package io.github.solclient.client.v26_2;

import io.github.solclient.client.platform.PlatformServices;
import io.github.solclient.client.platform.basic.BasicPlatform;
import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.platform.mc.texture.Texture;
import io.github.solclient.client.platform.mc.texture.TextureManager;

import java.util.concurrent.CompletableFuture;

final class NativeTextureManager implements TextureManager {

    private final net.minecraft.client.renderer.texture.TextureManager manager;
    @SuppressWarnings("unused")
    private final FabricPlatform platform;

    NativeTextureManager(net.minecraft.client.renderer.texture.TextureManager manager, FabricPlatform platform) {
        this.manager = manager;
        this.platform = platform;
    }

    @Override
    public CompletableFuture<Texture> download(String url) {
        return CompletableFuture.completedFuture(BasicPlatform.texture(url));
    }

    @Override
    public void bind(Texture texture) {
    }

    @Override
    public void bind(Identifier id) {
        PlatformServices.renderBackend().bindTexture(id);
    }

    @Override
    public void delete(Texture texture) {
    }

    @Override
    public Texture getTexture(Identifier id) {
        return BasicPlatform.texture(id.toString());
    }
}
