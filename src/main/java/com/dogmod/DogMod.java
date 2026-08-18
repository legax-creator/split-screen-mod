package com.dogmod;

import com.dogmod.capability.DogDataManager;
import com.dogmod.event.DogEventHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DogMod implements ModInitializer {
    public static final String MOD_ID = "dogmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Dog Player Mod başlatılıyor...");
        DogDataManager.register();
        DogEventHandler.register();
        LOGGER.info("Dog Player Mod hazır!");
    }
}
