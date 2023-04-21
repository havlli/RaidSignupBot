package com.github.havlli.raidsignupbot.promptkit;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;

public class PrivateTextPrompt implements PromptStep {

    private final ChatInputInteractionEvent event;
    private final String promptMessage;
    private final MessageGarbageCollector messageGarbageCollector;
    private Consumer<Message> inputHandler;

    public PrivateTextPrompt(
            ChatInputInteractionEvent event,
            String promptMessage,
            MessageGarbageCollector messageGarbageCollector
    ) {
        this.event = event;
        this.promptMessage = promptMessage;
        this.messageGarbageCollector = messageGarbageCollector;
        this.inputHandler = null;

    }

    public PrivateTextPrompt withInputHandler(Consumer<Message> inputHandler) {
        this.inputHandler = inputHandler;
        return this;
    }

    @Override
    public Mono<Message> getMono() {
        User user = event.getInteraction().getUser();
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        Mono<PrivateChannel> privateChannelMono = user.getPrivateChannel();

        return privateChannelMono
                .flatMap(privateChannel -> privateChannel.createMessage(promptMessage))
                .flatMap(previousMessage -> {
                    messageGarbageCollector.collectMessage(previousMessage);
                    return eventDispatcher.on(MessageCreateEvent.class)
                            .map(MessageCreateEvent::getMessage)
                            .filter(message -> message.getAuthor().equals(Optional.of(user)))
                            .next()
                            .flatMap(message -> {
                                if (inputHandler != null) inputHandler.accept(message);
                                return Mono.just(message);
                            });
                });
    }
}
