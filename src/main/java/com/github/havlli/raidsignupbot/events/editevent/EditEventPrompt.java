package com.github.havlli.raidsignupbot.events.editevent;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.component.ButtonRow;
import com.github.havlli.raidsignupbot.component.EditEventSelectMenu;
import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedGenerator;
import com.github.havlli.raidsignupbot.logger.Logger;
import com.github.havlli.raidsignupbot.prompts.MessageGarbageCollector;
import com.github.havlli.raidsignupbot.prompts.PrivateButtonPrompt;
import com.github.havlli.raidsignupbot.prompts.PrivateSelectPrompt;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

public class EditEventPrompt {

    private final ChatInputInteractionEvent event;
    private final Message targetMessage;
    private final MessageGarbageCollector garbageCollector;
    private final EmbedEvent.Builder builder;
    private final EmbedGenerator embedGenerator;
    private final Logger logger;

    public EditEventPrompt(ChatInputInteractionEvent event, Message targetMessage, EmbedGenerator embedGenerator) {
        this.event = event;
        this.targetMessage = targetMessage;
        this.embedGenerator = embedGenerator;
        this.builder = fetchBuilder();
        this.logger = Dependencies.getInstance().getLogger();
        this.garbageCollector = new MessageGarbageCollector(logger);
    }

    public Mono<Message> initiateEditEvent() {

        Mono<Message> chainedHandlerResult = PrivateSelectPrompt.builder(event)
                .withGarbageCollector(garbageCollector)
                .withPromptMessage(MessageCreateSpec.builder()
                        .addEmbed(embedGenerator.generatePreviewEmbed(builder))
                        .content("What would you like to change?")
                        .build())
                .withSelectMenuComponent(new EditEventSelectMenu())
                .withInteractionHandler(event -> {
                    String option = event.getValues().get(0);
                    EditField editField = EditField.fromStringValue(option);

                    return chainedHandler(editField, event, builder);
                })
                .build()
                .getMono();

        return chainedHandlerResult
                .flatMap(ignored -> confirmChangesMono());
    }

    private Mono<Message> confirmChangesMono() {
        System.out.println("confirmChangesMono: " + builder.getName());
        return PrivateButtonPrompt.builder(event)
                .withGarbageCollector(garbageCollector)
                .withPromptMessage(MessageCreateSpec.builder()
                        .addEmbed(embedGenerator.generatePreviewEmbed(builder))
                        .content("Confirm changes")
                        .build())
                .withButtonRowComponent(ButtonRow.builder()
                        .addButton("confirm", "Confirm", ButtonRow.Builder.buttonType.PRIMARY)
                        .addButton("more", "More changes", ButtonRow.Builder.buttonType.SECONDARY)
                        .addButton("cancel", "Cancel", ButtonRow.Builder.buttonType.DANGER)
                        .build())
                .withInteractionHandler(buttonAction -> {
                    String customId = buttonAction.getCustomId();
                    return switch (customId) {
                        case "confirm" -> Mono.empty();
                        case "more" -> initiateEditEvent();
                        case "cancel" -> Mono.empty();
                        default -> Mono.empty();
                    };
                })
                .build()
                .getMono();
    }

    private EmbedEvent.Builder fetchBuilder() {
        String embedEventId = targetMessage.getId().asString();
        EmbedEvent embedEvent = Dependencies.getInstance()
                .getEmbedEventPersistence()
                .getEmbedEventById(embedEventId)
                .orElse(null);

        if (embedEvent != null) return new EmbedEvent.Builder(embedEvent);

        return EmbedEvent.builder();
    }

    private Mono<Message> chainedHandler(EditField editField, SelectMenuInteractionEvent event, EmbedEvent.Builder builder) {
        EditHandler nameEditHandler = new NameEditHandler(null);
        return nameEditHandler.handleEditEvent(editField, event, builder);
    }
}
