package com.example.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class SplitScreenRenderer {

    // 2. görüntü için ayrı bir kamera nesnesi (şimdilik test amaçlı 1. oyuncuyla aynı yerden bakıyor)
    private static final Camera secondCamera = new Camera();

    public static void init() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (ExampleModClient.splitScreenActive) {
                drawDividerLine(context);
            }
        });

        WorldRenderEvents.END.register(context -> {
            if (ExampleModClient.splitScreenActive) {
                renderSecondViewport(context.tickDelta());
            }
        });
    }

    private static void renderSecondViewport(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || client.worldRenderer == null) {
            return;
        }

        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();

        // Test amaçlı: şimdilik 1. oyuncunun kendi konumundan bakıyoruz
        secondCamera.update(client.world, client.player, false, false, tickDelta);

        // Ekranın ALT yarısına sıkıştırıyoruz
        RenderSystem.viewport(0, 0, width, height / 2);

        Matrix4f projectionMatrix = client.gameRenderer.getBasicProjectionMatrix(70.0);
        MatrixStack matrices = new MatrixStack();

        client.worldRenderer.render(
                matrices,
                tickDelta,
                System.nanoTime(),
                false,
                secondCamera,
                client.gameRenderer,
                client.gameRenderer.getLightmapTextureManager(),
                projectionMatrix
        );

        // Görüntü alanını eski haline getiriyoruz, yoksa üst yarı de bozulur
        RenderSystem.viewport(0, 0, width, height);
    }

    private static void drawDividerLine(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int middleY = screenHeight / 2;

        // Artık yatay çizgi çiziyoruz (üst/alt bölünme için)
        context.fill(0, middleY - 1, screenWidth, middleY + 1, 0xFFFFFFFF);
    }
}
