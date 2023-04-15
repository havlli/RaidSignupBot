package com.github.havlli.raidsignupbot.embedgenerator;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;

public class EmbedFormatter {

    public static String raidSize(EmbedEvent embedEvent) {
        return embedEvent.getSignupUsers().size() + "/" + embedEvent.getMemberSize();
    }

    public static String leaderWithEmbedId(EmbedEvent embedEvent) {
        return "Leader: %s - ID: %s"
                .formatted(embedEvent.getAuthor(), embedEvent.getEmbedId());
    }

    public static String date(Long timestamp) {
        return "<t:%d:D>".formatted(timestamp);
    }

    public static String time(Long timestamp) {
        return "<t:%d:t>".formatted(timestamp);
    }
}
