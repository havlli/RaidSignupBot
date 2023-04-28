package com.github.havlli.raidsignupbot.prompts;

import com.github.havlli.raidsignupbot.component.ButtonRowComponent;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class PrivateButtonPrompt extends Prompt {
    private final Function<ButtonInteractionEvent, Mono<Message>> interactionHandler;
    private final ButtonRowComponent buttonRowComponent;

    public PrivateButtonPrompt(Builder builder) {
        super(builder.event, builder.promptMessage, builder.garbageCollector);
        this.interactionHandler = builder.interactionHandler;
        this.buttonRowComponent = builder.buttonRowComponent;
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

    public static class Builder extends PromptBuilder<Builder, PrivateButtonPrompt> {
        private Function<ButtonInteractionEvent, Mono<Message>> interactionHandler;
        private ButtonRowComponent buttonRowComponent;

        public Builder(ChatInputInteractionEvent event) {
            super(event);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected PrivateButtonPrompt doBuild() {
            return new PrivateButtonPrompt(this);
        }

        public Builder withInteractionHandler(Function<ButtonInteractionEvent, Mono<Message>> interactionHandler) {
            this.interactionHandler = interactionHandler;
            return this;
        }

        public Builder withButtonRowComponent(ButtonRowComponent buttonRowComponent) {
            this.buttonRowComponent = buttonRowComponent;
            return this;
        }
    }
}
