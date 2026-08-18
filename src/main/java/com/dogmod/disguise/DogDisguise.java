package com.dogmod.disguise;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import xyz.nucleoid.disguiselib.api.EntityDisguise;

public class DogDisguise {

    public static void apply(ServerPlayerEntity player) {
        DogData data = DogDataManager.get(player);
        ServerWorld world = (ServerWorld) player.getWorld();
        WolfEntity wolf = EntityType.WOLF.create(world);
        if (wolf == null) return;
        wolf.setInSittingPose(data.isSitting());
        wolf.setTamed(data.isTamed());
        wolf.setCustomName(Text.literal(buildNameTag(player, data)));
        wolf.setCustomNameVisible(true);
        ((EntityDisguise) player).disguiseAs(wolf);
    }

    public static void remove(ServerPlayerEntity player) {
        ((EntityDisguise) player).removeDisguise();
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
