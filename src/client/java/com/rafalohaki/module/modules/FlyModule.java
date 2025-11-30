package com.rafalohaki.module.modules;

import com.rafalohaki.event.ClientTickEvent;
import com.rafalohaki.event.EventBus;
import com.rafalohaki.module.Category;
import com.rafalohaki.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Example Fly hack module.
 * Allows player to fly in survival mode.
 */
public class FlyModule extends Module {
    private float speed = 1.0f;
    private final ClientTickEvent.Handler tickHandler = event -> onTick(event);
    
    public FlyModule() {
        super("Fly", "Allows you to fly in survival mode", Category.MOVEMENT);
    }
    
    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.getAbilities() != null) {
            mc.player.getAbilities().allowFlying = true;
            mc.player.getAbilities().flying = true;
        }
    }
    
    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.getAbilities() != null) {
            mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
    }
    
    @Override
    protected void registerEvents() {
        EventBus.getInstance().register(tickHandler);
    }
    
    @Override
    protected void unregisterEvents() {
        EventBus.getInstance().unregister(tickHandler);
    }
    
    private void onTick(ClientTickEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        // Custom fly speed logic
        if (mc.player.getAbilities().flying) {
            mc.player.getAbilities().setFlySpeed(0.05f * speed);
        }
    }
    
    public float getSpeed() {
        return speed;
    }
    
    /**
     * Set fly speed (called from GUI).
     */
    public void setSpeed(float newSpeed) {
        speed = Math.max(0.1f, Math.min(5.0f, newSpeed));
    }
}