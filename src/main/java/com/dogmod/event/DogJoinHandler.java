package com.dogmod.event;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import com.dogmod.command.DogCommand;
import com.dogmod.disguise.DogDisguise;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DogJoinHandler {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            // Komut listesini göster
            player.sendMessage(Text.literal(
                "§6=== 🐕 Dog Player Mod ===\n" +
                "§e/dog §7→ Köpek olursun\n" +
                "§e/dog çıkış §7→ İnsan olursun\n" +
                "§e/dog isim <isim> §7→ Köpeğine isim ver\n" +
                "§e/info §7→ Sunucudaki rolleri gör\n" +
                "§e/rastgele <oyuncu1> <oyuncu2> §7→ İki oyuncudan birine rastgele köpek rolü verir\n" +
                "§6========================"
            ), false);

            // Köpekse yeniden disguise uygula
            DogData data = DogDataManager.get(player);
            if (data.isDog()) {
                DogDisguise.apply(player);
                DogCommand.applyDogEffects(player);
                DogCommand.updateTabList(player, data);

                // Sahibi çevrimdışıysa otur
                if (data.isTamed() && data.getOwnerUUID() != null) {
                    ServerPlayerEntity owner = server.getPlayerManager()
                        .getPlayer(data.getOwnerUUID());
                    if (owner == null) {
                        data.setSitting(true);
                        player.sendMessage(Text.literal("§7Sahibin çevrimdışı, oturuyorsun."), true);
                        DogDisguise.refresh(player);
                    }
                }
            }

            // Sahibi gelince köpeği kaldır
            for (ServerPlayerEntity dog : server.getPlayerManager().getPlayerList()) {
                if (!DogOriginChecker.isDogPlayer(dog)) continue;
                DogData dogData = DogDataManager.get(dog);
                if (!dogData.isTamed()) continue;
                if (!dogData.isOwner(player.getUuid())) continue;
                if (!dogData.isSitting()) continue;

                // Bu oyuncu bir köpeğin sahibi ve köpek oturuyor
                dogData.setSitting(false);
                dog.sendMessage(Text.literal("§7Sahibin geldi, kalkabilirsin!"), true);
                DogDisguise.refresh(dog);
            }
        });

        // Çıkışta köpeği oturt
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity leaving = handler.getPlayer();

            // Bu oyuncu bir köpeğin sahibiyse köpeği oturt
            for (ServerPlayerEntity dog : server.getPlayerManager().getPlayerList()) {
                if (!DogOriginChecker.isDogPlayer(dog)) continue;
                DogData dogData = DogDataManager.get(dog);
                if (!dogData.isTamed()) continue;
                if (!dogData.isOwner(leaving.getUuid())) continue;

                dogData.setSitting(true);
                dog.sendMessage(Text.literal("§7Sahibin çıktı, oturuyorsun."), true);
                DogDisguise.refresh(dog);
            }

            // Köpek çıkıyorsa disguise kaldır ama veri tutulsun
            DogData data = DogDataManager.get(leaving);
            if (data.isDog()) {
                DogDisguise.remove(leaving);
                // Veriyi silme! Kalıcı olsun
            }
        });
    }
}
