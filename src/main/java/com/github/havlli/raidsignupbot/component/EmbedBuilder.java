package com.github.havlli.raidsignupbot.component;

import com.github.havlli.raidsignupbot.events.createevent.SignupUser;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmbedBuilder {

    private final List<EmbedCreateFields.Field> previewFields = new ArrayList<>();
    private final List<SignupUser> signupUsers = new ArrayList<>();
    private final HashMap<Integer, SignupUser> signupUserMap = new HashMap<>();
    private String name;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private List<String> selectedRaids;
    private String raidSize;
    private String destinationChannelId;
    private boolean reserveEnabled;
    private final User author;
    private Long embedId;
    private final HashMap<Integer, String> fieldsMap = new HashMap<>(Map.of(
            -1, "Absence",
            -2, "Late",
            -3, "Tentative",
            1, "Tank",
            2, "Melee",
            3, "Ranged",
            4, "Healer",
            5, "Support"
    ));

    public EmbedBuilder(User author) {
        this.name = null;
        this.description = null;
        this.date = null;
        this.time = null;
        this.selectedRaids = null;
        this.raidSize = null;
        this.destinationChannelId = null;
        this.reserveEnabled = false;
        this.embedId = null;
        this.author = author;
    }

    private Long getTimestamp() {
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        return dateTime.atZone(ZoneOffset.UTC).toInstant().getEpochSecond();
    }

    private String getRaidSizeString() {
        return signupUsers.size() + "/" + raidSize;
    }

    public EmbedCreateSpec getFinalEmbed() {
        String emptyString = "";

        return EmbedCreateSpec.builder()
                .addField(emptyString,"Leader: " + author.getUsername() + " - ID: " + embedId, false)
                .addField(name.toUpperCase(), emptyString,false)
                .addField(emptyString, description, false)
                .addField(emptyString, "<t:" + getTimestamp() + ":D>", true)
                .addField(emptyString, "<t:" + getTimestamp() + ":t>", true)
                .addField(emptyString, getRaidSizeString(), true)
                .addAllFields(getPopulatedFields())
                .build();
    }

    public EmbedCreateSpec getEmbedPreview() {
        return EmbedCreateSpec.builder().addAllFields(previewFields).build();
    }

    public List<LayoutComponent> getRoleButtons() {
        List<Button> roleButtons = new ArrayList<>();
        List<Button> defaultButtons = new ArrayList<>();
        fieldsMap.forEach((key, value) -> {
            String customId = embedId + "," + key;
            if (key > 0) roleButtons.add(Button.primary(customId, value));
            else defaultButtons.add(Button.secondary(customId, value));
        });

        return List.of(ActionRow.of(roleButtons),ActionRow.of(defaultButtons));
    }

    public List<EmbedCreateFields.Field> getPopulatedFields() {
        List<EmbedCreateFields.Field> populatedFields = new ArrayList<>();
        fieldsMap.forEach((key,value) -> {
            Stream<SignupUser> streamSignupUsers = signupUsers.stream()
                    .filter(user -> user.getFieldIndex() == key);
            long count = streamSignupUsers.count();
            if (count > 0) {
                String fieldConcat = value + " (" + count + "):" + "\n" +
                        signupUsers.stream()
                                .filter(user -> user.getFieldIndex() == key)
                                .map(user -> user.getUser().getUsername())
                                .collect(Collectors.joining("\n"));
                populatedFields.add(EmbedCreateFields.Field.of(fieldConcat, "", true));
            }
        });

        return populatedFields;
    }

    public void subscribeInteractions(EventDispatcher eventDispatcher) {

        fieldsMap.forEach((key, value) -> {
            String customId = embedId + "," + key;
            eventDispatcher.on(ButtonInteractionEvent.class)
                    .filter(event -> event.getCustomId().equals(customId))
                    .flatMap(event -> {
                        User user = event.getInteraction().getUser();
                        int signupOrder = signupUsers.size() + 1;
                        boolean alreadySigned = false;
                        for (SignupUser signupUser : signupUsers) {
                            if (signupUser.getUser().getId().equals(user.getId())) {
                                alreadySigned = true;
                                signupUser.setFieldIndex(key);
                            }
                        }
                        if (!alreadySigned) {
                            SignupUser signupUser = new SignupUser(signupOrder, user, key);
                            signupUsers.add(signupUser);
                        }

                        return event.deferEdit()
                                .then(event.editReply(InteractionReplyEditSpec.builder()
                                                .addEmbed(this.getFinalEmbed())
                                        .build())
                                );
                    }).subscribe();
        });
    }

    public void setName(Message message) {
        this.name = message.getContent();
        previewFields.add(EmbedCreateFields.Field.of("Name", name, false));
    }

    public void setDescription(Message message) {
        this.description = message.getContent();
        previewFields.add(EmbedCreateFields.Field.of("Description", description, false));
    }

    public void setDate(Message message) {
        this.date = LocalDate.parse(message.getContent(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        previewFields.add(EmbedCreateFields.Field.of("Date", date.toString(), true));
    }

    public void setTime(Message message) {
        this.time = LocalTime.parse(message.getContent(), DateTimeFormatter.ofPattern("HH:mm"));
        previewFields.add(EmbedCreateFields.Field.of("Time", time.toString(), true));
    }

    public void setSelectedRaids(List<String> selectedRaids) {
        this.selectedRaids = selectedRaids;
        previewFields.add(EmbedCreateFields.Field.of("Raids", String.join(", ", selectedRaids), false));
    }

    public void setRaidSize(List<String> raidSize) {
        String defaultSize = "25";
        this.raidSize = raidSize
                .stream()
                .findFirst()
                .orElse(defaultSize);
        previewFields.add(EmbedCreateFields.Field.of("Maximum size", this.raidSize, false));
    }

    public void setDestinationChannelId(List<String> destinationChannelId, String defaultChannelId) {
        this.destinationChannelId = destinationChannelId
                .stream()
                .findFirst()
                .orElse(defaultChannelId);
        previewFields.add(EmbedCreateFields.Field.of("Channel to post in", this.destinationChannelId, false));
    }

    public void setReserveEnabled(boolean reserveEnabled) {
        this.reserveEnabled = reserveEnabled;
        if (reserveEnabled) previewFields.add(EmbedCreateFields.Field.of("SoftReserve", "Enabled", false));
        else previewFields.add(EmbedCreateFields.Field.of("SoftReserve", "Disabled", false));
    }

    public void setEmbedId(Long messageId) {
        this.embedId = messageId;
    }

    public String getDestinationChannelId() {
        return destinationChannelId;
    }
}
