package com.rafalohaki.event;

/**
 * Cancellable event base class.
 */
public abstract class CancellableEvent extends Event {
    private boolean cancelled = false;
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void cancel() {
        cancelled = true;
    }
}