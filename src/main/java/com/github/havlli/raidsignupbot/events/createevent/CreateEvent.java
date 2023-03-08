package com.github.havlli.raidsignupbot.events.createevent;

import com.github.havlli.raidsignupbot.events.EventHandler;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class CreateEvent implements EventHandler {
    @Override
    public Class<? extends Event> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<?> handleEvent(Event event) {
        ChatInputInteractionEvent interactionEvent = (ChatInputInteractionEvent) event;
        if (interactionEvent.getCommandName().equals("create-event")) {

            return interactionEvent.deferReply().withEphemeral(true).then(deferredMessage(interactionEvent));
        }
        return Mono.empty();
    }

    private static Mono<Message> deferredMessage(ChatInputInteractionEvent event) {

        SignupCreation.setNameOfEvent(event.getInteraction().getUser(), event);
        return event.createFollowup("Initiated process of creating event in your DMs, please continue there!")
                .withEphemeral(true);
    }
}
