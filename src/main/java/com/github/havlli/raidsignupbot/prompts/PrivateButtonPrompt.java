package com.github.havlli.raidsignupbot.prompts;

import com.github.havlli.raidsignupbot.component.ButtonRowComponent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;

public class PrivateButtonPrompt extends BasicPrompt<ButtonInteractionEvent> {

    private final ButtonRowComponent buttonRowComponent;

    public PrivateButtonPrompt(Builder builder) {
        super(
                builder.event,
                builder.promptMessage,
                builder.interactionHandler,
                builder.garbageCollector,
                ButtonInteractionEvent.class
        );
        this.buttonRowComponent = builder.buttonRowComponent;
    }

    @Override
    protected Mono<? extends MessageChannel> interactionChannel() {
        return event.getInteraction().getUser().getPrivateChannel();
    }

    @Override
    protected Mono<Message> sendPrompt(MessageChannel channel) {
        return channel.createMessage(promptMessage.withComponents(buttonRowComponent.getActionRow()));
    }

    @Override
    protected Predicate<? super ButtonInteractionEvent> interactionFilter() {
        User user = event.getInteraction().getUser();
        return currentEvent -> {
            boolean isSameUser = currentEvent.getInteraction().getUser().equals(user);
            boolean areComponents = checkMatches(currentEvent);
            return isSameUser && areComponents;
        };
    }

    private boolean checkMatches(ButtonInteractionEvent event) {
        return buttonRowComponent.getCustomIds()
                .stream()
                .anyMatch(customId -> customId.equals(event.getCustomId()));
    }

    public static PrivateButtonPrompt.Builder builder(ChatInputInteractionEvent event) {
        return new PrivateButtonPrompt.Builder(event);
    }

    public static class Builder extends PromptBuilder<PrivateButtonPrompt.Builder, PrivateButtonPrompt> {
        private Function<ButtonInteractionEvent, Mono<Message>> interactionHandler;
        private ButtonRowComponent buttonRowComponent;

        public Builder(ChatInputInteractionEvent event) {
            super(event);
        }

        @Override
        protected PrivateButtonPrompt.Builder self() {
            return this;
        }

        @Override
        protected PrivateButtonPrompt doBuild() {
            return new PrivateButtonPrompt(this);
        }

        public PrivateButtonPrompt.Builder withInteractionHandler(Function<ButtonInteractionEvent, Mono<Message>> interactionHandler) {
            this.interactionHandler = interactionHandler;
            return this;
        }

        public PrivateButtonPrompt.Builder withButtonRowComponent(ButtonRowComponent buttonRowComponent) {
            this.buttonRowComponent = buttonRowComponent;
            return this;
        }
    }
}
