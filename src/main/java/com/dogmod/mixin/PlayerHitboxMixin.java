package com.dogmod.mixin;

import com.dogmod.capability.DogDataManager;
import com.dogmod.capability.DogOriginChecker;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerHitboxMixin {

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void dogHitbox(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (!(player instanceof ServerPlayerEntity sp)) return;
        if (!DogOriginChecker.isDogPlayer(sp)) return;
        cir.setReturnValue(EntityDimensions.changing(0.6f, 0.85f));
    }
}
