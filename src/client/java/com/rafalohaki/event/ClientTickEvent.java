package com.rafalohaki.event;

/**
 * Fired every client tick (20 times per second).
 */
public class ClientTickEvent extends Event {
    @FunctionalInterface
    public interface Handler {
        void handle(ClientTickEvent event);
    }
}