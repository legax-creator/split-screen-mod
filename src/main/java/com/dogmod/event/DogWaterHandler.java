package com.dogmod.event;

import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import com.dogmod.disguise.DogDisguise;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DogWaterHandler {

    private static final Map<UUID, Boolean> wasInWater = new HashMap<>();
    private static final Map<UUID, Integer> shakeTicks = new HashMap<>();
    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(DogWaterHandler::onTick);
    }

    private static void onTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter % 5 != 0) return;

        for (ServerPlayerEntity dog : server.getPlayerManager().getPlayerList()) {
            if (!DogOriginChecker.isDogPlayer(dog)) continue;
            if (!(dog.getWorld() instanceof ServerWorld sw)) continue;

            UUID uuid = dog.getUuid();
            boolean inWater = dog.isTouchingWater() || dog.isWet();
            boolean prev = wasInWater.getOrDefault(uuid, false);

            if (inWater != prev) {
                updateWetDisguise(dog, sw, inWater);
                wasInWater.put(uuid, inWater);
                if (!inWater && prev) {
                    shakeTicks.put(uuid, 40);
                }
            }

            Integer shake = shakeTicks.get(uuid);
            if (shake != null && shake > 0) {
                tickShake(dog, sw, shake);
                shakeTicks.put(uuid, shake - 5);
            } else if (shake != null && shake <= 0) {
                shakeTicks.remove(uuid);
                updateWetDisguise(dog, sw, false);
            }
        }
    }

    private static void updateWetDisguise(ServerPlayerEntity dog, ServerWorld world, boolean wet) {
        WolfEntity wolf = EntityType.WOLF.create(world);
        if (wolf == null) return;
        wolf.setId(dog.getId());
        wolf.setInSittingPose(DogDataManager.get(dog).isSitting());
        wolf.setTamed(DogDataManager.get(dog).isTamed());
    

     var dirtyEntries = wolf.getDataTracker().getDirtyEntries();
        if (dirtyEntries != null && !dirtyEntries.isEmpty()) {
            for (ServerPlayerEntity observer : world.getServer().getPlayerManager().getPlayerList()) {
                if (observer == dog) continue;
                observer.networkHandler.sendPacket(
                    new EntityTrackerUpdateS2CPacket(dog.getId(), dirtyEntries)
                );
            }
        }
    }

    private static void tickShake(ServerPlayerEntity dog, ServerWorld world, int remaining) {
        world.spawnParticles(ParticleTypes.SPLASH,
            dog.getX(), dog.getY() + 0.5, dog.getZ(),
            8, 0.4, 0.3, 0.4, 0.1);

        if (remaining == 40) {
            world.playSoundFromEntity(null, dog,
                SoundEvents.ENTITY_WOLF_SHAKE,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }
}
