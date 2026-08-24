package com.dogmod.command;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class InfoCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("info")
            .executes(ctx -> {
                execute(ctx.getSource());
                return 1;
            })
        );
    }

    private static void execute(ServerCommandSource source) {
        StringBuilder sb = new StringBuilder();
        sb.append("§6=== Oyuncu Rolleri ===\n");

        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            DogData data = DogDataManager.get(player);
            if (data.isDog()) {
                String name = data.getDogName() != null ? data.getDogName()
                    : player.getName().getString();
                String ownerName = "Sahipsiz";
                if (data.getOwnerUUID() != null) {
                    ServerPlayerEntity owner = source.getServer().getPlayerManager()
                        .getPlayer(data.getOwnerUUID());
                    ownerName = owner != null ? owner.getName().getString() : "Çevrimdışı";
                }
                sb.append("§6🐕 §e").append(name)
                  .append(" §7(Sahibi: §f").append(ownerName).append("§7)\n");
            } else {
                sb.append("§f👤 §7").append(player.getName().getString()).append("\n");
            }
        }

        sb.append("§6=====================");

        Text message = Text.literal(sb.toString());
        if (source.getPlayer() != null) {
            source.getPlayer().sendMessage(message, false);
        } else {
            source.sendFeedback(() -> message, false);
        }
    }
}
