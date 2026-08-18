package com.dogmod.mixin;

import com.dogmod.capability.DogOriginChecker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerInteractMixin {
    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void blockInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (!(player instanceof ServerPlayerEntity sp)) return;
        if (!DogOriginChecker.isDogPlayer(sp)) return;
        if (entity instanceof PlayerEntity) return;
        cir.setReturnValue(ActionResult.FAIL);
    }
}
