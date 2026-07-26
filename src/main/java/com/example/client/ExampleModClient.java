package com.example.client;

import com.example.SecondPlayerManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

public class ExampleModClient implements ClientModInitializer {
    public static boolean splitScreenActive = false;
    private static int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        SplitScreenRenderer.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;

            int joystickCount = 0;
            for (int j = GLFW.GLFW_JOYSTICK_1; j <= GLFW.GLFW_JOYSTICK_16; j++) {
                if (GLFW.glfwJoystickPresent(j)) joystickCount++;
            }

            if (tickCounter % 100 == 0 && client.player != null) {
                client.player.sendMessage(Text.literal("[SplitScreen] Bagli kumanda sayisi: " + joystickCount), false);
            }

            // 1. kumanda: Start tusuyla split-screen'i baslat
            if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1)) {
                java.nio.ByteBuffer buttons = GLFW.glfwGetJoystickButtons(GLFW.GLFW_JOYSTICK_1);
                if (buttons != null && buttons.capacity() > 7 && buttons.get(7) == GLFW.GLFW_PRESS && !splitScreenActive) {
                    splitScreenActive = true;
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("[SplitScreen] 2. oyuncu katildi!"), false);
                    }

                    // Artik sunucuya degil, dogrudan client dunyasina ekliyoruz
                    if (client.world != null && client.player != null) {
                        SecondPlayerManager.spawnPlayer2(client.world, client.player);
                    }
                }
            }

            // 2. kumanda: hareket
            if (splitScreenActive && GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_2)) {
                GLFWGamepadState state = GLFWGamepadState.create();
                if (GLFW.glfwGetGamepadState(GLFW.GLFW_JOYSTICK_2, state)) {
                    float moveX = state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X);
                    float moveY = state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y);
                    float lookX = state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X);

                    double deadzone = 0.2;
                    double strafe = Math.abs(moveX) > deadzone ? moveX * 0.2 : 0;
                    // Not: joystick yukari itildiginde eksen negatif olur, bu yuzden ters ceviriyoruz
                    double forward = Math.abs(moveY) > deadzone ? -moveY * 0.2 : 0;
                    float yawDelta = Math.abs(lookX) > deadzone ? lookX * 3.0f : 0;

                    if (strafe != 0 || forward != 0 || yawDelta != 0) {
                        SecondPlayerManager.movePlayer2(forward, strafe, yawDelta);
                    }
                }
            }
        });
    }
}
