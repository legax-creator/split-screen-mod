package com.dogmod.capability;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.server.network.ServerPlayerEntity;

public class DogOriginChecker {

    private static final String[] DOG_ORIGIN_IDS = {
        "dogmod:dog", "moborigins:wolf", "origins:wolf", "extraorigins:wolf"
    };

    public static boolean isDogPlayer(ServerPlayerEntity player) {
        try {
            OriginComponent component = ModComponents.ORIGIN.get(player);
            for (OriginLayer layer : OriginLayers.getLayers()) {
                Origin origin = component.getOrigin(layer);
                if (origin == null) continue;
                String id = origin.getIdentifier().toString();
                for (String dogId : DOG_ORIGIN_IDS) {
                    if (id.equals(dogId)) return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
