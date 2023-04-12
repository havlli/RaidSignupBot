package com.github.havlli.raidsignupbot.events.createevent;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventMapper;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventService;
import com.github.havlli.raidsignupbot.signupuser.SignupUser;
import com.github.havlli.raidsignupbot.signupuser.SignupUserService;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.spec.EmbedCreateSpec;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class EmbedBuilder {
    private final List<SignupUser> signupUsers;
    private final EmbedEvent embedEvent;
    private final EmbedEventMapper embedEventMapper;
    private final EmbedEventService embedEventService;
    private final SignupInteractionSubscriber signupInteractionSubscriber;
    private final EmbedFieldSupplier embedFieldSupplier;
    private final EmbedPreview embedPreview;

    public EmbedBuilder(
            EmbedEvent embedEvent,
            EmbedEventService embedEventService,
            SignupUserService signupUserService
    ) {
        this.embedEvent = embedEvent;
        this.embedEventService = embedEventService;
        this.embedEventMapper = new EmbedEventMapper(embedEvent);
        this.signupUsers = embedEvent.getSignupUsers();
        this.signupInteractionSubscriber = new SignupInteractionSubscriber(this, signupUsers, signupUserService);
        this.embedFieldSupplier = new EmbedFieldSupplier(signupUsers);
        this.embedPreview = new EmbedPreview(embedEvent);
    }

    public EmbedEventMapper getMapper() {
        return embedEventMapper;
    }

    private Long getTimestamp() {
        LocalDateTime dateTime = LocalDateTime.of(embedEvent.getDate(), embedEvent.getTime());
        return dateTime.toInstant(ZoneOffset.UTC).getEpochSecond();
    }

    private String getRaidSizeString() {
        return signupUsers.size() + "/" + embedEvent.getMemberSize();
    }

    public EmbedCreateSpec build() {
        String empty = "";
        String leaderAndIdOfEvent = "Leader: %s - ID: %d"
                .formatted(embedEvent.getAuthor(), embedEvent.getEmbedId());

        return EmbedCreateSpec.builder()
                .addField(empty, leaderAndIdOfEvent, false)
                .addField(embedEvent.getName(), empty, false)
                .addField(empty, embedEvent.getDescription(), false)
                .addField(empty, "<t:%d:D>".formatted(getTimestamp()), true)
                .addField(empty, "<t:%d:t>".formatted(getTimestamp()), true)
                .addField(empty, getRaidSizeString(), true)
                .addAllFields(embedFieldSupplier.getPopulatedFields())
                .build();
    }

    public void saveEmbedEvent() {
        embedEventService.addEmbedEvent(embedEvent);
    }

    public EmbedCreateSpec getPreviewEmbed() {
        return embedPreview.buildPreview();
    }

    public List<LayoutComponent> getLayoutComponents() {
        return ButtonLayoutGenerator.generateButtons(embedEvent);
    }

    public void subscribeInteractions(EventDispatcher eventDispatcher) {
        EmbedFields.getFieldsMap().forEach((fieldKey, value) -> {
            String customId = embedEvent.getEmbedId() + "," + fieldKey;
            eventDispatcher.on(ButtonInteractionEvent.class)
                    .filter(event -> event.getCustomId().equals(customId))
                    .flatMap(signupInteractionSubscriber::handleEvent)
                    .subscribe();
        });
    }

    public EmbedEvent getEmbedEvent() {
        return embedEvent;
    }

    public Long getDestinationChannelId() {
        return embedEvent.getDestinationChannelId();
    }
}
