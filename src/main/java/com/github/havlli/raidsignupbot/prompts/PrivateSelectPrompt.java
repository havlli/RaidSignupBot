package com.github.havlli.raidsignupbot.prompts;

import com.github.havlli.raidsignupbot.component.SelectMenuComponent;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class PrivateSelectPrompt implements Prompt {

    private final ChatInputInteractionEvent event;
    private final String promptMessage;
    private final Function<SelectMenuInteractionEvent, Mono<Message>> interactionHandler;
    private final SelectMenuComponent selectMenuComponent;
    private final MessageGarbageCollector garbageCollector;

    public PrivateSelectPrompt(Builder builder) {
        this.event = builder.event;
        this.promptMessage = builder.promptMessage;
        this.interactionHandler = builder.interactionHandler;
        this.selectMenuComponent = builder.selectMenuComponent;
        this.garbageCollector = builder.garbageCollector;
    }

    @Override
    public Mono<Message> getMono() {
        User user = event.getInteraction().getUser();
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        Mono<PrivateChannel> privateChannelMono = user.getPrivateChannel();

        return privateChannelMono
                .flatMap(channel -> channel.createMessage(promptMessage)
                        .withComponents(selectMenuComponent.getActionRow()))
                .flatMap(message -> {
                    collectGarbage(message);
                    return eventDispatcher.on(SelectMenuInteractionEvent.class)
                            .filter(event -> event.getInteraction().getUser().equals(user))
                            .filter(event -> event.getCustomId().equals(selectMenuComponent.getCustomId()))
                            .next()
                            .flatMap(event -> {
                                if (interactionHandler != null) return interactionHandler.apply(event);
                                return Mono.empty();
                            });
                });
    }

    private void collectGarbage(Message message) {
        if (garbageCollector != null) garbageCollector.collectMessage(message);
    }

    public static Builder builder(ChatInputInteractionEvent event) {
        return new Builder(event);
    }

    public static class Builder {
        private final ChatInputInteractionEvent event;
        private String promptMessage;
        private Function<SelectMenuInteractionEvent, Mono<Message>> interactionHandler;
        private SelectMenuComponent selectMenuComponent;
        private MessageGarbageCollector garbageCollector;

        private Builder(ChatInputInteractionEvent event) {
            this.event = event;
        }

        public Builder withPromptMessage(String promptMessage) {
            this.promptMessage = promptMessage;
            return this;
        }

        public Builder withInteractionHandler(Function<SelectMenuInteractionEvent, Mono<Message>> interactionHandler) {
            this.interactionHandler = interactionHandler;
            return this;
        }

        public Builder withSelectMenuComponent(SelectMenuComponent selectMenuComponent) {
            this.selectMenuComponent = selectMenuComponent;
            return this;
        }

        public Builder withGarbageCollector(MessageGarbageCollector garbageCollector) {
            this.garbageCollector = garbageCollector;
            return this;
        }

        public PrivateSelectPrompt build() {
            return new PrivateSelectPrompt(this);
        }
    }
}
