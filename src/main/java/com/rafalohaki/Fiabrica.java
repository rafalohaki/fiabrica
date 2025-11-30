package com.rafalohaki;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod initializer for Fiabrica.
 * This runs on both client and server (common code).
 */
public class Fiabrica implements ModInitializer {
    public static final String MOD_ID = "fiabrica";
    public static final String MOD_NAME = "Fiabrica";
    public static final String VERSION = "1.0.0";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    @Override
    public void onInitialize() {
        LOGGER.info("===========================================");
        LOGGER.info("  {} v{} initializing...", MOD_NAME, VERSION);
        LOGGER.info("===========================================");
        
        // Common initialization code (runs on both client and server)
        // Client-specific code is in FiabricaClient
        
        LOGGER.info("{} common initialization complete!", MOD_NAME);
    }
}