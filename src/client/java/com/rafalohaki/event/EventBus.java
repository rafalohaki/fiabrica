package com.rafalohaki.event;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Central event bus for all client events.
 * Thread-safe for concurrent registration/unregistration.
 */
public class EventBus {
    private static final EventBus INSTANCE = new EventBus();
    
    private final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<EventListener<?>>> listeners;
    
    private EventBus() {
        this.listeners = new ConcurrentHashMap<>();
    }
    
    public static EventBus getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register a listener for a specific event type.
     */
    public <T extends Event> void register(Class<T> eventClass, Consumer<T> handler) {
        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                .add(new EventListener<>(handler));
    }
    
    /**
     * Register a listener for ClientTickEvent using functional interface.
     */
    public void register(ClientTickEvent.Handler handler) {
        register(ClientTickEvent.class, event -> handler.handle(event));
    }
    
    /**
     * Unregister all listeners for a specific handler.
     */
    public <T extends Event> void unregister(Class<T> eventClass, Consumer<T> handler) {
        CopyOnWriteArrayList<EventListener<?>> eventListeners = listeners.get(eventClass);
        if (eventListeners != null) {
            eventListeners.removeIf(listener -> listener.getHandler().equals(handler));
        }
    }
    
    /**
     * Unregister ClientTickEvent handler.
     */
    public void unregister(ClientTickEvent.Handler handler) {
        unregister(ClientTickEvent.class, event -> handler.handle(event));
    }
    
    /**
     * Post an event to all registered listeners.
     */
    @SuppressWarnings("unchecked")
    public void post(Event event) {
        CopyOnWriteArrayList<EventListener<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener<?> listener : eventListeners) {
                try {
                    ((Consumer<Event>) listener.getHandler()).accept(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Clear all listeners (used for cleanup).
     */
    public void clear() {
        listeners.clear();
    }
    
    /**
     * Wrapper for event handler function.
     */
    private static class EventListener<T extends Event> {
        private final Consumer<T> handler;
        
        public EventListener(Consumer<T> handler) {
            this.handler = handler;
        }
        
        public Consumer<T> getHandler() {
            return handler;
        }
    }
}