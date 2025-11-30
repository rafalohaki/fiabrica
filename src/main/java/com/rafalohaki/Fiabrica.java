package com.rafalohaki;

import net.fabricmc.api.ModInitializer;
import org.slf4j.LoggerFactory;

public class Fiabrica implements ModInitializer {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("fiabrica");
    
    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world! Hacked Client active!");
    }
}