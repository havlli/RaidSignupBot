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

public class PrivateSelectPrompt extends Prompt {
    private final Function<SelectMenuInteractionEvent, Mono<Message>> interactionHandler;
    private final SelectMenuComponent selectMenuComponent;

    public PrivateSelectPrompt(Builder builder) {
        super(builder.event, builder.promptMessage, builder.garbageCollector);
        this.interactionHandler = builder.interactionHandler;
        this.selectMenuComponent = builder.selectMenuComponent;
    }

    @Override
    public Mono<Message> getMono() {
        User user = event.getInteraction().getUser();
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        Mono<PrivateChannel> privateChannelMono = user.getPrivateChannel();

        return privateChannelMono
                .flatMap(channel -> channel.createMessage(promptMessage
                        .withComponents(selectMenuComponent.getActionRow())))
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

    public static class Builder extends PromptBuilder<Builder, PrivateSelectPrompt> {
        private Function<SelectMenuInteractionEvent, Mono<Message>> interactionHandler;
        private SelectMenuComponent selectMenuComponent;

        private Builder(ChatInputInteractionEvent event) {
            super(event);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected PrivateSelectPrompt doBuild() {
            return new PrivateSelectPrompt(this);
        }

        public Builder withInteractionHandler(Function<SelectMenuInteractionEvent, Mono<Message>> interactionHandler) {
            this.interactionHandler = interactionHandler;
            return this;
        }

        public Builder withSelectMenuComponent(SelectMenuComponent selectMenuComponent) {
            this.selectMenuComponent = selectMenuComponent;
            return this;
        }
    }
}
