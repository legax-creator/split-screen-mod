package com.dogmod.mixin;

import com.dogmod.capability.DogOriginChecker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerArmorMixin {

    @Inject(method = "updateItems", at = @At("HEAD"))
    private void checkArmor(CallbackInfo ci) {
        PlayerInventory inv = (PlayerInventory)(Object)this;
        PlayerEntity player = inv.player;
        if (!(player instanceof ServerPlayerEntity sp)) return;
        if (!DogOriginChecker.isDogPlayer(sp)) return;

        // Zırh slotlarını kontrol et, varsa düşür
        for (int i = 0; i < inv.armor.size(); i++) {
            ItemStack armor = inv.armor.get(i);
            if (!armor.isEmpty()) {
                // Zırhı yere düşür
                player.dropItem(armor, false);
                inv.armor.set(i, ItemStack.EMPTY);
            }
        }
    }
}
