package com.rafalohaki.event;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

/**
 * Fired every client tick (20 times per second).
 */
public class ClientTickEvent extends Event {
    @FunctionalInterface
    public interface Handler {
        void handle(ClientTickEvent event);
    }
}

/**
 * Fired when rendering the world.
 */
public class RenderWorldEvent extends Event {
    private final MinecraftClient client;
    private final float partialTicks;
    
    public RenderWorldEvent(MinecraftClient client, float partialTicks) {
        this.client = client;
        this.partialTicks = partialTicks;
    }
    
    public MinecraftClient getClient() {
        return client;
    }
    
    public float getPartialTicks() {
        return partialTicks;
    }
}

/**
 * Fired when player moves.
 */
public class PlayerMoveEvent extends CancellableEvent {
    private final double x;
    private final double y;
    private final double z;
    
    public PlayerMoveEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getZ() {
        return z;
    }
}

/**
 * Fired when player attacks entity.
 */
public class AttackEntityEvent extends CancellableEvent {
    private final Entity target;
    
    public AttackEntityEvent(Entity target) {
        this.target = target;
    }
    
    public Entity getTarget() {
        return target;
    }
}

/**
 * Fired when rendering HUD/overlay.
 */
public class RenderHudEvent extends Event {
    private final float partialTicks;
    
    public RenderHudEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }
    
    public float getPartialTicks() {
        return partialTicks;
    }
}

/**
 * Fired when player sends chat message.
 */
public class SendChatMessageEvent extends CancellableEvent {
    private final String message;
    
    public SendChatMessageEvent(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}