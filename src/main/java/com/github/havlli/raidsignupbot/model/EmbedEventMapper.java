package com.github.havlli.raidsignupbot.model;

import discord4j.core.object.entity.Message;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmbedEventMapper {
    private final EmbedEvent embedEvent;

    public EmbedEventMapper(EmbedEvent embedEvent) {
        this.embedEvent = embedEvent;
    }

    public void mapNameToEmbedEvent(Message message) {
        embedEvent.setName(message.getContent());
    }

    public void mapDescriptionToEmbedEvent(Message message) {
        embedEvent.setDescription(message.getContent());
    }

    public void mapDateToEmbedEvent(Message message) {
        LocalDate date = LocalDate.parse(message.getContent(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        embedEvent.setDate(date);
    }

    public void mapTimeToEmbedEvent(Message message) {
        LocalTime time = LocalTime.parse(message.getContent(), DateTimeFormatter.ofPattern("HH:mm"));
        embedEvent.setTime(time);
    }

    public void mapInstancesToEmbedEvent(List<String> selectedInstances) {
        embedEvent.setInstances(selectedInstances);
    }

    public void mapMemberSizeToEmbedEvent(List<String> selectedSize, String defaultSize) {
        String size = selectedSize.stream()
                .findFirst()
                .orElse(defaultSize);
        embedEvent.setMemberSize(size);
    }

    public void mapDestChannelIdToEmbedEvent(List<String> selectedChannelId, String defaultChannelId) {
        Long channelId = selectedChannelId.stream()
                .mapToLong(Long::parseLong)
                .findFirst()
                .orElse(Long.parseLong(defaultChannelId));
        embedEvent.setDestinationChannelId(channelId);
    }

    public void mapReservingToEmbedEvent(boolean reserveEnabled) {
        embedEvent.setReservingEnabled(reserveEnabled);
    }

    public void mapEmbedIdToEmbedEvent(Message message) {
        Long id = message.getId().asLong();
        embedEvent.setEmbedId(id);
    }
}
