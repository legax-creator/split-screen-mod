package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {
    private boolean player2Joined = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (int joystickId = GLFW.GLFW_JOYSTICK_1; joystickId <= GLFW.GLFW_JOYSTICK_16; joystickId++) {
                if (GLFW.glfwJoystickPresent(joystickId)) {
                    java.nio.ByteBuffer buttons = GLFW.glfwGetJoystickButtons(joystickId);
                    if (buttons != null && buttons.capacity() > 7) {
                        boolean startPressed = buttons.get(7) == GLFW.GLFW_PRESS;
                        if (startPressed && !player2Joined) {
                            player2Joined = true;
                            System.out.println("2. Oyuncu katıldı! Kumanda ID: " + joystickId);
                        }
                    }
                }
            }
        });
    }
}
