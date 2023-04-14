package com.github.havlli.raidsignupbot.events.createevent;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.ArrayList;
import java.util.List;

public class EmbedPreview {

    private final List<EmbedCreateFields.Field> fieldList = new ArrayList<>();

    public EmbedCreateSpec buildPreview(EmbedEvent.EmbedEventBuilder embedEventBuilder) {
        addFieldIfNotPresent("Name", embedEventBuilder.getName(), false);
        addFieldIfNotPresent("Description", embedEventBuilder.getDescription(), false);
        addFieldIfNotPresent("Time", embedEventBuilder.getTime() != null ? embedEventBuilder.getTime().toString() : null, true);
        addFieldIfNotPresent("Date", embedEventBuilder.getDate() != null ? embedEventBuilder.getDate().toString() : null, true);
        addFieldIfNotPresent("Raids", embedEventBuilder.getInstances(), false);
        addFieldIfNotPresent("Raid Size", embedEventBuilder.getMemberSize(), false);
        addFieldIfNotPresent("Destination channel ID", embedEventBuilder.getDestinationChannelId(), false);
        if (embedEventBuilder.isReservingEnabled()) {
            EmbedCreateFields.Field reservingEnabled = EmbedCreateFields.Field.of("SoftReserve Enabled", "", false);
            addFieldIfNotPresent(reservingEnabled);
        }
        return EmbedCreateSpec.builder()
                .addAllFields(fieldList)
                .build();
    }

    private void addFieldIfNotPresent(String name, String value, boolean inline) {
        if (value != null && fieldList.stream().noneMatch(field -> field.name().equals(name))) {
            EmbedCreateFields.Field field = EmbedCreateFields.Field.of(name, value, inline);
            fieldList.add(field);
        }
    }

    private void addFieldIfNotPresent(EmbedCreateFields.Field field) {
        if (!fieldList.contains(field)) {
            fieldList.add(field);
        }
    }
}
