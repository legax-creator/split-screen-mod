package com.dogmod.mixin;

import com.dogmod.capability.DogOriginChecker;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class PlayerCommandMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onCommandExecution", at = @At("HEAD"), cancellable = true)
    private void blockCommands(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        if (!DogOriginChecker.isDogPlayer(player)) return;
        player.sendMessage(Text.literal("§cKöpekler komut kullanamaz!"), true);
        ci.cancel();
    }
}
