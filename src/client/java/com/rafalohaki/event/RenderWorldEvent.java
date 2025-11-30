package com.rafalohaki.event;

import net.minecraft.client.MinecraftClient;

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