package com.rafalohaki

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Fiabrica : ModInitializer {
    private val logger = LoggerFactory.getLogger("fiabrica")
    override fun onInitialize() {
        logger.info("Hello Fabric world! Hacked Client active!")
    }
}