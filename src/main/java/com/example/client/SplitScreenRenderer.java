package com.example.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class SplitScreenRenderer {

    private static final Camera secondCamera = new Camera();
    private static boolean renderingSecondView = false;

    public static void init() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (ExampleModClient.splitScreenActive) {
                drawDividerLine(context);
            }
        });

        WorldRenderEvents.END.register(context -> {
            if (renderingSecondView) return;
            if (!ExampleModClient.splitScreenActive) return;

            renderingSecondView = true;
            try {
                renderSecondViewport(context.tickDelta());
            } finally {
                renderingSecondView = false;
            }
        });
    }

    private static void renderSecondViewport(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || client.worldRenderer == null) {
            return;
        }

        int width = client.getWindow().getFramebufferWidth();
        int halfHeight = client.getWindow().getFramebufferHeight() / 2;

        secondCamera.update(client.world, client.player, false, false, tickDelta);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0, 0, width, halfHeight);

        RenderSystem.viewport(0, 0, width, halfHeight);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);

        // Aspect oranını, gerçek (yarım yükseklikteki) viewport'a göre kendimiz hesaplıyoruz
        float aspect = (float) width / (float) halfHeight;
        Matrix4f projectionMatrix = new Matrix4f().perspective(
                (float) Math.toRadians(70.0),
                aspect,
                0.05f,
                512.0f
        );

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

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        RenderSystem.viewport(0, 0, width, client.getWindow().getFramebufferHeight());
    }

    private static void drawDividerLine(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int middleY = screenHeight / 2;

        context.fill(0, middleY - 1, screenWidth, middleY + 1, 0xFFFFFFFF);
    }
}
