package com.dogmod;

import com.dogmod.capability.DogDataManager;
import com.dogmod.command.DogCommand;
import com.dogmod.disguise.DogAnimationHandler;
import com.dogmod.event.DogEventHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
        DogAnimationHandler.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DogCommand.register(dispatcher);
        });
        LOGGER.info("Dog Player Mod hazır!");
    }
}
