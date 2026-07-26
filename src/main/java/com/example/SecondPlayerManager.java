package com.example;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.fabricmc.fabric.api.entity.FakePlayer;

import java.util.UUID;

public class SecondPlayerManager {
    // Sabit bir kimlik (UUID) — her açılışta aynı kalacak, böylece envanter korunur
    private static final UUID PLAYER2_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final String PLAYER2_NAME = "Oyuncu2";

    public static ServerPlayerEntity player2 = null;

    public static void spawnPlayer2(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        GameProfile profile = new GameProfile(PLAYER2_UUID, PLAYER2_NAME);

        player2 = FakePlayer.get(world, profile);
        System.out.println("2. Oyuncu dünyaya eklendi, kimlik: " + PLAYER2_UUID);
    }
}
