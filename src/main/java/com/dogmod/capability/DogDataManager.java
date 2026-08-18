package com.dogmod.capability;

import com.dogmod.DogMod;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DogDataManager {
    private static final Map<UUID, DogData> DATA_MAP = new HashMap<>();

    public static void register() {
        DogMod.LOGGER.info("DogDataManager hazır");
    }

    public static DogData get(ServerPlayerEntity player) {
        return DATA_MAP.computeIfAbsent(player.getUuid(), uuid -> new DogData());
    }

    public static DogData get(UUID uuid) {
        return DATA_MAP.computeIfAbsent(uuid, u -> new DogData());
    }

    public static void remove(UUID uuid) {
        DATA_MAP.remove(uuid);
    }
}
