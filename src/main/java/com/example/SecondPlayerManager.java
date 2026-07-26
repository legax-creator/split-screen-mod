package com.example;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.entity.MovementType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class SecondPlayerManager {
    private static final UUID PLAYER2_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final String PLAYER2_NAME = "Oyuncu2";

    public static ServerPlayerEntity player2 = null;

    public static void spawnPlayer2(MinecraftServer server) {
        if (server == null) return;

        server.execute(() -> {
            if (player2 != null) return; // zaten eklenmiş

            ServerWorld world = server.getOverworld();
            GameProfile profile = new GameProfile(PLAYER2_UUID, PLAYER2_NAME);
            player2 = FakePlayer.get(world, profile);

            if (!world.getPlayers().isEmpty()) {
                ServerPlayerEntity host = world.getPlayers().get(0);
                player2.refreshPositionAndAngles(
                        host.getX() + 2, host.getY(), host.getZ(),
                        host.getYaw(), host.getPitch()
                );
            }

            world.spawnEntity(player2);
            System.out.println("2. Oyuncu dünyaya eklendi: " + PLAYER2_UUID);
        });
    }

    public static void movePlayer2(MinecraftServer server, double dx, double dz, float yawDelta) {
        if (server == null || player2 == null) return;

        server.execute(() -> {
            if (player2 == null) return;
            player2.setYaw(player2.getYaw() + yawDelta);
            player2.move(MovementType.SELF, new Vec3d(dx, 0, dz));
        });
    }
}
