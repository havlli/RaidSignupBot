package com.github.havlli.raidsignupbot.prompts;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;

public class PrivateTextPrompt extends Prompt {
    private final Consumer<Message> inputHandler;
    private final String errorMessage;

    public PrivateTextPrompt(Builder builder) {
        super(builder.event, builder.promptMessage, builder.garbageCollector);
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
                    collectGarbage(previousMessage);
                    return eventDispatcher.on(MessageCreateEvent.class)
                            .map(MessageCreateEvent::getMessage)
                            .filter(message -> message.getAuthor().equals(Optional.of(user)))
                            .next()
                            .flatMap(message -> {
                                if (inputHandler != null) inputHandler.accept(message);
                                return Mono.just(message);
                            })
                            .onErrorResume(error -> handleError(privateChannelMono));
                });
    }

    private Mono<Message> handleError(Mono<PrivateChannel> privateChannelMono) {

        if (errorMessage == null) return Mono.empty();

        return privateChannelMono
                .flatMap(channel -> channel.createMessage(errorMessage)
                        .flatMap(message -> {
                            collectGarbage(message);
                            return Mono.just(message);
                        }))
                .then(this.getMono());
    }

    private void collectGarbage(Message message) {
        if (garbageCollector != null) garbageCollector.collectMessage(message);
    }

    public static PrivateTextPrompt.Builder builder(ChatInputInteractionEvent event) {
        return new Builder(event);
    }

    public static class Builder extends PromptBuilder<Builder, PrivateTextPrompt> {
        private Consumer<Message> inputHandler;
        private String errorMessage;

        private Builder(ChatInputInteractionEvent event) {
            super(event);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected PrivateTextPrompt doBuild() {
            return new PrivateTextPrompt(this);
        }

        public Builder withInputHandler(Consumer<Message> inputHandler) {
            this.inputHandler = inputHandler;
            return this;
        }

        public Builder withOnErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
    }
}
