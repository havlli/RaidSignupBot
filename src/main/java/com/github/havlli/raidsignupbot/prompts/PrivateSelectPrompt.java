package com.github.havlli.raidsignupbot.prompts;

import com.github.havlli.raidsignupbot.component.SelectMenuComponent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;

public class PrivateSelectPrompt extends BasicPrompt<SelectMenuInteractionEvent> {
    private final SelectMenuComponent selectMenuComponent;

    public PrivateSelectPrompt(Builder builder) {
        super(
                builder.event,
                builder.promptMessage,
                builder.interactionHandler,
                builder.garbageCollector,
                SelectMenuInteractionEvent.class
        );
        this.selectMenuComponent = builder.selectMenuComponent;
    }

    @Override
    protected Mono<? extends MessageChannel> interactionChannel() {
        return event.getInteraction().getUser().getPrivateChannel();
    }

    @Override
    protected Mono<Message> sendPrompt(MessageChannel channel) {
        return channel.createMessage(promptMessage.withComponents(selectMenuComponent.getActionRow()));
    }

    @Override
    protected Predicate<? super SelectMenuInteractionEvent> interactionFilter() {
        User user = event.getInteraction().getUser();
        return currentEvent -> {
            boolean isSameUser = currentEvent.getInteraction().getUser().equals(user);
            boolean isComponent = currentEvent.getCustomId().equals(selectMenuComponent.getCustomId());
            return isSameUser && isComponent;
        };
    }

    public static PrivateSelectPrompt.Builder builder(ChatInputInteractionEvent event) {
        return new PrivateSelectPrompt.Builder(event);
    }

    public static class Builder extends PromptBuilder<PrivateSelectPrompt.Builder, PrivateSelectPrompt> {
        private Function<SelectMenuInteractionEvent, Mono<Message>> interactionHandler;
        private SelectMenuComponent selectMenuComponent;

        private Builder(ChatInputInteractionEvent event) {
            super(event);
        }

        @Override
        protected PrivateSelectPrompt.Builder self() {
            return this;
        }

        @Override
        protected PrivateSelectPrompt doBuild() {
            return new PrivateSelectPrompt(this);
        }

        public PrivateSelectPrompt.Builder withInteractionHandler(Function<SelectMenuInteractionEvent, Mono<Message>> interactionHandler) {
            this.interactionHandler = interactionHandler;
            return this;
        }

        public PrivateSelectPrompt.Builder withSelectMenuComponent(SelectMenuComponent selectMenuComponent) {
            this.selectMenuComponent = selectMenuComponent;
            return this;
        }
    }
}
