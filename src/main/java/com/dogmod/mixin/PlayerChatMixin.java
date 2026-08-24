package com.dogmod.mixin;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class PlayerChatMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "handleMessage", at = @At("HEAD"), cancellable = true)
    private void onChat(SignedMessage message, CallbackInfo ci) {
        if (!DogOriginChecker.isDogPlayer(player)) return;

        DogData data = DogDataManager.get(player);
        String chatName = data.getDogName() != null
            ? data.getDogName()
            : player.getName().getString();

        String content = message.getSignedContent();

        // Köpek chat mesajını özel prefix ile gönder
        Text chatMessage = Text.literal("§e" + chatName + "§7: §f" + content);
        for (ServerPlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) {
            p.sendMessage(chatMessage, false);
        }

        ci.cancel(); // Orijinal mesajı iptal et
    }
}
