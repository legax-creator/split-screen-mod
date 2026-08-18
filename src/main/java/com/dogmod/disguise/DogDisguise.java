package com.dogmod.disguise;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.SpawnEntityS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Vanilla packet sistemi ile oyuncuyu wolf olarak gösterir.
 * Kütüphane gerektirmez.
 *
 * Nasıl çalışır:
 * 1. Diğer oyunculara "bu oyuncu yok" paketi gönder (destroy)
 * 2. Aynı ID ile wolf entity spawn paketi gönder
 * 3. Wolf'un metadata paketini gönder (oturma, isim tag vs.)
 */
public class DogDisguise {

    public static void apply(ServerPlayerEntity dogPlayer) {
        ServerWorld world = (ServerWorld) dogPlayer.getWorld();
        DogData data = DogDataManager.get(dogPlayer);

        // Geçici wolf entity oluştur (dünyaya eklenmez)
        WolfEntity wolf = EntityType.WOLF.create(world);
        if (wolf == null) return;

        wolf.setInSittingPose(data.isSitting());
        wolf.setTamed(data.isTamed());
        wolf.setCustomName(Text.literal(buildNameTag(dogPlayer, data)));
        wolf.setCustomNameVisible(true);
        wolf.setId(dogPlayer.getId()); // Aynı entity ID'yi kullan

        // Diğer tüm oyunculara wolf paketi gönder
        for (ServerPlayerEntity observer : world.getServer().getPlayerManager().getPlayerList()) {
            if (observer == dogPlayer) continue; // Kendine gönderme

            // Önce oyuncuyu yok et
            observer.networkHandler.sendPacket(
                new EntitiesDestroyS2CPacket(dogPlayer.getId())
            );

            // Wolf olarak spawn et
            observer.networkHandler.sendPacket(
                new SpawnEntityS2CPacket(wolf, 0, wolf.getBlockPos())
            );

            // Wolf metadata (oturma pozu, isim tag)
            observer.networkHandler.sendPacket(
                new EntityTrackerUpdateS2CPacket(wolf.getId(), wolf.getDataTracker().getDirtyEntries())
            );
        }
    }

    public static void remove(ServerPlayerEntity dogPlayer) {
        ServerWorld world = (ServerWorld) dogPlayer.getWorld();

        // Gerçek oyuncu paketini herkese yeniden gönder
        for (ServerPlayerEntity observer : world.getServer().getPlayerManager().getPlayerList()) {
            if (observer == dogPlayer) continue;

            // Wolf'u yok et
            observer.networkHandler.sendPacket(
                new EntitiesDestroyS2CPacket(dogPlayer.getId())
            );

            // Gerçek oyuncuyu tekrar spawn et
            observer.networkHandler.sendPacket(
                new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, dogPlayer)
            );
        }
    }

    public static void refresh(ServerPlayerEntity dogPlayer) {
        apply(dogPlayer);
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
