package com.github.havlli.raidsignupbot.events.createevent;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmbedBuilder {

    private final List<EmbedCreateFields.Field> fields = new ArrayList<>();
    private String name;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private List<String> selectedRaids;
    private String raidSize;
    private String destinationChannelId;
    private boolean reserveEnabled;

    public EmbedBuilder() {
        this.name = null;
        this.description = null;
        this.date = null;
        this.time = null;
        this.selectedRaids = null;
        this.raidSize = null;
        this.destinationChannelId = null;
        this.reserveEnabled = false;
    }

    public static EmbedCreateSpec buildEmbed() {
        return EmbedCreateSpec.builder()
                .addField("","Leader: Placeholder - ID:1010101010\nSecond Line",true)
                .addField("", "Second line", false)
                .build();
    }

    public EmbedCreateSpec getEmbedPreview() {
        return EmbedCreateSpec.builder().addAllFields(fields).build();
    }

    public void setName(Message message) {
        this.name = message.getContent();
        fields.add(EmbedCreateFields.Field.of("Name", name, false));
    }

    public void setDescription(Message message) {
        this.description = message.getContent();
        fields.add(EmbedCreateFields.Field.of("Description", description, false));
    }

    public void setDate(Message message) {
        this.date = LocalDate.parse(message.getContent(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        fields.add(EmbedCreateFields.Field.of("Date", date.toString(), true));
    }

    public void setTime(Message message) {
        this.time = LocalTime.parse(message.getContent(), DateTimeFormatter.ofPattern("HH:mm"));
        fields.add(EmbedCreateFields.Field.of("Time", time.toString(), true));
    }

    public void setSelectedRaids(List<String> selectedRaids) {
        this.selectedRaids = selectedRaids;
        fields.add(EmbedCreateFields.Field.of("Raids", String.join(", ", selectedRaids), false));
    }

    public void setRaidSize(List<String> raidSize) {
        String defaultSize = "25";
        this.raidSize = raidSize
                .stream()
                .findFirst()
                .orElse(defaultSize);
        fields.add(EmbedCreateFields.Field.of("Maximum size", this.raidSize, false));
    }

    public void setDestinationChannelId(List<String> destinationChannelId, String defaultChannelId) {
        this.destinationChannelId = destinationChannelId
                .stream()
                .findFirst()
                .orElse(defaultChannelId);
        fields.add(EmbedCreateFields.Field.of("Channel to post in", this.destinationChannelId, false));
    }

    public void setReserveEnabled(boolean reserveEnabled) {
        this.reserveEnabled = reserveEnabled;
        if (reserveEnabled) fields.add(EmbedCreateFields.Field.of("SoftReserve", "Enabled", false));
        else fields.add(EmbedCreateFields.Field.of("SoftReserve", "Disabled", false));
    }
}
