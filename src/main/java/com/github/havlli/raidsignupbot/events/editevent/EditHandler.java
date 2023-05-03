package com.github.havlli.raidsignupbot.events.editevent;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedGenerator;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public abstract class EditHandler {

    protected EditHandler successor;
    protected EmbedEvent.Builder builder;
    protected EmbedGenerator generator;
    protected SelectMenuInteractionEvent event;
    public EditHandler(EditHandler successor,
                       SelectMenuInteractionEvent event,
                       EmbedEvent.Builder builder,
                       EmbedGenerator generator) {
        this.successor = successor;
        this.event = event;
        this.builder = builder;
        this.generator = generator;
    }

    public abstract Mono<Message> handleEditEvent(EditField editField);
}
