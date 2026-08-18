package com.dogmod.mixin;

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
        try {
            var component = io.github.apace100.origins.registry.ModComponents.ORIGIN.get(client.player);
            for (var layer : io.github.apace100.origins.origin.OriginLayers.getLayers()) {
                var origin = component.getOrigin(layer);
                if (origin == null) continue;
                String id = origin.getIdentifier().toString();
                if (id.equals("dogmod:dog") || id.contains("wolf")) { ci.cancel(); return; }
            }
        } catch (Exception ignored) {}
    }
}
