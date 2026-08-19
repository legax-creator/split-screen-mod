package com.dogmod.mixin;

import com.dogmod.capability.DogDataManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
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
        // Client tarafında UUID ile server data'ya erişemeyiz
        // Bu yüzden client-side bir flag tutuyoruz
        // Şimdilik devre dışı - ileride client-server sync eklenebilir
    }
}
