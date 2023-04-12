package com.github.havlli.raidsignupbot.events.createevent;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.ArrayList;
import java.util.List;

public class EmbedPreview {

    private final EmbedEvent embedEvent;

    public EmbedPreview(EmbedEvent embedEvent) {
        this.embedEvent = embedEvent;
    }

    private final List<EmbedCreateFields.Field> fieldList = new ArrayList<>();

    public EmbedCreateSpec buildPreview() {
        addFieldIfNotPresent("Name", embedEvent.getName(), false);
        addFieldIfNotPresent("Description", embedEvent.getDescription(), false);
        addFieldIfNotPresent("Time", embedEvent.getTime() != null ? embedEvent.getTime().toString() : null, true);
        addFieldIfNotPresent("Date", embedEvent.getDate() != null ? embedEvent.getDate().toString() : null, true);
        addFieldIfNotPresent("Raids", embedEvent.getInstances() != null ? String.join(", ", embedEvent.getInstances()) : null, false);
        addFieldIfNotPresent("Raid Size", embedEvent.getMemberSize(), false);
        addFieldIfNotPresent("Destination channel ID", embedEvent.getDestinationChannelId() != null ? embedEvent.getDestinationChannelId().toString() : null, false);
        if (embedEvent.isReservingEnabled()) {
            EmbedCreateFields.Field reservingEnabled = EmbedCreateFields.Field.of("SoftReserve Enabled", "", false);
            addFieldIfNotPresent(reservingEnabled);
        }
        return EmbedCreateSpec.builder().addAllFields(fieldList).build();
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
