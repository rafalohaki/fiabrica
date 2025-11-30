package com.rafalohaki.event;

/**
 * Base class for all events.
 */
public abstract class Event {
}

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