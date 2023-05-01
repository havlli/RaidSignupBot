package com.github.havlli.raidsignupbot.events.editevent;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

import java.util.List;

public class NameEditHandler extends EditHandler {

    public NameEditHandler(EditHandler successor) {
        super(successor);
    }

    @Override
    public Mono<Message> handleEditEvent(EditField editField, SelectMenuInteractionEvent event, EmbedEvent.Builder builder) {
        String fieldToHandle = EditField.NAME.getStringValue();
        if (editField.getStringValue().equals(fieldToHandle)) {
            System.out.println("Handling name edit");
            return editNameMono(event, builder);
        } else if(successor != null) {
            return successor.handleEditEvent(editField, event, builder);
        }
        return Mono.empty();
    }

    private Mono<Message> editNameMono(SelectMenuInteractionEvent event, EmbedEvent.Builder builder) {
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        User user = event.getInteraction().getUser();
        return event.edit(InteractionApplicationCommandCallbackSpec.builder()
                        .components(List.of())
                        .content("Enter new name for event!")
                .build())
                .then(eventDispatcher.on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(message -> message.getChannelId().equals(event.getInteraction().getChannelId()))
                        .filter(message -> message.getAuthor()
                                .map(author -> author.getId().equals(user.getId())).orElse(false))
                        .next()
                        .flatMap(message -> {
                            builder.addName(message.getContent());

                            System.out.println("editNameMono : " + builder.getName());
                            return Mono.just(message).cast(Message.class);
                        })
                );
    }
}
