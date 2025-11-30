package com.rafalohaki.module.modules;

import com.rafalohaki.event.ClientTickEvent;
import com.rafalohaki.event.EventBus;
import com.rafalohaki.module.Category;
import com.rafalohaki.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Speed module - increases player movement speed.
 */
public class SpeedModule extends Module {
    private float speedMultiplier = 1.5f;
    private final ClientTickEvent.Handler tickHandler = this::onTick;
    
    public SpeedModule() {
        super("Speed", "Increases movement speed", Category.MOVEMENT);
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
        
        // Simple speed boost when on ground and moving
        if (mc.player.isOnGround()) {
            // Check if player is moving by checking velocity
            double velX = mc.player.getVelocity().x;
            double velZ = mc.player.getVelocity().z;
            if (velX != 0 || velZ != 0) {
                mc.player.setVelocity(
                    velX * speedMultiplier,
                    mc.player.getVelocity().y,
                    velZ * speedMultiplier
                );
            }
        }
    }
    
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }
    
    public void setSpeedMultiplier(float value) {
        speedMultiplier = Math.max(1.0f, Math.min(3.0f, value));
    }
}
