package com.github.havlli.raidsignupbot.embedgenerator;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.ArrayList;
import java.util.List;

public class EmbedPreview {

    private final List<EmbedCreateFields.Field> fieldList = new ArrayList<>();

    public EmbedCreateSpec buildPreview(EmbedEvent.Builder embedEventBuilder) {
        addFieldIfNotPresent(Field.NAME.getName(), embedEventBuilder.getName(), false);
        addFieldIfNotPresent(Field.DESCRIPTION.getName(), embedEventBuilder.getDescription(), false);
        addFieldIfNotPresent(Field.TIME.getName(), embedEventBuilder.getTime() != null ? embedEventBuilder.getTime().toString() : null, true);
        addFieldIfNotPresent(Field.DATE.getName(), embedEventBuilder.getDate() != null ? embedEventBuilder.getDate().toString() : null, true);
        addFieldIfNotPresent(Field.INSTANCES.getName(), embedEventBuilder.getInstances(), false);
        addFieldIfNotPresent(Field.MEMBER_SIZE.getName(), embedEventBuilder.getMemberSize(), false);
        addFieldIfNotPresent(Field.DEST_CHANNEL.getName(), embedEventBuilder.getDestinationChannelId(), false);
        addFieldIfNotPresent(Field.RESERVE.getName(), embedEventBuilder.isReservingEnabled() ? "Enabled" : "Disabled", false);
        return EmbedCreateSpec.builder()
                .addAllFields(fieldList)
                .build();
    }

    public void updateFieldList(String name, String newValue) {
        fieldList.stream()
                .filter(field -> field.name().equals(name))
                .findFirst()
                .ifPresent(field -> {
                    EmbedCreateFields.Field updatedField = EmbedCreateFields.Field.of(field.name(), newValue, field.inline());
                    fieldList.set(fieldList.indexOf(field), updatedField);
                });
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

    public enum Field {
        NAME("Name"),
        DESCRIPTION("Description"),
        TIME("Time"),
        DATE("Date"),
        INSTANCES("Raids"),
        MEMBER_SIZE("Raid Size"),
        DEST_CHANNEL("Channel ID"),
        RESERVE("Soft-Reserve");

        private final String fieldName;
        Field(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getName() {
            return fieldName;
        }
    }
}
