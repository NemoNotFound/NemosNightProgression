package com.nemonotfound.nemos.night.progression;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NemosNightProgression implements ModInitializer {

    public static final String MOD_ID = "nemos_night_progression";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Thank you for using Nemo's Night Progression");
    }
}
