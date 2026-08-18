package com.dogmod;

import net.fabricmc.api.ClientModInitializer;

public class DogModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DogMod.LOGGER.info("Dog Player Mod client hazır!");
    }
}
