package com.rafalohaki.event;

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