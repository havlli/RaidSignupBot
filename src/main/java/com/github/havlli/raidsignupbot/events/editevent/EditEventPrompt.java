package com.github.havlli.raidsignupbot.events.editevent;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.component.ButtonRow;
import com.github.havlli.raidsignupbot.component.EditEventSelectMenu;
import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedGenerator;
import com.github.havlli.raidsignupbot.logger.Logger;
import com.github.havlli.raidsignupbot.prompts.InteractionFormatter;
import com.github.havlli.raidsignupbot.prompts.MessageGarbageCollector;
import com.github.havlli.raidsignupbot.prompts.PrivateButtonPrompt;
import com.github.havlli.raidsignupbot.prompts.PrivateSelectPrompt;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Mono;

import java.util.List;

public class EditEventPrompt {

    private final ChatInputInteractionEvent event;
    private final Message targetMessage;
    private final EmbedEvent targetEmbedEvent;
    private final MessageGarbageCollector garbageCollector;
    private final EmbedEvent.Builder builder;
    private final EmbedGenerator embedGenerator;
    private final Snowflake guildId;

    public EditEventPrompt(
            ChatInputInteractionEvent event,
            Message targetMessage,
            EmbedGenerator embedGenerator,
            Snowflake guildId
    ) {
        this.event = event;
        this.targetMessage = targetMessage;
        this.embedGenerator = embedGenerator;
        this.guildId = guildId;
        this.targetEmbedEvent = fetchTargetEmbedEvent();
        this.builder = fetchBuilder();
        Logger logger = Dependencies.getInstance().getLogger();
        this.garbageCollector = new MessageGarbageCollector(logger);
    }

    public Mono<Message> initiateEditEvent() {
        Mono<PrivateChannel> privateChannelMono = event.getInteraction().getUser().getPrivateChannel();

        return PrivateSelectPrompt.builder(event)
                .withGarbageCollector(garbageCollector)
                .withPromptMessage(MessageCreateSpec.builder()
                        .content("What would you like to change?")
                        .build())
                .withSelectMenuComponent(new EditEventSelectMenu())
                .withInteractionHandler(event -> {
                    String option = event.getValues().get(0);
                    EditField editField = EditField.fromStringValue(option);

                    return chainedHandlers(editField, event);
                })
                .build()
                .getMono()
                .flatMap(message -> confirmChangesMono())
                .onErrorResume(error -> privateChannelMono
                        .flatMap(channel -> channel.createMessage("Invalid format! Try again with correct format.")
                                .flatMap(message -> {
                                    garbageCollector.collectMessage(message);
                                    return Mono.just(message);
                                }))
                        .then(initiateEditEvent())
                );
    }

    private Mono<Message> confirmChangesMono() {
        return PrivateButtonPrompt.builder(event)
                .withPromptMessage(MessageCreateSpec.builder()
                        .addEmbed(embedGenerator.generatePreviewEmbed(builder))
                        .build())
                .withButtonRowComponent(ButtonRow.builder()
                        .addButton("confirm", "Confirm", ButtonRow.Builder.buttonType.PRIMARY)
                        .addButton("more", "More changes", ButtonRow.Builder.buttonType.SECONDARY)
                        .addButton("cancel", "Cancel", ButtonRow.Builder.buttonType.DANGER)
                        .build())
                .withInteractionHandler(buttonAction -> {
                    String customId = buttonAction.getCustomId();
                    return switch (customId) {
                        case "confirm" -> saveChanges(buttonAction);
                        case "more" -> editMoreChain(buttonAction);
                        case "cancel" -> Mono.empty();
                        default -> Mono.empty();
                    };
                })
                .build()
                .getMono();
    }

    private Mono<Message> editMoreChain(ButtonInteractionEvent event) {
        Mono<MessageChannel> messageChannelMono = event.getInteraction().getChannel();
        return event.deferEdit()
                .then(garbageCollector.cleanup(messageChannelMono))
                .then(event.deleteReply())
                .then(initiateEditEvent())
                .then(Mono.empty());
    }

    private Mono<Message> saveChanges(ButtonInteractionEvent event) {
        Mono<MessageChannel> messageChannelMono = event.getInteraction().getChannel();
        InteractionFormatter formatter = new InteractionFormatter();

        EmbedEvent embedEvent = builder.build();
        embedEvent.setSignupUsers(targetEmbedEvent.getSignupUsers());
        embedGenerator.updateEmbedEvent(embedEvent);

        Snowflake destinationChannelId = Snowflake.of(embedEvent.getDestinationChannelId());
        Snowflake messageId = targetMessage.getId();

        Mono<Message> editTargetMessage = this.event.getInteraction().getGuild()
                .flatMap(guild -> guild.getChannelById(destinationChannelId)
                        .cast(MessageChannel.class)
                        .flatMap(channel -> channel.getMessageById(messageId))
                        .flatMap(message -> message.edit(MessageEditSpec.builder()
                                        .contentOrNull(null)
                                        .addEmbed(embedGenerator.generateEmbed(embedEvent))
                                        .build())
                        )
                );

        String messageURL = formatter.messageURL(guildId,destinationChannelId,messageId);
        String response = "Event %s changed successfully!".formatted(messageURL);

        return event.deferEdit()
                .then(garbageCollector.cleanup(messageChannelMono))
                .then(event.editReply(InteractionReplyEditSpec.builder()
                                .contentOrNull(response)
                                .embeds(List.of())
                                .components(List.of())
                        .build())
                        .flatMap(message -> editTargetMessage)
                );
    }

    private EmbedEvent.Builder fetchBuilder() {
        if (this.targetEmbedEvent != null) return new EmbedEvent.Builder(this.targetEmbedEvent);
        return EmbedEvent.builder();
    }

    private EmbedEvent fetchTargetEmbedEvent() {
        String embedEventId = targetMessage.getId().asString();
        return Dependencies.getInstance()
                .getEmbedEventPersistence()
                .getEmbedEventById(embedEventId)
                .orElse(null);
    }

    private Mono<Message> chainedHandlers(EditField editField, SelectMenuInteractionEvent event) {
        EditHandler nameEditHandler = new TextEditHandler(null, event, builder, embedGenerator);
        EditHandler selectionEditHandler = new SelectionEditHandler(nameEditHandler, event, builder, embedGenerator);
        EditHandler buttonEditHandler = new ButtonEditHandler(selectionEditHandler, event, builder, embedGenerator);
        return buttonEditHandler.handleEditEvent(editField);
    }
}
