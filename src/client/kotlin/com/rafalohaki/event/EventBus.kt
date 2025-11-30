package com.rafalohaki.event

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Central event bus for all client events.
 * Thread-safe for concurrent registration/unregistration.
 */
object EventBus {
    @PublishedApi
    internal val listeners = ConcurrentHashMap<Class<out Event>, CopyOnWriteArrayList<EventListener<*>>>()
    
    /**
     * Register a listener for a specific event type.
     */
    inline fun <reified T : Event> register(noinline handler: (T) -> Unit) {
        val eventClass = T::class.java
        listeners.getOrPut(eventClass) { CopyOnWriteArrayList() }
            .add(EventListener(handler as (Event) -> Unit))
    }
    
    /**
     * Unregister all listeners for a specific handler.
     */
    inline fun <reified T : Event> unregister(noinline handler: (T) -> Unit) {
        val eventClass = T::class.java
        listeners[eventClass]?.removeIf { it.handler == handler }
    }
    
    /**
     * Post an event to all registered listeners.
     */
    fun post(event: Event) {
        listeners[event::class.java]?.forEach { listener ->
            try {
                @Suppress("UNCHECKED_CAST")
                (listener.handler as (Event) -> Unit)(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Clear all listeners (used for cleanup).
     */
    fun clear() {
        listeners.clear()
    }
}

/**
 * Wrapper for event handler function.
 */
data class EventListener<T : Event>(val handler: (T) -> Unit)