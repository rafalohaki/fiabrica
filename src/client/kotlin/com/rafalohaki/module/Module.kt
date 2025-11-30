package com.rafalohaki.module

import com.rafalohaki.event.EventBus

/**
 * Base class for all hack modules.
 * Each module can register/unregister itself from EventBus on toggle.
 */
abstract class Module(
    val name: String,
    val description: String,
    val category: Category
) {
    var enabled = false
        private set
    
    /**
     * Toggle module on/off.
     */
    fun toggle() {
        if (enabled) disable() else enable()
    }
    
    /**
     * Enable module (register to EventBus).
     */
    fun enable() {
        if (enabled) return
        enabled = true
        onEnable()
        registerEvents()
    }
    
    /**
     * Disable module (unregister from EventBus).
     */
    fun disable() {
        if (!enabled) return
        enabled = false
        onDisable()
        unregisterEvents()
    }
    
    /**
     * Called when module is enabled.
     * Override to add custom enable logic.
     */
    protected open fun onEnable() {}
    
    /**
     * Called when module is disabled.
     * Override to add custom disable logic.
     */
    protected open fun onDisable() {}
    
    /**
     * Register event handlers.
     * Override to register module-specific events.
     */
    protected open fun registerEvents() {}
    
    /**
     * Unregister event handlers.
     * Override to unregister module-specific events.
     */
    protected open fun unregisterEvents() {}
}

/**
 * Module categories for organization.
 */
enum class Category {
    COMBAT,
    MOVEMENT,
    RENDER,
    PLAYER,
    WORLD,
    MISC
}