package com.github.havlli.raidsignupbot.events.createevent;

import com.github.havlli.raidsignupbot.component.ActionRows;
import com.github.havlli.raidsignupbot.component.EmbedBuilder;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public class SignupBuilder {

    private final User user;
    private final ChatInputInteractionEvent event;
    private final Mono<PrivateChannel> privateChannelMono;
    private final GatewayDiscordClient client;
    private final EventDispatcher eventDispatcher;
    private List<TextChannel> textChannels;
    private String defaultChannelId;
    private final EmbedBuilder embedBuilder;
    private final int interactionTimeoutSeconds;
    private final List<Long> messagesToClean;


    public SignupBuilder(ChatInputInteractionEvent event) {
        this.event = event;
        this.client = event.getClient();
        this.eventDispatcher = event.getClient().getEventDispatcher();
        this.user = event.getInteraction().getUser();
        this.privateChannelMono = user.getPrivateChannel();
        this.embedBuilder = new EmbedBuilder(this.user);
        this.messagesToClean = new ArrayList<>();
        this.interactionTimeoutSeconds = 30;
    }

    public void startBuildProcess(){
        fetchDefaultChannel();
        fetchTextChannels();
        sendNamePrompt().subscribe();

    }

    private void fetchTextChannels() {
        textChannels = event.getInteraction()
                .getGuild()
                .map(Guild::getId)
                .flatMapMany(guildId -> client.getGuildChannels(guildId).ofType(TextChannel.class))
                .collectList()
                .block();
    }

    private void fetchDefaultChannel() {
        defaultChannelId = event.getInteraction().getChannel()
                .map(channel -> channel.getId().asString())
                .block();
    }

    private Mono<Message> sendNamePrompt() {
        String messagePrompt = "**Step 1**\nEnter name of the event!";

        return privateChannelMono
                .flatMap(privateChannel -> privateChannel.createMessage(messagePrompt))
                .flatMap(this::awaitNameInput);
    }

    private Mono<Message> awaitNameInput(Message previousMessage) {
        messagesToClean.add(previousMessage.getId().asLong());

        return eventDispatcher.on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().equals(Optional.of(user)))
                .next()
                .flatMap(message -> {
                    embedBuilder.getMapper().mapNameToEmbedEvent(message);
                    return sendDescriptionPrompt();
                });
    }
    private Mono<Message> sendDescriptionPrompt() {
        String messagePrompt = "**Step 2**\nEnter description of the event!";
        return privateChannelMono
                .flatMap(messageChannel -> messageChannel.createMessage(messagePrompt))
                .flatMap(this::awaitDescriptionInput);
    }

    private Mono<Message> awaitDescriptionInput(Message previousMessage) {
        messagesToClean.add(previousMessage.getId().asLong());

        return eventDispatcher.on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().equals(Optional.of(user)))
                .next()
                .flatMap(message -> {
                    embedBuilder.getMapper().mapDescriptionToEmbedEvent(message);
                    return sendDatePrompt();
                });
    }

    private Mono<Message> sendDatePrompt() {
        String messagePrompt = "**Step 3**\nEnter the date (format: yyyy-MM-dd)";
        return privateChannelMono
                .flatMap(channel -> channel.createMessage(messagePrompt))
                .flatMap(this::awaitDateInput);
    }

    private Mono<Message> awaitDateInput(Message previousMessage) {
        messagesToClean.add(previousMessage.getId().asLong());

        return eventDispatcher.on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().equals(Optional.of(user)))
                .next()
                .flatMap(message -> {
                    embedBuilder.getMapper().mapDateToEmbedEvent(message);
                    return sendTimePrompt();
                })
                .onErrorResume(DateTimeParseException.class, onError -> privateChannelMono
                        .flatMap(channel -> channel.createMessage("Invalid input, try again!"))
                        .then(sendDatePrompt())
                );
    }

    private Mono<Message> sendTimePrompt() {
        Instant currentUtcTime = Instant.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("UTC"));
        String time = dtf.format(currentUtcTime);
        String messagePrompt = "**Step 4**\nEnter the time of the event in UTC timezone " + time + " (format: HH:mm)";
        return privateChannelMono
                .flatMap(channel -> channel.createMessage(messagePrompt))
                .flatMap(this::awaitTimeInput);
    }

    private Mono<Message> awaitTimeInput(Message previousMessage) {
        messagesToClean.add(previousMessage.getId().asLong());

        return eventDispatcher.on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().equals(Optional.of(user)))
                .next()
                .flatMap(message -> {
                    embedBuilder.getMapper().mapTimeToEmbedEvent(message);
                    return cleanupMessages(message)
                            .then(raidSelectPrompt());
                })
                .onErrorResume(DateTimeParseException.class, onError -> privateChannelMono
                        .flatMap(channel -> channel.createMessage("Invalid input, try again!"))
                        .then(sendTimePrompt())
                );
    }

    private Mono<Message> raidSelectPrompt() {

        System.out.printf("Sending private message to user %s.%s%n" , user.getUsername(), user.getDiscriminator());
        String messagePrompt = "**Step 5**\nChoose which raids is this signup for:\nRequired 1 selection, maximum 3";

        return privateChannelMono
                .flatMap(channel -> channel.createMessage(messagePrompt)
                        .withComponents(ActionRows.getRaidSelectMenu()))
                .flatMap(message -> {
                    System.out.printf("Select Menu message id is %s%n", message.getId().asString());

                    return awaitRaidSelectInteraction(message);
                });
    }

    private Mono<Message> awaitRaidSelectInteraction(Message message) {
        return eventDispatcher.on(SelectMenuInteractionEvent.class)
                .filter(event -> event.getCustomId().equals("raid-select") && event.getInteraction().getUser().equals(user))
                .next()
                .flatMap(event -> {
                    System.out.printf("User %s.%s - SelectMenuInteraction invoked in private channel %s%n - raid-select",
                            user.getUsername(), user.getDiscriminator(), event.getInteraction().getChannelId().asString());

                    return sendRaidSizePrompt(event, message);
                })
                .timeout(Duration.ofSeconds(interactionTimeoutSeconds))
                .onErrorResume(TimeoutException.class, ignore -> {
                    System.out.println("SelectMenu Timed out - raid-select");
                    return message.edit(MessageEditSpec
                            .builder()
                            .contentOrNull("Timed out, please start over")
                            .components(Collections.emptyList())
                            .embeds(Collections.emptyList())
                            .contentOrNull("Second Line")
                            .build());
                });
    }

    private Mono<Message> sendRaidSizePrompt(SelectMenuInteractionEvent event, Message message) {
        String messageId = message.getId().asString(); // Store ID of message before deleting it
        System.out.println(messageId);

        embedBuilder.getMapper().mapInstancesToEmbedEvent(event.getValues());

        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                                .addEmbed(embedBuilder.getPreview())
                                .addComponent(ActionRows.getRaidSizeSelect())
                                .contentOrNull("Test")
                        .build())
                )
                .flatMap(event1 -> awaitRaidSizeInteraction(message));
    }

    private Mono<Message> awaitRaidSizeInteraction(Message message) {
        return eventDispatcher.on(SelectMenuInteractionEvent.class)
                .filter(event -> event.getCustomId().equals("raid-size") && event.getInteraction().getUser().equals(user))
                .next()
                .flatMap(event -> {
                    System.out.printf("User %s.%s - SelectMenuInteraction invoked in private channel %s%n - raid-size",
                            user.getUsername(), user.getDiscriminator(), event.getInteraction().getChannelId().asString());

                    return sendGuildChannelPrompt(event, message);
                })
                .timeout(Duration.ofSeconds(interactionTimeoutSeconds))
                .onErrorResume(TimeoutException.class, ignore -> {
                    System.out.println("SelectMenu Timed out - raid-size");
                    return message.edit(MessageEditSpec
                            .builder()
                            .contentOrNull("Timed out, please start over")
                            .components(Collections.emptyList())
                            .embeds(Collections.emptyList())
                            .contentOrNull("Another content")
                            .build());
                });
    }

    private Mono<Message> sendGuildChannelPrompt(SelectMenuInteractionEvent event, Message message) {
        String messageId = message.getId().asString(); // Store ID of message before deleting it
        System.out.println(messageId);

        embedBuilder.getMapper().mapMemberSizeToEmbedEvent(event.getValues(), "25");

        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                        .addEmbed(embedBuilder.getPreview())
                        .addComponent(ActionRows.getTextChannelSelect(textChannels))
                        .contentOrNull("Test")
                        .build())
                )
                .flatMap(event1 -> awaitGuildChannelInteraction(message));
    }

    private Mono<Message> awaitGuildChannelInteraction(Message message) {
        return eventDispatcher.on(SelectMenuInteractionEvent.class)
                .filter(event -> event.getCustomId().equals("destination-channel") && event.getInteraction().getUser().equals(user))
                .next()
                .flatMap(event -> {
                    System.out.printf("User %s.%s - SelectMenuInteraction invoked in private channel %s%n - raid-size",
                            user.getUsername(), user.getDiscriminator(), event.getInteraction().getChannelId().asString());

                    embedBuilder.getMapper().mapDestChannelIdToEmbedEvent(event.getValues(), defaultChannelId);

                    return sendSoftReservePrompt(event, message);
                })
                .timeout(Duration.ofSeconds(interactionTimeoutSeconds))
                .onErrorResume(TimeoutException.class, ignore -> {
                    System.out.println("SelectMenu Timed out - destination-channel");
                    return message.edit(MessageEditSpec
                            .builder()
                            .contentOrNull("Timed out, please start over")
                            .components(Collections.emptyList())
                            .embeds(Collections.emptyList())
                            .contentOrNull("Another content")
                            .build());
                });
    }

    private Mono<Message> sendSoftReservePrompt(SelectMenuInteractionEvent event, Message message) {
        String messageId = message.getId().asString(); // Store ID of message before deleting it
        System.out.println(messageId);

        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                        .addEmbed(embedBuilder.getPreview())
                        .addComponent(ActionRows.getReserveRow())
                        .contentOrNull("Test")
                        .build())
                )
                .flatMap(event1 -> awaitSoftReserveInteraction(message));
    }

    private Mono<Message> awaitSoftReserveInteraction(Message message) {
        return eventDispatcher.on(ButtonInteractionEvent.class)
                .filter(event -> event.getInteraction().getUser().equals(user))
                .filter(event -> event.getCustomId().equals("reserveYes") || event.getCustomId().equals("reserveNo"))
                .next()
                .flatMap(event -> {
                    if (event.getCustomId().equals("reserveYes")) {
                        System.out.printf("User %s.%s - ButtonInteractionEvent invoked in private channel %s%n - reserveYes",
                                user.getUsername(), user.getDiscriminator(), event.getInteraction().getChannelId().asString());
                        embedBuilder.getMapper().mapReservingToEmbedEvent(true);
                    } else {
                        System.out.printf("User %s.%s - ButtonInteractionEvent invoked in private channel %s%n - reserveNo",
                                user.getUsername(), user.getDiscriminator(), event.getInteraction().getChannelId().asString());
                        embedBuilder.getMapper().mapReservingToEmbedEvent(false);
                    }

                    return sendConfirmationPrompt(event, message);
                })
                .timeout(Duration.ofSeconds(interactionTimeoutSeconds))
                .onErrorResume(TimeoutException.class, ignore -> {
                    System.out.println("SelectMenu Timed out - destination-channel");
                    return message.edit(MessageEditSpec
                            .builder()
                            .contentOrNull("Timed out, please start over")
                            .components(Collections.emptyList())
                            .embeds(Collections.emptyList())
                            .contentOrNull("Another content")
                            .build());
                });
    }

    private Mono<Message> sendConfirmationPrompt(ButtonInteractionEvent event, Message message) {
        String messageId = message.getId().asString(); // Store ID of message before deleting it
        System.out.println(messageId);

        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                        .addEmbed(embedBuilder.getPreview())
                        .addComponent(ActionRows.getConfirmationRow())
                        .contentOrNull("Test")
                        .build())
                )
                .flatMap(event1 -> awaitConfirmationInteraction(message));
    }

    private Mono<Message> awaitConfirmationInteraction(Message message) {
        return eventDispatcher.on(ButtonInteractionEvent.class)
                .filter(event -> event.getInteraction().getUser().equals(user))
                .filter(event -> event.getCustomId().equals("cancel") || event.getCustomId().equals("confirm"))
                .next()
                .flatMap(event -> {
                    if (event.getCustomId().equals("cancel")) {
                        System.out.printf("User %s.%s - ButtonInteractionEvent invoked in private channel %s%n - cancel",
                                user.getUsername(), user.getDiscriminator(), event.getInteraction().getChannelId().asString());
                        return finalizeProcess(event, message);
                    } else {
                        System.out.printf("User %s.%s - ButtonInteractionEvent invoked in private channel %s%n - confirm",
                                user.getUsername(), user.getDiscriminator(), event.getInteraction().getChannelId().asString());
                        return finalizeProcess(event, message);
                    }
                })
                .timeout(Duration.ofSeconds(interactionTimeoutSeconds))
                .onErrorResume(TimeoutException.class, ignore -> {
                    System.out.println("SelectMenu Timed out - destination-channel");
                    return message.edit(MessageEditSpec
                            .builder()
                            .contentOrNull("Timed out, please start over")
                            .components(Collections.emptyList())
                            .embeds(Collections.emptyList())
                            .contentOrNull("Another content")
                            .build());
                });
    }

    private Mono<Message> finalizeProcess(ButtonInteractionEvent event, Message message) {
        String messageId = message.getId().asString(); // Store ID of message before deleting it
        System.out.println(messageId);

        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                        .addEmbed(embedBuilder.getFinalEmbed())
                        .build())
                )
                .flatMap(event1 -> this.event.getInteraction()
                        .getGuild()
                        .flatMap(guild -> guild.getChannelById(Snowflake.of(embedBuilder.getDestinationChannelId()))
                                .cast(MessageChannel.class)
                                .flatMap(channel -> channel.createMessage("Generating event..."))
                                .flatMap(finalMessage -> {
                                    embedBuilder.getMapper().mapEmbedIdToEmbedEvent(finalMessage);

                                    return finalMessage.edit(MessageEditSpec.builder()
                                            .contentOrNull("")
                                            .addEmbed(embedBuilder.getFinalEmbed())
                                            .addAllComponents(embedBuilder.getRoleButtons())
                                            .build());
                                })
                                .flatMap(process -> {
                                    embedBuilder.subscribeInteractions(eventDispatcher);
                                    return Mono.empty();
                                })
                        )
                );
    }

    private Mono<Void> cleanupMessages(Message message) {
        for (Long id : messagesToClean) {
            message.getChannel()
                    .flatMap(channel -> channel.getMessageById(Snowflake.of(id)))
                    .flatMap(SignupBuilder::deleteMessage)
                    .subscribe();
        }
        return Mono.empty();
    }

    private static Mono<Void> deleteMessage(Message message) {
        return message.delete().onErrorResume(error -> {
            System.out.println("Failed to delete message " + message.getId().asString() + " : " + error.getMessage());
            return Mono.empty();
        });
    }
}
