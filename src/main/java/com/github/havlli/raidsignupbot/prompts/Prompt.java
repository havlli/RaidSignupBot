package com.github.havlli.raidsignupbot.prompts;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

public abstract class Prompt {
    protected final ChatInputInteractionEvent event;
    protected final MessageCreateSpec promptMessage;
    protected final MessageGarbageCollector garbageCollector;

    public Prompt(ChatInputInteractionEvent event,
                  MessageCreateSpec promptMessage,
                  MessageGarbageCollector garbageCollector) {
        this.event = event;
        this.promptMessage = promptMessage;
        this.garbageCollector = garbageCollector;
    }

    public abstract Mono<Message> getMono();
}
