package com.paylog.app;

public class ChatMessage {
    private String message; // Content of the message
    private String sender;  // Sender of the message

    public ChatMessage(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }
}
