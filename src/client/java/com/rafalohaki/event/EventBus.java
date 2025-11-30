package com.rafalohaki.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Central event bus for all client events.
 * Thread-safe for concurrent registration/unregistration.
 * 
 * Uses object identity for unregistering handlers (not equals()).
 */
public class EventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger("fiabrica");
    private static final EventBus INSTANCE = new EventBus();
    
    private final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<Object>> listeners;
    
    private EventBus() {
        this.listeners = new ConcurrentHashMap<>();
    }
    
    public static EventBus getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register a listener for a specific event type.
     * The handler object is stored directly for identity-based unregistration.
     */
    public <T extends Event> void register(Class<T> eventClass, Consumer<T> handler) {
        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                .add(handler);
    }
    
    /**
     * Register a listener for ClientTickEvent using functional interface.
     * IMPORTANT: Store the handler reference for later unregistration!
     */
    public void register(ClientTickEvent.Handler handler) {
        listeners.computeIfAbsent(ClientTickEvent.class, k -> new CopyOnWriteArrayList<>())
                .add(handler);
    }
    
    /**
     * Unregister a handler using object identity (==).
     * The handler must be the SAME object reference that was registered.
     */
    public <T extends Event> void unregister(Class<T> eventClass, Object handler) {
        CopyOnWriteArrayList<Object> eventListeners = listeners.get(eventClass);
        if (eventListeners != null) {
            eventListeners.removeIf(listener -> listener == handler);
        }
    }
    
    /**
     * Unregister ClientTickEvent handler using object identity.
     */
    public void unregister(ClientTickEvent.Handler handler) {
        CopyOnWriteArrayList<Object> eventListeners = listeners.get(ClientTickEvent.class);
        if (eventListeners != null) {
            eventListeners.removeIf(listener -> listener == handler);
        }
    }
    
    /**
     * Post an event to all registered listeners.
     */
    @SuppressWarnings("unchecked")
    public void post(Event event) {
        CopyOnWriteArrayList<Object> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (Object listener : eventListeners) {
                try {
                    if (listener instanceof Consumer) {
                        ((Consumer<Event>) listener).accept(event);
                    } else if (event instanceof ClientTickEvent && listener instanceof ClientTickEvent.Handler) {
                        ((ClientTickEvent.Handler) listener).handle((ClientTickEvent) event);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error posting event {}: {}", event.getClass().getSimpleName(), e.getMessage());
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
     * Get the number of listeners for debugging.
     */
    public int getListenerCount() {
        return listeners.values().stream().mapToInt(CopyOnWriteArrayList::size).sum();
    }
}