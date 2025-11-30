package com.rafalohaki.module.modules

import com.rafalohaki.event.ClientTickEvent
import com.rafalohaki.event.EventBus
import com.rafalohaki.module.Category
import com.rafalohaki.module.Module
import net.minecraft.client.MinecraftClient

/**
 * Example Fly hack module.
 * Allows player to fly in survival mode.
 */
class FlyModule : Module(
    name = "Fly",
    description = "Allows you to fly in survival mode",
    category = Category.MOVEMENT
) {
    var speed = 1.0f
        private set
    
    private val tickHandler: (ClientTickEvent) -> Unit = { event ->
        onTick()
    }
    
    override fun onEnable() {
        MinecraftClient.getInstance().player?.abilities?.apply {
            allowFlying = true
            flying = true
        }
    }
    
    override fun onDisable() {
        MinecraftClient.getInstance().player?.abilities?.apply {
            allowFlying = false
            flying = false
        }
    }
    
    override fun registerEvents() {
        EventBus.register(tickHandler)
    }
    
    override fun unregisterEvents() {
        EventBus.unregister(tickHandler)
    }
    
    private fun onTick() {
        val player = MinecraftClient.getInstance().player ?: return
        
        // Custom fly speed logic
        if (player.abilities.flying) {
            player.abilities.flySpeed = 0.05f * speed
        }
    }
    
    /**
     * Set fly speed (called from GUI).
     */
    fun setSpeed(newSpeed: Float) {
        speed = newSpeed.coerceIn(0.1f, 5.0f)
    }
}