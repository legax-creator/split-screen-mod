package com.dogmod.command;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import com.dogmod.disguise.DogDisguise;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DogCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        // /dog → köpek ol
        dispatcher.register(CommandManager.literal("dog")
            .executes(ctx -> {
                ServerPlayerEntity player = ctx.getSource().getPlayer();
                if (player == null) return 0;
                DogData data = DogDataManager.get(player);
                if (data.isDog()) {
                    player.sendMessage(Text.literal("§cZaten köpeksin!"), false);
                    return 0;
                }
                becomeDog(player, data);
                return 1;
            })

            // /dog çıkış → insan ol
            .then(CommandManager.literal("çıkış")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) return 0;
                    DogData data = DogDataManager.get(player);
                    if (!data.isDog()) {
                        player.sendMessage(Text.literal("§cKöpek değilsin!"), false);
                        return 0;
                    }
                    becomeHuman(player, data);
                    return 1;
                })
            )

            // /dog isim <isim>
            .then(CommandManager.literal("isim")
                .then(CommandManager.argument("isim", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        if (player == null) return 0;
                        String name = StringArgumentType.getString(ctx, "isim");
                        return nameDog(player, name, ctx.getSource());
                    })
                )
            )
        );
    }

    public static void becomeDog(ServerPlayerEntity player, DogData data) {
        data.setDog(true);
        applyDogEffects(player);
        DogDisguise.apply(player);
        updateTabList(player, data);
        player.sendMessage(Text.literal("§a🐕 Artık bir köpeksin! Sahibin seni kemikle evcilleştirebilir."), false);
    }

    public static void becomeHuman(ServerPlayerEntity player, DogData data) {
        data.setDog(false);
        data.setTamed(false);
        data.setOwnerUUID(null);
        data.setSitting(false);
        data.clearLeash();
        data.setDogName(null);
        removeDogEffects(player);
        DogDisguise.remove(player);
        updateTabList(player, data);
        player.sendMessage(Text.literal("§7Köpek olmaktan çıktın."), false);
    }

    private static int nameDog(ServerPlayerEntity sender, String name, ServerCommandSource source) {
        DogData senderData = DogDataManager.get(sender);

        // Sahibi köpeğine isim veriyor
        for (ServerPlayerEntity dog : sender.getServer().getPlayerManager().getPlayerList()) {
            if (!DogOriginChecker.isDogPlayer(dog)) continue;
            DogData dogData = DogDataManager.get(dog);
            if (!dogData.isOwner(sender.getUuid())) continue;

            dogData.setDogName(name);
            DogDisguise.refresh(dog);
            updateTabList(dog, dogData);

            dog.sendMessage(Text.literal("§a✨ Sahibin sana '§e" + name + "§a' ismini verdi!"), false);
            sender.sendMessage(Text.literal("§a✨ Köpeğine '§e" + name + "§a' ismini verdin!"), false);
            return 1;
        }

        // Köpek kendi ismini değiştiremez
        sender.sendMessage(Text.literal("§cBu komutu sadece sahibi kullanabilir!"), false);
        return 0;
    }

    public static void applyDogEffects(ServerPlayerEntity player) {
        // Vanilla wolf değerleri
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);
        player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(4.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED).setBaseValue(16.0);
        player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE).setBaseValue(2.0);
    }

    public static void removeDogEffects(ServerPlayerEntity player) {
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
        player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(1.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED).setBaseValue(4.0);
        player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE).setBaseValue(3.0);
    }

    public static void updateTabList(ServerPlayerEntity player, DogData data) {
        String displayName;
        if (data.isDog()) {
            String name = data.getDogName() != null ? data.getDogName()
                : player.getName().getString();
            displayName = "§6🐕 " + name;
        } else {
            displayName = "§f👤 " + player.getName().getString();
        }
        player.setCustomName(Text.literal(displayName));
    }
}
