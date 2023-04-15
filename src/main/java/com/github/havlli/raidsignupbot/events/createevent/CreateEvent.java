package com.github.havlli.raidsignupbot.events.createevent;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedGenerator;
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

        EmbedGenerator embedGenerator = new EmbedGenerator(
                Dependencies.getInstance().getEmbedEventService(),
                Dependencies.getInstance().getSignupUserService()
        );
        SignupBuilder signupBuilder = new SignupBuilder(event, embedGenerator);
        signupBuilder.startBuildProcess();

        return event.createFollowup("Initiated process of creating event in your DMs, please continue there!")
                .withEphemeral(true);
    }
}
