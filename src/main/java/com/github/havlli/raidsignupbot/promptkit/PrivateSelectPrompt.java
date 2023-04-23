package com.github.havlli.raidsignupbot.promptkit;

import com.github.havlli.raidsignupbot.component.SelectMenuComponent;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.InteractionReplyEditSpec;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

public class PrivateSelectPrompt implements PromptStep {

    private final ChatInputInteractionEvent event;
    private final String name;
    private final String promptMessage;
    private final Consumer<List<String>> inputHandler;
    private final SelectMenuComponent selectMenuComponent;
    private final MessageGarbageCollector garbageCollector;

    public PrivateSelectPrompt(Builder builder) {
        this.event = builder.event;
        this.name = builder.name;
        this.promptMessage = builder.promptMessage;
        this.inputHandler = builder.inputHandler;
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
                    if (garbageCollector != null) garbageCollector.collectMessage(message);
                    return eventDispatcher.on(SelectMenuInteractionEvent.class)
                            .filter(event -> event.getInteraction().getUser().equals(user))
                            .filter(event -> event.getCustomId().equals(selectMenuComponent.getCustomId()))
                            .next()
                            .flatMap(event -> {
                                inputHandler.accept(event.getValues());
                                return event.deferEdit()
                                        .then(event.editReply(InteractionReplyEditSpec.builder()
                                                .components(List.of())
                                                .contentOrNull(formatContent(event.getValues()))
                                                .build()));
                            });
                });
    }

    private String formatContent(List<String> values) {
        String concatValues = String.join(", ", values);
        if (name == null) {
            return "You have selected " + concatValues;
        } else {
            return name + ": " + concatValues;
        }
    }

    public static Builder builder(ChatInputInteractionEvent event) {
        return new Builder(event);
    }

    static class Builder {
        private final ChatInputInteractionEvent event;
        private String name;
        private String promptMessage;
        private Consumer<List<String>> inputHandler;
        private SelectMenuComponent selectMenuComponent;
        private MessageGarbageCollector garbageCollector;

        private Builder(ChatInputInteractionEvent event) {
            this.event = event;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withPromptMessage(String promptMessage) {
            this.promptMessage = promptMessage;
            return this;
        }

        public Builder withInputHandler(Consumer<List<String>> inputHandler) {
            this.inputHandler = inputHandler;
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
