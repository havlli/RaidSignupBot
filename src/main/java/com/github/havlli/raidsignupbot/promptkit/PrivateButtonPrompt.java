package com.github.havlli.raidsignupbot.promptkit;

import com.github.havlli.raidsignupbot.component.ButtonRowComponent;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.InteractionReplyEditSpec;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

public class PrivateButtonPrompt implements PromptStep {

    private final ChatInputInteractionEvent event;
    private final String name;
    private final String promptMessage;
    private final Consumer<ButtonInteractionEvent> buttonHandler;
    private final ButtonRowComponent buttonRowComponent;
    private final MessageGarbageCollector garbageCollector;

    public PrivateButtonPrompt(Builder builder) {
        this.event = builder.event;
        this.name = builder.name;
        this.promptMessage = builder.promptMessage;
        this.buttonHandler = builder.buttonHandler;
        this.buttonRowComponent = builder.buttonRowComponent;
        this.garbageCollector = builder.garbageCollector;
    }

    @Override
    public Mono<Message> getMono() {
        User user = event.getInteraction().getUser();
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        Mono<PrivateChannel> privateChannelMono = user.getPrivateChannel();

        return privateChannelMono
                .flatMap(channel -> channel.createMessage(promptMessage)
                        .withComponents(buttonRowComponent.getActionRow()))
                .flatMap(message -> {
                    collectGarbage(message);
                    return eventDispatcher.on(ButtonInteractionEvent.class)
                            .filter(event -> event.getInteraction().getUser().equals(user))
                            .filter(this::checkMatches)
                            .next()
                            .flatMap(event -> {
                                buttonHandler.accept(event);
                                return event.deferEdit()
                                        .then(event.editReply(InteractionReplyEditSpec.builder()
                                                .components(List.of())
                                                .contentOrNull(formatContent(event.getCustomId()))
                                                .build()));
                            });
                });
    }

    private boolean checkMatches(ButtonInteractionEvent event) {
        return buttonRowComponent.getCustomIds()
                .stream()
                .anyMatch(customId -> customId.equals(event.getCustomId()));
    }

    private String formatContent(String value) {

        if (name == null) {
            return "You have selected " + value;
        } else {
            return name + ": " + value;
        }
    }

    private void collectGarbage(Message message) {
        if (garbageCollector != null) garbageCollector.collectMessage(message);
    }

    public static Builder builder(ChatInputInteractionEvent event) {
        return new Builder(event);
    }

    static class Builder {
        private final ChatInputInteractionEvent event;
        private String name;
        private String promptMessage;
        private Consumer<ButtonInteractionEvent> buttonHandler;
        private ButtonRowComponent buttonRowComponent;
        private MessageGarbageCollector garbageCollector;

        public Builder(ChatInputInteractionEvent event) {
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

        public Builder withButtonHandler(Consumer<ButtonInteractionEvent> buttonHandler) {
            this.buttonHandler = buttonHandler;
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
