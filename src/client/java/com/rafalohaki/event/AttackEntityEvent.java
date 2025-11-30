package com.rafalohaki.event;

import net.minecraft.entity.Entity;

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