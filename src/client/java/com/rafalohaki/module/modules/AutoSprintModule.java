package com.rafalohaki.module.modules;

import com.rafalohaki.event.ClientTickEvent;
import com.rafalohaki.event.EventBus;
import com.rafalohaki.module.Category;
import com.rafalohaki.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * AutoSprint module - automatically sprints when moving forward.
 */
public class AutoSprintModule extends Module {
    private final ClientTickEvent.Handler tickHandler = this::onTick;
    
    public AutoSprintModule() {
        super("AutoSprint", "Automatically sprint when moving", Category.MOVEMENT);
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
        
        // Auto sprint when moving forward (check velocity Z for forward movement)
        boolean isMovingForward = mc.player.getVelocity().z != 0 || mc.player.getVelocity().x != 0;
        
        if (isMovingForward && 
            !mc.player.isSneaking() && 
            !mc.player.horizontalCollision &&
            mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
