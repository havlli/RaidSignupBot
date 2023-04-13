package com.github.havlli.raidsignupbot.embedevent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class EmbedEventValidator {

    public static boolean isExpired(EmbedEvent embedEvent) {
        ZoneId utcZoneId = ZoneId.of("UTC");

        LocalDateTime eventDateTime = LocalDateTime.of(embedEvent.getDate(), embedEvent.getTime());
        ZonedDateTime zonedDateTime = ZonedDateTime.of(eventDateTime, utcZoneId);

        return zonedDateTime.isBefore(ZonedDateTime.now(utcZoneId));
    }
}
