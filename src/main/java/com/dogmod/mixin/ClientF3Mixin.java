package com.dogmod.mixin;

import com.dogmod.capability.DogDataManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(DebugHud.class)
public class ClientF3Mixin {

    @Inject(method = "getLeftText", at = @At("HEAD"), cancellable = true)
    private void hideF3Left(CallbackInfoReturnable<List<String>> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        try {
            // Client tarafında köpek kontrolü NBT ile
            var nbt = new net.minecraft.nbt.NbtCompound();
            client.player.writeNbt(nbt);
            if (nbt.getBoolean("dogmod_isdog")) {
                cir.setReturnValue(List.of());
            }
        } catch (Exception ignored) {}
    }

    @Inject(method = "getRightText", at = @At("HEAD"), cancellable = true)
    private void hideF3Right(CallbackInfoReturnable<List<String>> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        try {
            var nbt = new net.minecraft.nbt.NbtCompound();
            client.player.writeNbt(nbt);
            if (nbt.getBoolean("dogmod_isdog")) {
                cir.setReturnValue(List.of());
            }
        } catch (Exception ignored) {}
    }
}
