package com.github.havlli.raidsignupbot.events.editevent;

import com.github.havlli.raidsignupbot.component.ButtonRow;
import com.github.havlli.raidsignupbot.component.ButtonRowComponent;
import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedGenerator;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedPreview;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

public class ButtonEditHandler extends EditHandler {

    private final EnumSet<EditField> handledFields;

    public ButtonEditHandler(
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
                EditField.RESERVE
        );
    }

    @Override
    public Mono<Message> handleEditEvent(EditField editField) {
        if (handledFields.contains(editField)) {
            String promptMessage;
            if (editField == EditField.RESERVE) {
                promptMessage = "Choose soft-reserve option!";
                ButtonRow buttonRow = ButtonRow.builder()
                        .addButton("enabled","Enable", ButtonRow.Builder.buttonType.PRIMARY)
                        .addButton("disabled", "Disable", ButtonRow.Builder.buttonType.SECONDARY)
                        .build();
                return editPrompt(promptMessage, buttonRow, updateReserve);
            }
        } else if(successor != null) {
            return successor.handleEditEvent(editField);
        }
        return Mono.empty();
    }

    private Mono<Message> editPrompt(String promptMessage, ButtonRowComponent buttonRowComponent, Function<ButtonInteractionEvent, Mono<Message>> updateFunction) {
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        User user = event.getInteraction().getUser();
        return event.edit(InteractionApplicationCommandCallbackSpec.builder()
                        .addComponent(buttonRowComponent.getActionRow())
                        .content(promptMessage)
                        .build())
                .then(eventDispatcher.on(ButtonInteractionEvent.class)
                        .filter(event -> event.getInteraction().getUser().equals(user))
                        .filter(event -> buttonRowComponent.getCustomIds().contains(event.getCustomId()))
                        .next()
                        .flatMap(updateFunction)
                        .doOnSuccess(System.out::println)
                );
    }

    private final Function<ButtonInteractionEvent, Mono<Message>> updateReserve = event -> {
        String customId = event.getCustomId();
        if (customId.equals("enabled")) {
            builder.addReservingEnabled(true);
            generator.updatePreviewEmbed(EmbedPreview.Field.RESERVE.getName(), "Enabled");
        } else {
            builder.addReservingEnabled(false);
            generator.updatePreviewEmbed(EmbedPreview.Field.RESERVE.getName(), "Disabled");
        }

        String responseText = "Soft-reserve " + customId;

        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                        .components(List.of())
                        .contentOrNull(responseText)
                        .build()));
    };
}
