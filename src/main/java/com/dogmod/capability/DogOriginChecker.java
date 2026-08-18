package com.dogmod.capability;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;

public class DogOriginChecker {

    private static final String[] DOG_ORIGIN_IDS = {
        "dogmod:dog", "moborigins:wolf", "origins:wolf", "extraorigins:wolf"
    };

    public static boolean isDogPlayer(ServerPlayerEntity player) {
        try {
            NbtCompound nbt = new NbtCompound();
            player.writeNbt(nbt);

            // Origins modu verisini player NBT'sinde "origins:origin" altında saklar
            if (!nbt.contains("ForgeCaps") && !nbt.contains("origins:origin")) {
                // Fabric'te Origins, PlayerData NBT'sine yazar
                // "origins" key'i altında layer -> originId şeklinde
                if (nbt.contains("origins")) {
                    NbtCompound originsNbt = nbt.getCompound("origins");
                    for (String key : originsNbt.getKeys()) {
                        String originId = originsNbt.getString(key);
                        for (String dogId : DOG_ORIGIN_IDS) {
                            if (originId.equals(dogId)) return true;
                        }
                    }
                }
            }

            // Alternatif: persistentData içinde ara
            if (nbt.contains("BukkitValues")) return false;

        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
