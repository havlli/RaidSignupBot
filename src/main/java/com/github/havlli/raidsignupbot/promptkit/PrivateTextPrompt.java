package com.github.havlli.raidsignupbot.promptkit;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;

public class PrivateTextPrompt implements PromptStep {

    private final ChatInputInteractionEvent event;
    private final String promptMessage;
    private final MessageGarbageCollector garbageCollector;
    private final Consumer<Message> inputHandler;
    private final String errorMessage;

    public PrivateTextPrompt(Builder builder) {
        this.event = builder.event;
        this.promptMessage = builder.promptMessage;
        this.garbageCollector = builder.garbageCollector;
        this.inputHandler = builder.inputHandler;
        this.errorMessage = builder.errorMessage;

    }

    @Override
    public Mono<Message> getMono() {
        User user = event.getInteraction().getUser();
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        Mono<PrivateChannel> privateChannelMono = user.getPrivateChannel();

        return privateChannelMono
                .flatMap(privateChannel -> privateChannel.createMessage(promptMessage))
                .flatMap(previousMessage -> {
                    if (garbageCollector != null) garbageCollector.collectMessage(previousMessage);
                    return eventDispatcher.on(MessageCreateEvent.class)
                            .map(MessageCreateEvent::getMessage)
                            .filter(message -> message.getAuthor().equals(Optional.of(user)))
                            .next()
                            .flatMap(message -> {
                                if (inputHandler != null) inputHandler.accept(message);
                                return Mono.just(message);
                            })
                            .onErrorResume(error -> {
                                if (errorMessage != null) {
                                    return privateChannelMono
                                            .flatMap(channel -> channel.createMessage(errorMessage))
                                            .then(this.getMono());
                                } else {
                                    return Mono.empty();
                                }
                            });
                });
    }

    public static PrivateTextPrompt.Builder builder(ChatInputInteractionEvent event) {
        return new Builder(event);
    }

    static class Builder {
        private final ChatInputInteractionEvent event;
        private String promptMessage;
        private MessageGarbageCollector garbageCollector;
        private Consumer<Message> inputHandler;
        private String errorMessage;

        Builder(ChatInputInteractionEvent event) {
            this.event = event;
        }

        public Builder withPromptMessage(String promptMessage) {
            this.promptMessage = promptMessage;
            return this;
        }

        public Builder withGarbageCollector(MessageGarbageCollector garbageCollector) {
            this.garbageCollector = garbageCollector;
            return this;
        }

        public Builder withInputHandler(Consumer<Message> inputHandler) {
            this.inputHandler = inputHandler;
            return this;
        }

        public Builder withOnErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public PrivateTextPrompt build() {
            return new PrivateTextPrompt(this);
        }
    }
}
