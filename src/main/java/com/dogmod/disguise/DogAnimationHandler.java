package com.dogmod.disguise;

import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Her tick'te wolf entity'sinin hareket paketlerini gönderir.
 * Bu sayede diğer oyuncular wolf animasyonunu görür.
 */
public class DogAnimationHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(DogAnimationHandler::onTick);
    }

    private static void onTick(MinecraftServer server) {
        for (ServerPlayerEntity dog : server.getPlayerManager().getPlayerList()) {
            if (!DogOriginChecker.isDogPlayer(dog)) continue;

            ServerWorld world = (ServerWorld) dog.getWorld();

            // Geçici wolf oluştur, pozisyonunu köpek oyuncuyla eşleştir
            WolfEntity wolf = EntityType.WOLF.create(world);
            if (wolf == null) continue;

            wolf.setId(dog.getId());
            wolf.setPosition(dog.getPos());
            wolf.setVelocity(dog.getVelocity());
            wolf.setYaw(dog.getYaw());
            wolf.setPitch(dog.getPitch());
            wolf.setHeadYaw(dog.getHeadYaw());

            boolean isMoving = dog.getVelocity().horizontalLength() > 0.01;
            wolf.setInSittingPose(DogDataManager.get(dog).isSitting());

            // Diğer oyunculara hareket paketi gönder
            for (ServerPlayerEntity observer : server.getPlayerManager().getPlayerList()) {
                if (observer == dog) continue;

                // Pozisyon ve rotasyon paketi
                observer.networkHandler.sendPacket(
                    new EntityS2CPacket.MoveRelative(
                        dog.getId(),
                        (short)(dog.getVelocity().x * 8000),
                        (short)(dog.getVelocity().y * 8000),
                        (short)(dog.getVelocity().z * 8000),
                        isMoving
                    )
                );

                // Baş rotasyonu
                observer.networkHandler.sendPacket(
                    new EntityS2CPacket.RotateHead(wolf, (byte)(dog.getHeadYaw() * 256.0F / 360.0F))
                );

                // Hız paketi (yürüyüş animasyonu için)
                if (isMoving) {
                    observer.networkHandler.sendPacket(
                        new EntityVelocityUpdateS2CPacket(
                            dog.getId(),
                            dog.getVelocity()
                        )
                    );
                }
            }
        }
    }
}
