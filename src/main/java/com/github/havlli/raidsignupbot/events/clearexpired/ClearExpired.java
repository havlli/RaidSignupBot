package com.github.havlli.raidsignupbot.events.clearexpired;

import com.github.havlli.raidsignupbot.events.EventHandler;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

public class ClearExpired implements EventHandler {

    private static final String COMMAND_NAME = "clear-expired";
    @Override
    public Class<? extends Event> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<?> handleEvent(Event event) {
        ChatInputInteractionEvent interactionEvent = (ChatInputInteractionEvent) event;
        if (interactionEvent.getCommandName().equals(COMMAND_NAME)) {
            return interactionEvent.deferReply()
                    .withEphemeral(true)
                    .then(deferredMessage(interactionEvent));
        }

        return Mono.empty();
    }

    private Mono<Message> deferredMessage(ChatInputInteractionEvent event) {
        return event.getInteraction().getChannel()
                .flatMapMany(messageChannel -> messageChannel.getMessagesAfter(Snowflake.of(0))
                    .filter(message -> message.getAuthor().map(User::isBot).orElse(false))
                    .filter(this::isExpired)
                    .flatMap(message -> message.delete().thenReturn(message))
                )
                .collectList()
                .flatMap(messages -> {
                    String response;
                    int count = messages.size();
                    if (count > 0) {
                        String messageWord = count == 1 ? "message" : "messages";
                        response = String.format("Deleted %d %s in this channel.", count, messageWord);
                    } else response = "No expired messages found in this channel.";

                    return event.createFollowup(response);
                });
    }

    private boolean isExpired(Message message) {
        return message.getComponents().stream()
                .flatMap(components -> components.getChildren().stream())
                .filter(messageComponent -> messageComponent instanceof SelectMenu)
                .map(messageComponent -> (SelectMenu) messageComponent)
                .anyMatch(selectMenu -> selectMenu.getCustomId().equals("expired-menu"));
    }
}
