package com.github.havlli.raidsignupbot.events.createevent;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventService;
import com.github.havlli.raidsignupbot.signupuser.SignupUserService;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.spec.EmbedCreateSpec;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class EmbedBuilder {
    private final EmbedEventService embedEventService;
    private final SignupInteractionSubscriber signupInteractionSubscriber;
    private final EmbedFieldSupplier embedFieldSupplier;
    private final EmbedPreview embedPreview;

    public EmbedBuilder(
            EmbedEventService embedEventService,
            SignupUserService signupUserService
    ) {
        this.embedEventService = embedEventService;
        this.signupInteractionSubscriber = new SignupInteractionSubscriber(this, signupUserService);
        this.embedFieldSupplier = new EmbedFieldSupplier();
        this.embedPreview = new EmbedPreview();
    }

    private Long getTimestamp(EmbedEvent embedEvent) {
        LocalDateTime dateTime = LocalDateTime.of(embedEvent.getDate(), embedEvent.getTime());
        return dateTime.toInstant(ZoneOffset.UTC).getEpochSecond();
    }

    private String getRaidSizeString(EmbedEvent embedEvent) {
        return embedEvent.getSignupUsers().size() + "/" + embedEvent.getMemberSize();
    }

    public EmbedCreateSpec generateEmbed(EmbedEvent embedEvent) {
        String empty = "";
        String leaderAndIdOfEvent = "Leader: %s - ID: %s"
                .formatted(embedEvent.getAuthor(), embedEvent.getEmbedId());
        Long timestamp = getTimestamp(embedEvent);

        return EmbedCreateSpec.builder()
                .addField(empty, leaderAndIdOfEvent, false)
                .addField(embedEvent.getName(), empty, false)
                .addField(empty, embedEvent.getDescription(), false)
                .addField(empty, "<t:%d:D>".formatted(timestamp), true)
                .addField(empty, "<t:%d:t>".formatted(timestamp), true)
                .addField(empty, getRaidSizeString(embedEvent), true)
                .addAllFields(embedFieldSupplier.getPopulatedFields(embedEvent))
                .build();
    }

    public void saveEmbedEvent(EmbedEvent embedEvent) {
        embedEventService.addEmbedEvent(embedEvent);
    }

    public EmbedCreateSpec getPreviewEmbed(EmbedEvent.EmbedEventBuilder embedEventBuilder) {
        return embedPreview.buildPreview(embedEventBuilder);
    }

    public List<LayoutComponent> getLayoutComponents(EmbedEvent embedEvent) {
        return ButtonLayoutGenerator.generateButtons(embedEvent);
    }

    public void subscribeInteractions(EventDispatcher eventDispatcher, EmbedEvent embedEvent) {
        EmbedFields.getFieldsMap().forEach((fieldKey, value) -> {
            String customId = embedEvent.getEmbedId() + "," + fieldKey;
            eventDispatcher.on(ButtonInteractionEvent.class)
                    .filter(event -> event.getCustomId().equals(customId))
                    .flatMap(interaction -> signupInteractionSubscriber.handleEvent(interaction, embedEvent))
                    .subscribe();
        });
    }
}
