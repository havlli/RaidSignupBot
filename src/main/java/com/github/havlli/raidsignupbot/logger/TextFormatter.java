package com.github.havlli.raidsignupbot.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextFormatter implements Formatter{

    @Override
    public String format(Message message) {
        return formatTimestamp(message) + " " + message.getMessage();
    }

    private String formatTimestamp(Message message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime messageTimestamp = message.getTimestamp();

        return "[" + messageTimestamp.format(formatter) + "]";
    }
}
