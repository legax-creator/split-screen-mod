package com.dogmod.mixin;

import com.dogmod.capability.DogData;
import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class PlayerHandMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onPlayerInteractBlock", at = @At("HEAD"), cancellable = true)
    private void blockInteractBlock(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        if (DogOriginChecker.isDogPlayer(player)) ci.cancel();
    }

    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true)
    private void blockMovement(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (!DogOriginChecker.isDogPlayer(player)) return;
        DogData data = DogDataManager.get(player);
        if (!data.isSitting()) return;
        if (packet instanceof PlayerMoveC2SPacket.PositionAndOnGround
            || packet instanceof PlayerMoveC2SPacket.Full) ci.cancel();
    }
}
