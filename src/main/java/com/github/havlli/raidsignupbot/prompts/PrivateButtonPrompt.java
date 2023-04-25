package com.github.havlli.raidsignupbot.prompts;

import com.github.havlli.raidsignupbot.component.ButtonRowComponent;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class PrivateButtonPrompt implements PromptStep {

    private final ChatInputInteractionEvent event;
    private final MessageCreateSpec promptMessage;
    private final Function<ButtonInteractionEvent, Mono<Message>> interactionHandler;
    private final ButtonRowComponent buttonRowComponent;
    private final MessageGarbageCollector garbageCollector;

    public PrivateButtonPrompt(Builder builder) {
        this.event = builder.event;
        this.promptMessage = builder.promptMessage;
        this.interactionHandler = builder.interactionHandler;
        this.buttonRowComponent = builder.buttonRowComponent;
        this.garbageCollector = builder.garbageCollector;
    }

    @Override
    public Mono<Message> getMono() {
        User user = event.getInteraction().getUser();
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        Mono<PrivateChannel> privateChannelMono = user.getPrivateChannel();

        return privateChannelMono
                .flatMap(channel -> channel.createMessage(promptMessage
                        .withComponents(buttonRowComponent.getActionRow())))
                .flatMap(message -> {
                    collectGarbage(message);
                    return eventDispatcher.on(ButtonInteractionEvent.class)
                            .filter(event -> event.getInteraction().getUser().equals(user))
                            .filter(this::checkMatches)
                            .next()
                            .flatMap(event -> {
                                if (interactionHandler != null) return interactionHandler.apply(event);
                                return Mono.empty();
                            });
                });
    }

    private boolean checkMatches(ButtonInteractionEvent event) {
        return buttonRowComponent.getCustomIds()
                .stream()
                .anyMatch(customId -> customId.equals(event.getCustomId()));
    }

    private void collectGarbage(Message message) {
        if (garbageCollector != null) garbageCollector.collectMessage(message);
    }

    public static Builder builder(ChatInputInteractionEvent event) {
        return new Builder(event);
    }

    public static class Builder {
        private final ChatInputInteractionEvent event;
        private MessageCreateSpec promptMessage;
        private Function<ButtonInteractionEvent, Mono<Message>> interactionHandler;
        private ButtonRowComponent buttonRowComponent;
        private MessageGarbageCollector garbageCollector;

        public Builder(ChatInputInteractionEvent event) {
            this.event = event;
        }

        public Builder withPromptMessage(String promptMessage) {
            this.promptMessage = MessageCreateSpec.builder()
                    .content(promptMessage)
                    .build();
            return this;
        }

        public Builder withPromptMessage(MessageCreateSpec promptMessage) {
            this.promptMessage = promptMessage;
            return this;
        }

        public Builder withInteractionHandler(Function<ButtonInteractionEvent, Mono<Message>> interactionHandler) {
            this.interactionHandler = interactionHandler;
            return this;
        }

        public Builder withButtonRowComponent(ButtonRowComponent buttonRowComponent) {
            this.buttonRowComponent = buttonRowComponent;
            return this;
        }

        public Builder withGarbageCollector(MessageGarbageCollector garbageCollector) {
            this.garbageCollector = garbageCollector;
            return this;
        }

        public PrivateButtonPrompt build() {
            return new PrivateButtonPrompt(this);
        }
    }
}
