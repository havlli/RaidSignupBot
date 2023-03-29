package com.github.havlli.raidsignupbot.component;

import com.github.havlli.raidsignupbot.database.Datasource;
import com.github.havlli.raidsignupbot.events.createevent.SignupUser;
import com.github.havlli.raidsignupbot.model.EmbedEvent;
import com.github.havlli.raidsignupbot.model.EmbedEventMapper;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmbedBuilder {
    private final List<SignupUser> signupUsers = new ArrayList<>();
    private final EmbedEvent embedEvent;
    private final EmbedEventMapper embedEventMapper;
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
        this.embedEvent = new EmbedEvent(author);
        this.embedEventMapper = new EmbedEventMapper(embedEvent);
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

    public EmbedCreateSpec getFinalEmbed() {
        String emptyString = "";
        String leaderAndIdOfEvent = "Leader: " + embedEvent.getAuthor().getUsername() + " - ID: " + embedEvent.getEmbedId();
        Datasource.getInstance().insertEmbedEvent(embedEvent);
        return EmbedCreateSpec.builder()
                .addField(emptyString, leaderAndIdOfEvent, false)
                .addField(embedEvent.getName(), emptyString, false)
                .addField(emptyString, embedEvent.getDescription(), false)
                .addField(emptyString, "<t:" + getTimestamp() + ":D>", true)
                .addField(emptyString, "<t:" + getTimestamp() + ":t>", true)
                .addField(emptyString, getRaidSizeString(), true)
                .addAllFields(getPopulatedFields())
                .build();
    }

    private final Map<String, EmbedCreateFields.Field> fieldPreviewMap = new LinkedHashMap<>();
    public EmbedCreateSpec getPreview() {

        if (embedEvent.getName() != null && !fieldPreviewMap.containsKey("name")) {
            EmbedCreateFields.Field name =
                    EmbedCreateFields.Field.of("Name", embedEvent.getName(), false);
            fieldPreviewMap.put("name", name);
        }
        if (embedEvent.getDescription() != null && !fieldPreviewMap.containsKey("desc")) {
            EmbedCreateFields.Field desc =
                    EmbedCreateFields.Field.of("Description", embedEvent.getDescription(), false);
            fieldPreviewMap.put("desc", desc);
        }
        if (embedEvent.getTime() != null && !fieldPreviewMap.containsKey("time")) {
            EmbedCreateFields.Field time =
                    EmbedCreateFields.Field.of("Time", embedEvent.getTime().toString(), true);
            fieldPreviewMap.put("time", time);
        }
        if (embedEvent.getDate() != null && !fieldPreviewMap.containsKey("date")) {
            EmbedCreateFields.Field date =
                    EmbedCreateFields.Field.of("Date", embedEvent.getDate().toString(), true);
            fieldPreviewMap.put("date", date);
        }
        if (embedEvent.getInstances() != null && !fieldPreviewMap.containsKey("instances")) {
            EmbedCreateFields.Field instances =
                    EmbedCreateFields.Field.of("Raids", String.join(", ", embedEvent.getInstances()), false);
            fieldPreviewMap.put("instances", instances);
        }
        if (embedEvent.getMemberSize() != null && !fieldPreviewMap.containsKey("size")) {
            EmbedCreateFields.Field memberSize =
                    EmbedCreateFields.Field.of("Raid Size", embedEvent.getMemberSize(), false);
            fieldPreviewMap.put("size", memberSize);
        }
        if (embedEvent.getDestinationChannelId() != null && !fieldPreviewMap.containsKey("channel")) {
            EmbedCreateFields.Field channelId =
                    EmbedCreateFields.Field.of("Destination channel ID", embedEvent.getDestinationChannelId().toString(), false);
            fieldPreviewMap.put("channel", channelId);
        }
        if (embedEvent.isReservingEnabled() && !fieldPreviewMap.containsKey("reserve")) {
            EmbedCreateFields.Field reservingEnabled =
                    EmbedCreateFields.Field.of("SoftReserve Enabled", "", false);
            fieldPreviewMap.put("reserve", reservingEnabled);
        }

        return EmbedCreateSpec.builder().addAllFields(fieldPreviewMap.values()).build();
    }

    public List<LayoutComponent> getRoleButtons() {
        List<Button> roleButtons = new ArrayList<>();
        List<Button> defaultButtons = new ArrayList<>();
        fieldsMap.forEach((key, value) -> {
            String customId = embedEvent.getEmbedId() + "," + key;
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
                boolean isOneLineField = key < 0;
                String fieldConcat = value + " (" + count + "):" + (isOneLineField ? " " : "\n") +
                        signupUsers.stream()
                                .filter(user -> user.getFieldIndex() == key)
                                .map(user -> "`" + user.getOrder() + "`" + user.getUser().getUsername())
                                .collect(Collectors.joining(isOneLineField ? ", " : "\n"));
                if (isOneLineField) populatedFields.add(EmbedCreateFields.Field.of("", fieldConcat, false));
                else populatedFields.add(EmbedCreateFields.Field.of(fieldConcat, "", true));
            }
        });

        return populatedFields;
    }

    public void subscribeInteractions(EventDispatcher eventDispatcher) {

        fieldsMap.forEach((key, value) -> {
            String customId = embedEvent.getEmbedId() + "," + key;
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

    public Long getDestinationChannelId() {
        return embedEvent.getDestinationChannelId();
    }
}
