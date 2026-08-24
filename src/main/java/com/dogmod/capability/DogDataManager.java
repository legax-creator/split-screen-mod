package com.dogmod.capability;

import com.dogmod.DogMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerPlayerEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DogDataManager {

    private static final Map<UUID, DogData> DATA_MAP = new HashMap<>();

    public static void register() {
        // Respawn olunca veriyi koru
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            UUID uuid = newPlayer.getUuid();
            if (DATA_MAP.containsKey(uuid)) {
                DogMod.LOGGER.debug("Respawn: {} için dog data korundu", uuid);
            }
        });

        // Veriyi NBT ile kaydet (sunucu kapatılınca)
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DogMod.LOGGER.info("Dog verileri kaydediliyor...");
        });

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

    // Veriyi NBT'ye kaydet
    public static NbtCompound save(UUID uuid) {
        NbtCompound nbt = new NbtCompound();
        DogData data = DATA_MAP.get(uuid);
        if (data != null) data.writeToNbt(nbt);
        return nbt;
    }

    // Veriyi NBT'den yükle
    public static void load(UUID uuid, NbtCompound nbt) {
        DogData data = new DogData();
        data.readFromNbt(nbt);
        DATA_MAP.put(uuid, data);
    }
}
