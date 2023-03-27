package com.github.havlli.raidsignupbot.events.deleteevent;

import com.github.havlli.raidsignupbot.events.EventHandler;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class DeleteEvent implements EventHandler {
    @Override
    public Class<? extends Event> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<?> handleEvent(Event event) {
        ChatInputInteractionEvent interactionEvent = (ChatInputInteractionEvent) event;
        if (interactionEvent.getCommandName().equals("delete-event")) {

            return interactionEvent.deferReply().withEphemeral(true).then(deferredMessage(interactionEvent));
        }
        return Mono.empty();
    }

    private static Mono<Message> deferredMessage(ChatInputInteractionEvent event) {
        return event.getInteraction().getChannel()
                .flatMap(messageChannel -> {
                    String messageId = event.getOption("message-id")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString)
                            .orElse("1");
                    return messageChannel.getMessageById(Snowflake.of(messageId))
                            .flatMap(message -> {
                                Optional<User> author = message.getAuthor();
                                if (author.isPresent() && author.get().getId().equals(event.getClient().getSelfId())) {
                                    return message.delete()
                                            .then(event.createFollowup("Event deleted!")
                                                    .withEphemeral(true));
                                } else {
                                    return event.createFollowup("Event not found, already deleted or not posted by this bot!")
                                            .withEphemeral(true);
                                }
                            })
                            .onErrorResume(e -> event.createFollowup("Event not found!")
                                    .withEphemeral(true));
                });
    }
}
