package com.dogmod.command;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class RastgeleCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("rastgele")
            .then(CommandManager.argument("birinci", StringArgumentType.word())
                .then(CommandManager.argument("ikinci", StringArgumentType.word())
                    .executes(ctx -> {
                        String birinci = StringArgumentType.getString(ctx, "birinci");
                        String ikinci = StringArgumentType.getString(ctx, "ikinci");
                        return execute(ctx.getSource(), birinci, ikinci);
                    })
                )
            )
        );
    }

    private static int execute(ServerCommandSource source, String birinci, String ikinci) {
        ServerPlayerEntity dogPlayer = source.getServer().getPlayerManager().getPlayer(birinci);
        ServerPlayerEntity humanPlayer = source.getServer().getPlayerManager().getPlayer(ikinci);

        if (dogPlayer == null) {
            source.sendFeedback(() -> Text.literal("§c" + birinci + " adlı oyuncu bulunamadı!"), false);
            return 0;
        }
        if (humanPlayer == null) {
            source.sendFeedback(() -> Text.literal("§c" + ikinci + " adlı oyuncu bulunamadı!"), false);
            return 0;
        }

        // Birinci kişi direk köpek olur
        DogData dogData = DogDataManager.get(dogPlayer);
        DogCommand.becomeDog(dogPlayer, dogData);

        // Tüm oyunculara duyur
        String dogName = dogData.getDogName() != null ? dogData.getDogName() : dogPlayer.getName().getString();
        for (ServerPlayerEntity p : source.getServer().getPlayerManager().getPlayerList()) {
            p.sendMessage(Text.literal("§6🐕 " + dogName + " §aköpek seçildi, tebrikler!"), false);
            p.sendMessage(Text.literal("§f👤 " + humanPlayer.getName().getString() + " §ainsan seçildi, tebrikler!"), false);
        }

        return 1;
    }
}
