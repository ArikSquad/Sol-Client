package io.github.solclient.client.v26_2;

import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.platform.mc.resource.Resource;
import io.github.solclient.client.platform.mc.resource.ResourceManager;

import java.util.ArrayList;
import java.util.List;

final class NativeResourceManager implements ResourceManager {

    private final net.minecraft.server.packs.resources.ResourceManager manager;

    NativeResourceManager(net.minecraft.server.packs.resources.ResourceManager manager) {
        this.manager = manager;
    }

    @Override
    public List<Resource> getResources(Identifier id) {
        List<net.minecraft.server.packs.resources.Resource> nativeResources = manager.getResourceStack(nativeId(id));
        if (nativeResources.isEmpty() && "sol_client".equals(id.namespace())) {
            nativeResources = manager.getResourceStack(legacyNativeId(id));
        }
        List<Resource> result = new ArrayList<>();
        for (net.minecraft.server.packs.resources.Resource resource : nativeResources) {
            result.add(resource::open);
        }
        return result;
    }

    private net.minecraft.resources.Identifier nativeId(Identifier id) {
        String namespace = "sol_client".equals(id.namespace()) ? "sol-client" : id.namespace();
        return net.minecraft.resources.Identifier.fromNamespaceAndPath(namespace, id.path());
    }

    private net.minecraft.resources.Identifier legacyNativeId(Identifier id) {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath("sol_client", id.path());
    }
}
