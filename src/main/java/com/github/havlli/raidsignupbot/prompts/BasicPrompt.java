package com.github.havlli.raidsignupbot.prompts;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class BasicPrompt<T extends Event> implements Prompt {
    protected final ChatInputInteractionEvent event;
    protected final MessageCreateSpec promptMessage;
    protected final MessageGarbageCollector garbageCollector;
    private final Class<T> eventClass;
    private final Function<T, Mono<Message>> interactionHandler;

    public BasicPrompt(
            ChatInputInteractionEvent event,
            MessageCreateSpec promptMessage,
            Function<T, Mono<Message>> interactionHandler,
            MessageGarbageCollector garbageCollector,
            Class<T> eventClass
    ) {
        this.event = event;
        this.promptMessage = promptMessage;
        this.interactionHandler = interactionHandler;
        this.garbageCollector = garbageCollector;
        this.eventClass = eventClass;
    }

    @Override
    public Mono<Message> getMono() {
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();

        return interactionChannel()
                .flatMap(this::sendPrompt)
                .flatMap(previousMessage -> {
                    collectGarbage(previousMessage);
                    return eventDispatcher.on(eventClass)
                            .filter(interactionFilter())
                            .next()
                            .flatMap(this::handleInteraction)
                            .onErrorResume(this::handleErrors);
                });
    }

    protected abstract Mono<? extends MessageChannel> interactionChannel();
    protected abstract Mono<Message> sendPrompt(MessageChannel channel);
    protected abstract Predicate<? super T> interactionFilter();
    protected Mono<Message> handleInteraction(T event) {
        if (interactionHandler != null) return interactionHandler.apply(event);
        return Mono.empty();
    }

    protected Mono<Message> handleErrors(Throwable error) {
        return Mono.empty();
    }

    protected void collectGarbage(Message message) {
        if (garbageCollector != null) garbageCollector.collectMessage(message);
    }
}
