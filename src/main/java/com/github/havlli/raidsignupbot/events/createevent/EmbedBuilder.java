package com.github.havlli.raidsignupbot.events.createevent;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventMapper;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventService;
import com.github.havlli.raidsignupbot.signupuser.SignupUser;
import com.github.havlli.raidsignupbot.signupuser.SignupUserService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmbedBuilder {
    private final List<SignupUser> signupUsers;
    private final EmbedEvent embedEvent;
    private final EmbedEventMapper embedEventMapper;
    private final EmbedEventService embedEventService;
    private final SignupUserService signupUserService;

    public EmbedBuilder(EmbedEvent embedEvent, EmbedEventService embedEventService, SignupUserService signupUserService) {
        this.embedEvent = embedEvent;
        this.embedEventService = embedEventService;
        this.signupUserService = signupUserService;
        this.embedEventMapper = new EmbedEventMapper(embedEvent);
        this.signupUsers = embedEvent.getSignupUsers();
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
        String leaderAndIdOfEvent = "Leader: " + embedEvent.getAuthor() + " - ID: " + embedEvent.getEmbedId();

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

    public void saveToDatabase() {
        embedEventService.addEmbedEvent(embedEvent);
    }

    private final List<EmbedCreateFields.Field> fieldList = new ArrayList<>();

    public EmbedCreateSpec getPreview() {
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

    public List<LayoutComponent> getRoleButtons() {
        List<Button> roleButtons = new ArrayList<>();
        List<Button> defaultButtons = new ArrayList<>();
        EmbedFields.getFieldsMap().forEach((key, value) -> {
            String customId = embedEvent.getEmbedId() + "," + key;
            if (key > 0) roleButtons.add(Button.primary(customId, value));
            else defaultButtons.add(Button.secondary(customId, value));
        });

        return List.of(ActionRow.of(roleButtons),ActionRow.of(defaultButtons));
    }

    public List<EmbedCreateFields.Field> getPopulatedFields() {
        List<EmbedCreateFields.Field> populatedFields = new ArrayList<>();
        EmbedFields.getFieldsMap().forEach((key,value) -> {
            Stream<SignupUser> streamSignupUsers = signupUsers.stream()
                    .filter(user -> user.getFieldIndex() == key);
            long count = streamSignupUsers.count();
            if (count > 0) {
                boolean isOneLineField = key < 0;
                String fieldConcat = value + " (" + count + "):" + (isOneLineField ? " " : "\n") +
                        signupUsers.stream()
                                .filter(user -> user.getFieldIndex() == key)
                                .map(user -> "`" + user.getOrder() + "`" + user.getUsername())
                                .collect(Collectors.joining(isOneLineField ? ", " : "\n"));
                if (isOneLineField) populatedFields.add(EmbedCreateFields.Field.of("", fieldConcat, false));
                else populatedFields.add(EmbedCreateFields.Field.of(fieldConcat, "", true));
            }
        });

        return populatedFields;
    }

    public void subscribeInteractions(EventDispatcher eventDispatcher) {
        EmbedFields.getFieldsMap().forEach((fieldKey, value) -> {
            String customId = embedEvent.getEmbedId() + "," + fieldKey;
            eventDispatcher.on(ButtonInteractionEvent.class)
                    .filter(event -> event.getCustomId().equals(customId))
                    .flatMap(event -> {
                        User user = event.getInteraction().getUser();
                        String userId = user.getId().asString();
                        String embedEventId = embedEvent.getEmbedId().toString();

                        SignupUser signupUser = signupUserService.getSignupUser(userId, signupUsers);
                        if (signupUser == null) {
                            int signupOrder = signupUsers.size() + 1;
                            signupUser = new SignupUser(signupOrder, userId, user.getUsername(), fieldKey);
                            signupUserService.addSignupUser(signupUser, signupUsers, embedEventId);
                        } else {
                            signupUserService.updateSignupUser(signupUser, embedEventId, fieldKey);
                        }

                        return event.deferEdit()
                                .then(event.editReply(InteractionReplyEditSpec.builder()
                                                .addEmbed(this.getFinalEmbed())
                                        .build())
                                );
                    }).subscribe();
        });
    }

    public EmbedEvent getEmbedEvent() {
        return embedEvent;
    }

    public Long getDestinationChannelId() {
        return embedEvent.getDestinationChannelId();
    }
}
