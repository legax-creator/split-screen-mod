package com.dogmod.command;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import com.dogmod.disguise.DogDisguise;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DogCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        // /dog - köpek ol
        dispatcher.register(CommandManager.literal("dog")
            .executes(ctx -> {
                ServerPlayerEntity player = ctx.getSource().getPlayer();
                if (player == null) return 0;

                DogData data = DogDataManager.get(player);

                if (data.isDog()) {
                    player.sendMessage(Text.literal("§cZaten köpeksin!"), false);
                    return 0;
                }

                data.setDog(true);
                applyDogEffects(player);
                DogDisguise.apply(player);

                player.sendMessage(Text.literal(
                    "§a🐕 Artık bir köpeksin! Sahibin seni kemikle evcilleştirebilir."), false);
                return 1;
            })
            .then(CommandManager.literal("leave")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) return 0;

                    DogData data = DogDataManager.get(player);

                    if (!data.isDog()) {
                        player.sendMessage(Text.literal("§cKöpek değilsin!"), false);
                        return 0;
                    }

                    data.setDog(false);
                    data.setTamed(false);
                    data.setOwnerUUID(null);
                    data.setSitting(false);
                    data.clearLeash();

                    removeDogEffects(player);
                    DogDisguise.remove(player);

                    player.sendMessage(Text.literal("§7Köpek olmaktan çıktın."), false);
                    return 1;
                })
            )
        );
    }

    public static void applyDogEffects(ServerPlayerEntity player) {
        // Max can vanilla wolf gibi (20 HP)
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
            .setBaseValue(20.0);

        // Hafif hız artışı
        player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
            .setBaseValue(0.13);
    }

    public static void removeDogEffects(ServerPlayerEntity player) {
        // Varsayılan değerlere dön
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
            .setBaseValue(20.0);
        player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
            .setBaseValue(0.1);
    }
}
