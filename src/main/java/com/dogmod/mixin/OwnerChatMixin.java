package com.dogmod.mixin;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class OwnerChatMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onOwnerChat(ChatMessageC2SPacket packet, CallbackInfo ci) {
        // Köpek değilse sahip mi kontrol et
        if (DogOriginChecker.isDogPlayer(player)) return;

        // Bu oyuncu herhangi bir köpeğin sahibi mi?
        String dogName = null;
        for (ServerPlayerEntity dog : player.getServer().getPlayerManager().getPlayerList()) {
            if (!DogOriginChecker.isDogPlayer(dog)) continue;
            DogData dogData = DogDataManager.get(dog);
            if (!dogData.isOwner(player.getUuid())) continue;
            dogName = dogData.getDogName() != null
                ? dogData.getDogName()
                : dog.getName().getString();
            break;
        }

        if (dogName == null) return; // Sahip değil, normal chat

        String content = packet.chatMessage();
        String ownerPrefix = "§6" + dogName + "'nın Sahibi " + player.getName().getString();
        Text chatMessage = Text.literal(ownerPrefix + "§7: §f" + content);

        for (ServerPlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) {
            p.sendMessage(chatMessage, false);
        }

        ci.cancel();
    }
}
