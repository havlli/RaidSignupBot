package com.github.havlli.raidsignupbot.events.editevent;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedGenerator;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedPreview;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

public class TextEditHandler extends EditHandler {
    private final EnumSet<EditField> handledFields;
    private final SelectMenuInteractionEvent event;
    private EmbedEvent.Builder builder;
    private EmbedGenerator generator;

    public TextEditHandler(
            EditHandler successor,
            SelectMenuInteractionEvent event,
            EmbedEvent.Builder builder,
            EmbedGenerator generator
    ) {
        super(successor);
        this.event = event;
        this.builder = builder;
        this.generator = generator;
        this.handledFields = populateHandledFields();
    }

    private EnumSet<EditField> populateHandledFields() {
        return EnumSet.of(
                EditField.NAME,
                EditField.DESCRIPTION,
                EditField.DATE,
                EditField.TIME
        );
    }

    @Override
    public Mono<Message> handleEditEvent(EditField editField) {
        if (handledFields.contains(editField)) {
            switch (editField) {
                case NAME -> {
                    String promptMessage = "Enter new name for event!";
                    return editPrompt(promptMessage, updateName);
                }
                case DESCRIPTION -> {
                    String promptMessage = "Enter new description for event!";
                    return editPrompt(promptMessage, updateDescription);
                }
                case DATE -> {
                    String promptMessage = "Enter new date for event!";
                    return editPrompt(promptMessage, updateDate);
                }
                case TIME -> {
                    String promptMessage = "Enter new time for event!";
                    return editPrompt(promptMessage, updateTime);
                }
            }
        } else if(successor != null) {
            return successor.handleEditEvent(editField);
        }
        return Mono.empty();
    }

    private Mono<Message> editPrompt(String promptMessage, Function<Message, Mono<Message>> updateFunction) {
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        User user = event.getInteraction().getUser();
        return event.edit(InteractionApplicationCommandCallbackSpec.builder()
                        .components(List.of())
                        .content(promptMessage)
                .build())
                .then(eventDispatcher.on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(message -> message.getChannelId().equals(event.getInteraction().getChannelId()))
                        .filter(message -> message.getAuthor()
                                .map(author -> author.getId().equals(user.getId())).orElse(false))
                        .next()
                        .flatMap(updateFunction)
                );
    }

    private final Function<Message, Mono<Message>> updateName = message -> {
        String newValue = message.getContent();
        builder.addName(newValue);
        generator.updatePreviewEmbed(EmbedPreview.Field.NAME.getName(), newValue);
        return Mono.just(message);
    };

    private final Function<Message, Mono<Message>> updateDescription = message -> {
        String newValue = message.getContent();
        builder.addDescription(newValue);
        generator.updatePreviewEmbed(EmbedPreview.Field.DESCRIPTION.getName(), newValue);
        return Mono.just(message);
    };

    private final Function<Message, Mono<Message>> updateDate = message -> {
        String newValue = message.getContent();
        builder.addDate(newValue);
        generator.updatePreviewEmbed(EmbedPreview.Field.DESCRIPTION.getName(), newValue);
        return Mono.just(message);
    };

    private final Function<Message, Mono<Message>> updateTime = message -> {
        String newValue = message.getContent();
        builder.addTime(newValue);
        generator.updatePreviewEmbed(EmbedPreview.Field.DESCRIPTION.getName(), newValue);
        return Mono.just(message);
    };
}
