package com.github.havlli.raidsignupbot.embedevent;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmbedEventMapper {
    private final EmbedEvent embedEvent;

    public EmbedEventMapper(EmbedEvent embedEvent) {
        this.embedEvent = embedEvent;
    }

    public void mapAuthorFromUser(User user) {
        embedEvent.setAuthor(String.format("%s#%s", user.getUsername(), user.getDiscriminator()));
    }

    public void mapNameFromMessage(Message message) {
        embedEvent.setName(message.getContent());
    }

    public void mapDescriptionFromMessage(Message message) {
        embedEvent.setDescription(message.getContent());
    }

    public void mapDateFromMessage(Message message) {
        LocalDate date = LocalDate.parse(message.getContent(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        embedEvent.setDate(date);
    }

    public void mapTimeFromMessage(Message message) {
        LocalTime time = LocalTime.parse(message.getContent(), DateTimeFormatter.ofPattern("HH:mm"));
        embedEvent.setTime(time);
    }

    public void mapInstancesFromList(List<String> selectedInstances) {
        embedEvent.setInstances(selectedInstances);
    }

    public void mapMemberSizeFromList(List<String> selectedSize, String defaultSize) {
        String size = selectedSize.stream()
                .findFirst()
                .orElse(defaultSize);
        embedEvent.setMemberSize(size);
    }

    public void mapDestChannelIdFromList(List<String> selectedChannelId, String defaultChannelId) {
        Long channelId = selectedChannelId.stream()
                .mapToLong(Long::parseLong)
                .findFirst()
                .orElse(Long.parseLong(defaultChannelId));
        embedEvent.setDestinationChannelId(channelId);
    }

    public void mapReservingFromBoolean(boolean reserveEnabled) {
        embedEvent.setReservingEnabled(reserveEnabled);
    }

    public void mapEmbedIdFromMessage(Message message) {
        Long id = message.getId().asLong();
        embedEvent.setEmbedId(id);
    }
}
