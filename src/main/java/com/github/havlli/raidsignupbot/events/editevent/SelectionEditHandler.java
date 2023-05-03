package com.github.havlli.raidsignupbot.events.editevent;

import com.github.havlli.raidsignupbot.component.MemberSizeSelectMenu;
import com.github.havlli.raidsignupbot.component.RaidSelectMenu;
import com.github.havlli.raidsignupbot.component.SelectMenuComponent;
import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedGenerator;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedPreview;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

public class SelectionEditHandler extends EditHandler {

    private final EnumSet<EditField> handledFields;
    public SelectionEditHandler(
            EditHandler successor,
            SelectMenuInteractionEvent event,
            EmbedEvent.Builder builder,
            EmbedGenerator generator
    ) {
        super(successor,event, builder, generator);
        this.handledFields = populateHandledFields();
    }

    private EnumSet<EditField> populateHandledFields() {
        return EnumSet.of(
                EditField.INSTANCES,
                EditField.MEMBER_SIZE
        );
    }

    @Override
    public Mono<Message> handleEditEvent(EditField editField) {
        if (handledFields.contains(editField)) {
            String promptMessage;
            switch (editField) {
                case INSTANCES -> {
                    promptMessage = "Choose raid instances!";
                    return editPrompt(promptMessage, new RaidSelectMenu(), updateInstances);
                }
                case MEMBER_SIZE -> {
                    promptMessage = "Choose size of the raid!";
                    return editPrompt(promptMessage, new MemberSizeSelectMenu(), updateMemberSize);
                }
            }
        } else if(successor != null) {
            return successor.handleEditEvent(editField);
        }
        return Mono.empty();
    }
    private Mono<Message> editPrompt(String promptMessage, SelectMenuComponent selectMenuComponent, Function<SelectMenuInteractionEvent, Mono<Message>> updateFunction) {
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        User user = event.getInteraction().getUser();
        return event.edit(InteractionApplicationCommandCallbackSpec.builder()
                        .addComponent(selectMenuComponent.getActionRow())
                        .content(promptMessage)
                        .build())
                .then(eventDispatcher.on(SelectMenuInteractionEvent.class)
                        .filter(event -> event.getInteraction().getUser().equals(user))
                        .filter(event -> event.getCustomId().equals(selectMenuComponent.getCustomId()))
                        .next()
                        .flatMap(updateFunction)
                        .doOnSuccess(System.out::println)
                );
    }

    private final Function<SelectMenuInteractionEvent, Mono<Message>> updateInstances = event -> {
        String values = String.join(", ", event.getValues());
        builder.addInstances(values);
        generator.updatePreviewEmbed(EmbedPreview.Field.INSTANCES.getName(), values);

        String responseText = "Instances selected: " + values;

        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                                .components(List.of())
                                .contentOrNull(responseText)
                        .build()));
    };

    private final Function<SelectMenuInteractionEvent, Mono<Message>> updateMemberSize = event -> {
        String value = event.getValues().get(0);
        builder.addMemberSize(value);
        generator.updatePreviewEmbed(EmbedPreview.Field.MEMBER_SIZE.getName(), value);

        String responseText = "Selected member size: " + value;

        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                        .components(List.of())
                        .contentOrNull(responseText)
                        .build()));
    };
}
