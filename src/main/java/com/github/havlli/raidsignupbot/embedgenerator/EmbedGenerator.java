package com.github.havlli.raidsignupbot.embedgenerator;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventService;
import com.github.havlli.raidsignupbot.signupuser.SignupUserService;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.Disposable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class EmbedGenerator {
    private final EmbedEventService embedEventService;
    private final SignupInteractionSubscriber signupInteractionSubscriber;
    private final EmbedFieldSupplier embedFieldSupplier;
    private final EmbedPreview embedPreview;
    private final String DELIMITER = ",";

    public EmbedGenerator(
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

    public EmbedCreateSpec generateEmbed(EmbedEvent embedEvent) {
        String empty = "";
        String leaderWithEmbedId = EmbedFormatter.leaderWithEmbedId(embedEvent);
        String raidSize = EmbedFormatter.raidSize(embedEvent);
        String date = EmbedFormatter.date(getTimestamp(embedEvent));
        String time = EmbedFormatter.time(getTimestamp(embedEvent));

        return EmbedCreateSpec.builder()
                .addField(empty, leaderWithEmbedId, false)
                .addField(embedEvent.getName(), empty, false)
                .addField(empty, embedEvent.getDescription(), false)
                .addField(empty, date, true)
                .addField(empty, time, true)
                .addField(empty, raidSize, true)
                .addAllFields(embedFieldSupplier.getPopulatedFields(embedEvent))
                .build();
    }

    public void acceptEmbedEvent(EmbedEvent embedEvent) {
        embedEventService.addEmbedEvent(embedEvent);
    }

    public void updateEmbedEvent(EventDispatcher eventDispatcher, EmbedEvent newEmbedEvent) {
        EmbedEvent oldEmbedEvent = embedEventService.updateEmbedEvent(newEmbedEvent);
        unsubscribeInteractions(oldEmbedEvent);
        subscribeInteractions(eventDispatcher, newEmbedEvent);
    }
    public EmbedCreateSpec generatePreviewEmbed(EmbedEvent.Builder embedEventBuilder) {
        return embedPreview.buildPreview(embedEventBuilder);
    }

    public void updatePreviewEmbed(String name, String newValue) {
        embedPreview.updateFieldList(name, newValue);
    }

    public List<LayoutComponent> getLayoutComponents(EmbedEvent embedEvent) {
        return ButtonLayoutGenerator.generateButtons(embedEvent, DELIMITER);
    }

    public void subscribeInteractions(EventDispatcher eventDispatcher, EmbedEvent embedEvent) {
        List<Disposable> subscriptions = new ArrayList<>();

        EmbedFields.getFieldsMap().forEach((fieldKey, value) -> {
            String customId = embedEvent.getEmbedId() + DELIMITER + fieldKey;
            Disposable subscription = eventDispatcher.on(ButtonInteractionEvent.class)
                    .filter(event -> event.getCustomId().equals(customId))
                    .flatMap(interaction -> signupInteractionSubscriber.handleEvent(interaction, embedEvent))
                    .subscribe();
            subscriptions.add(subscription);
        });

        embedEvent.setSubscriptions(subscriptions);
    }

    public void unsubscribeInteractions(EmbedEvent embedEvent) {
        embedEvent.getSubscriptions().forEach(Disposable::dispose);
    }
}
