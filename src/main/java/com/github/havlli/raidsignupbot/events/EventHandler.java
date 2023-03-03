package com.github.havlli.raidsignupbot.events;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface EventHandler {
    Class<? extends Event> getEventType();
    Mono<?> handleEvent(Event event);

}
