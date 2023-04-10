package com.github.havlli.raidsignupbot.logger;

import java.time.LocalDateTime;

public class Message {
    private final String message;
    private final LocalDateTime timestamp;

    public Message(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
