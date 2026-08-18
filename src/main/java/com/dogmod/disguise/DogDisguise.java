package com.dogmod.disguise;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class DogDisguise {

    public static void apply(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        DogData data = DogDataManager.get(player);

        WolfEntity wolf = EntityType.WOLF.create(world);
        if (wolf == null) return;

        wolf.setInSittingPose(data.isSitting());
        wolf.setTamed(data.isTamed());
        wolf.setCustomName(Text.literal(buildNameTag(player, data)));
        wolf.setCustomNameVisible(true);
        wolf.setId(player.getId());

        for (ServerPlayerEntity observer : world.getServer().getPlayerManager().getPlayerList()) {
            if (observer == player) continue;

            observer.networkHandler.sendPacket(
                new EntitiesDestroyS2CPacket(player.getId())
            );
            observer.networkHandler.sendPacket(
                new EntitySpawnS2CPacket(wolf, 0, wolf.getBlockPos())
            );
            if (wolf.getDataTracker().getDirtyEntries() != null) {
                observer.networkHandler.sendPacket(
                    new EntityTrackerUpdateS2CPacket(wolf.getId(),
                        wolf.getDataTracker().getDirtyEntries())
                );
            }
        }
    }

    public static void remove(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        for (ServerPlayerEntity observer : world.getServer().getPlayerManager().getPlayerList()) {
            if (observer == player) continue;
            observer.networkHandler.sendPacket(
                new EntitiesDestroyS2CPacket(player.getId())
            );
            observer.networkHandler.sendPacket(
                new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player)
            );
        }
    }

    public static void refresh(ServerPlayerEntity player) {
        apply(player);
    }

    private static String buildNameTag(ServerPlayerEntity player, DogData data) {
        String playerName = player.getName().getString();
        if (!data.isTamed() || data.getOwnerUUID() == null) {
            return "§7Sahipsiz Köpek §8| §f" + playerName;
        }
        ServerPlayerEntity owner = player.getServer()
            .getPlayerManager().getPlayer(data.getOwnerUUID());
        String ownerName = owner != null ? owner.getName().getString() : "§oÇevrimdışı§r";
        return "§e" + ownerName + "'nın Köpeği §8| §f" + playerName;
    }
}
