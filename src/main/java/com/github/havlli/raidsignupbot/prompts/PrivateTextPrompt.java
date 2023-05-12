package com.github.havlli.raidsignupbot.prompts;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class PrivateTextPrompt extends BasicPrompt<MessageCreateEvent> {
    private final String errorMessage;
    public PrivateTextPrompt(Builder builder) {
        super(
                builder.event,
                builder.promptMessage,
                builder.interactionHandler,
                builder.garbageCollector,
                MessageCreateEvent.class
        );
        this.errorMessage = builder.errorMessage;
    }

    @Override
    protected Mono<? extends MessageChannel> interactionChannel() {
        return event.getInteraction().getUser().getPrivateChannel();
    }

    @Override
    protected Mono<Message> sendPrompt(MessageChannel channel) {
        return channel.createMessage(promptMessage);
    }

    @Override
    protected Predicate<? super MessageCreateEvent> interactionFilter() {
        Optional<User> user = Optional.of(event.getInteraction().getUser());
        return event -> event.getMessage().getAuthor().equals(user);
    }

    @Override
    protected Mono<Message> handleErrors(Throwable error) {
        if (errorMessage == null) return Mono.empty();

        return interactionChannel()
                .flatMap(channel -> channel.createMessage(errorMessage)
                        .flatMap(message -> {
                            collectGarbage(message);
                            return Mono.just(message);
                        })
                )
                .then(this.getMono());
    }

    public static PrivateTextPrompt.Builder builder(ChatInputInteractionEvent event) {
        return new PrivateTextPrompt.Builder(event);
    }

    public static class Builder extends PromptBuilder<PrivateTextPrompt.Builder, PrivateTextPrompt> {
        private Function<MessageCreateEvent, Mono<Message>> interactionHandler;
        private String errorMessage;

        private Builder(ChatInputInteractionEvent event) {
            super(event);
        }

        @Override
        protected PrivateTextPrompt.Builder self() {
            return this;
        }

        @Override
        protected PrivateTextPrompt doBuild() {
            return new PrivateTextPrompt(this);
        }

        public PrivateTextPrompt.Builder withInteractionHandler(Function<MessageCreateEvent, Mono<Message>> interactionHandler) {
            this.interactionHandler = interactionHandler;
            return this;
        }

        public PrivateTextPrompt.Builder withOnErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
    }
}
