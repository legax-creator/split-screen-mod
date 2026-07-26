package com.example.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class SplitScreenRenderer {
    public static void init() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (ExampleModClient.splitScreenActive) {
                drawDividerLine(context);
            }
        });
    }

    private static void drawDividerLine(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int middleX = screenWidth / 2;

        // Ortadan dikey bir çizgi çiziyoruz, bu ekranın "bölündüğünü" görsel olarak gösterecek ilk test
        context.fill(middleX - 1, 0, middleX + 1, screenHeight, 0xFFFFFFFF);
    }
}
