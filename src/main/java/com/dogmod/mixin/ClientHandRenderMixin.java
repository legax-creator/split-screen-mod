package com.dogmod.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HeldItemRenderer.class)
public class ClientHandRenderMixin {

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void hideHand(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        try {
            // Origins verisini player NBT'sinden oku
            NbtCompound nbt = new NbtCompound();
            client.player.writeNbt(nbt);

            // Origins, origin ID'sini PlayerData NBT'sine yazar
            if (nbt.contains("origins")) {
                NbtCompound origins = nbt.getCompound("origins");
                for (String key : origins.getKeys()) {
                    String originId = origins.getString(key);
                    if (originId.equals("dogmod:dog") || originId.contains("wolf")) {
                        ci.cancel();
                        return;
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
