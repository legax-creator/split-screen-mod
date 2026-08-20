package com.dogmod.disguise;

import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class DogAnimationHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(DogAnimationHandler::onTick);
    }

    private static void onTick(MinecraftServer server) {
        for (ServerPlayerEntity dog : server.getPlayerManager().getPlayerList()) {
            if (!DogOriginChecker.isDogPlayer(dog)) continue;

            ServerWorld world = (ServerWorld) dog.getWorld();

            WolfEntity wolf = EntityType.WOLF.create(world);
            if (wolf == null) continue;

            wolf.setId(dog.getId());
            wolf.setPosition(dog.getPos());
            wolf.setVelocity(dog.getVelocity());
            wolf.setYaw(dog.getYaw());
            wolf.setPitch(dog.getPitch());
            wolf.setHeadYaw(dog.getHeadYaw());
            wolf.setInSittingPose(DogDataManager.get(dog).isSitting());

            boolean isMoving = dog.getVelocity().horizontalLength() > 0.01;

            for (ServerPlayerEntity observer : server.getPlayerManager().getPlayerList()) {
                if (observer == dog) continue;

                // Pozisyon ve hareket paketi
                observer.networkHandler.sendPacket(
                    new EntityS2CPacket.MoveRelative(
                        dog.getId(),
                        (short)(dog.getVelocity().x * 8000),
                        (short)(dog.getVelocity().y * 8000),
                        (short)(dog.getVelocity().z * 8000),
                        isMoving
                    )
                );

                // Baş rotasyonu (1.20.1'de doğru paket)
                observer.networkHandler.sendPacket(
                    new EntitySetHeadYawS2CPacket(wolf,
                        (byte)(dog.getHeadYaw() * 256.0F / 360.0F))
                );

                // Hız paketi
                if (isMoving) {
                    observer.networkHandler.sendPacket(
                        new EntityVelocityUpdateS2CPacket(dog.getId(), dog.getVelocity())
                    );
                }
            }
        }
    }
}
