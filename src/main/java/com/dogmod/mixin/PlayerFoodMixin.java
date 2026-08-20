package com.dogmod.mixin;

import com.dogmod.capability.DogOriginChecker;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerFoodMixin {

    // Kendi başına yemek yemeyi engelle
    @Inject(method = "eatFood", at = @At("HEAD"), cancellable = true)
    private void blockEating(net.minecraft.world.World world, ItemStack stack,
            CallbackInfoReturnable<ItemStack> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (!(player instanceof ServerPlayerEntity sp)) return;
        if (!DogOriginChecker.isDogPlayer(sp)) return;
        cir.setReturnValue(stack);
    }

    // Yerden eşya almayı engelle
    @Inject(
        method = "touch(Lnet/minecraft/entity/Entity;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void blockPickup(net.minecraft.entity.Entity entity, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (!(player instanceof ServerPlayerEntity sp)) return;
        if (!DogOriginChecker.isDogPlayer(sp)) return;
        if (entity instanceof ItemEntity) {
            ci.cancel();
        }
    }
}
