package com.rafalohaki.event;

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