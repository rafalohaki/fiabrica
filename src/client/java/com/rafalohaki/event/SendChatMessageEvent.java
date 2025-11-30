package com.rafalohaki.event;

/**
 * Fired when player sends chat message.
 */
public class SendChatMessageEvent extends CancellableEvent {
    private final String message;
    
    public SendChatMessageEvent(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}