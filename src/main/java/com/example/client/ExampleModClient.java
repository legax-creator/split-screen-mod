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

            // Her ~5 saniyede bir, kaç kumanda bağlı olduğunu ekrana yazar (test amaçlı)
            if (tickCounter % 100 == 0 && client.player != null) {
                int connected = 0;
                for (int j = GLFW.GLFW_JOYSTICK_1; j <= GLFW.GLFW_JOYSTICK_16; j++) {
                    if (GLFW.glfwJoystickPresent(j)) connected++;
                }
                client.player.sendMessage(Text.of("[SplitScreen] Bagli kumanda sayisi: " + connected), false);
            }

            for (int joystickId = GLFW.GLFW_JOYSTICK_1; joystickId <= GLFW.GLFW_JOYSTICK_16; joystickId++) {
                if (!GLFW.glfwJoystickPresent(joystickId)) continue;

                GLFWGamepadState state = GLFWGamepadState.create();
                boolean isGamepad = GLFW.glfwGetGamepadState(joystickId, state);

                boolean startPressed;
                if (isGamepad) {
                    startPressed = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_START) == GLFW.GLFW_PRESS;
                } else {
                    java.nio.ByteBuffer buttons = GLFW.glfwGetJoystickButtons(joystickId);
                    startPressed = buttons != null && buttons.capacity() > 7 && buttons.get(7) == GLFW.GLFW_PRESS;
                }

                if (startPressed && !splitScreenActive) {
                    splitScreenActive = true;
                    if (client.player != null) {
                        client.player.sendMessage(Text.of("[SplitScreen] 2. oyuncu katildi!"), false);
                    }
                    SecondPlayerManager.spawnPlayer2(client.getServer());
                }
            }
        });
    }
}
