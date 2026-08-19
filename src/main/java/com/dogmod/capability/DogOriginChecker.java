package com.dogmod.capability;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Origins'e bağımlılık yok.
 * Oyuncunun köpek olup olmadığını DogData üzerinden kontrol eder.
 * /dog komutuyla köpek olunur, /dog leave ile çıkılır.
 */
public class DogOriginChecker {

    public static boolean isDogPlayer(ServerPlayerEntity player) {
        try {
            DogData data = DogDataManager.get(player);
            return data.isDog();
        } catch (Exception e) {
            return false;
        }
    }
}
