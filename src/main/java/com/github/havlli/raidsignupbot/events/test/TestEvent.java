package com.github.havlli.raidsignupbot.events.test;

import com.github.havlli.raidsignupbot.events.EventHandler;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.TextInput;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

public class TestEvent implements EventHandler {
    @Override
    public Class<? extends Event> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Message> handleEvent(Event event) {
        MessageCreateEvent messageCreateEvent = (MessageCreateEvent) event;

        TextInput textInput = TextInput.small("textinput","Label");

        if (messageCreateEvent.getMessage().getContent().equals("test")) {
            return messageCreateEvent.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage(MessageCreateSpec
                            .builder()
                                    .content("Testing")
                            .build()));
        }

        return Mono.empty();
    }
}
