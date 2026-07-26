package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (int joystickId = GLFW.GLFW_JOYSTICK_1; joystickId <= GLFW.GLFW_JOYSTICK_16; joystickId++) {
                if (GLFW.glfwJoystickPresent(joystickId)) {
                    String name = GLFW.glfwGetJoystickName(joystickId);
                    System.out.println("Kumanda bulundu: " + name + " (ID: " + joystickId + ")");
                }
            }
        });
    }
}
