package com.rafalohaki.event

/**
 * Base class for all events.
 */
abstract class Event

/**
 * Cancellable event base class.
 */
abstract class CancellableEvent : Event() {
    var cancelled = false
        private set
    
    fun cancel() {
        cancelled = true
    }
}