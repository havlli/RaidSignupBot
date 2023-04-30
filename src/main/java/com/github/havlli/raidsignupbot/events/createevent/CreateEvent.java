package com.github.havlli.raidsignupbot.events.createevent;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedGenerator;
import com.github.havlli.raidsignupbot.events.EventHandler;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class CreateEvent implements EventHandler {

    private static final String COMMAND_NAME = "create-event";
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

    private static Mono<Message> deferredMessage(ChatInputInteractionEvent event) {

        EmbedGenerator embedGenerator = new EmbedGenerator(
                Dependencies.getInstance().getEmbedEventService(),
                Dependencies.getInstance().getSignupUserService()
        );
        Snowflake guild = event.getInteraction().getGuildId().orElse(Snowflake.of("0"));
        CreateEventPrompt createEventPrompt = new CreateEventPrompt(event, embedGenerator, guild);

        return event.createFollowup("Initiated process of creating event in your DMs, please continue there!")
                .withEphemeral(true)
                .flatMap(message -> createEventPrompt.getMono());
    }
}
