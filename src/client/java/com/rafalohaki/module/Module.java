package com.rafalohaki.module;

import com.rafalohaki.event.EventBus;

/**
 * Base class for all hack modules.
 * Each module can register/unregister itself from EventBus on toggle.
 */
public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled = false;
    
    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Toggle module on/off.
     */
    public void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }
    
    /**
     * Enable module (register to EventBus).
     */
    public void enable() {
        if (enabled) return;
        enabled = true;
        onEnable();
        registerEvents();
    }
    
    /**
     * Disable module (unregister from EventBus).
     */
    public void disable() {
        if (!enabled) return;
        enabled = false;
        onDisable();
        unregisterEvents();
    }
    
    /**
     * Called when module is enabled.
     * Override to add custom enable logic.
     */
    protected void onEnable() {}
    
    /**
     * Called when module is disabled.
     * Override to add custom disable logic.
     */
    protected void onDisable() {}
    
    /**
     * Register event handlers.
     * Override to register module-specific events.
     */
    protected void registerEvents() {}
    
    /**
     * Unregister event handlers.
     * Override to unregister module-specific events.
     */
    protected void unregisterEvents() {}
}