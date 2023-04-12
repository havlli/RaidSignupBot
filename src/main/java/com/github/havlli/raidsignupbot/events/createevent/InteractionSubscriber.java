package com.github.havlli.raidsignupbot.events.createevent;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public interface InteractionSubscriber {
    Mono<Message> handleEvent(ButtonInteractionEvent event);
}
