package com.github.havlli.raidsignupbot.events.createevent;

import com.github.havlli.raidsignupbot.component.ActionRows;
import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedGenerator;
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
    private final EmbedGenerator embedGenerator;
    private final EmbedEvent.EmbedEventBuilder embedEventBuilder;
    private final int INTERACTION_TIMEOUT_SECONDS = 300;
    private final List<Long> messagesToClean;


    public SignupBuilder(ChatInputInteractionEvent event, EmbedGenerator embedGenerator) {
        this.event = event;
        this.client = event.getClient();
        this.eventDispatcher = event.getClient().getEventDispatcher();
        this.user = event.getInteraction().getUser();
        this.privateChannelMono = user.getPrivateChannel();
        this.embedEventBuilder = EmbedEvent.builder();
        this.embedGenerator = embedGenerator;
        this.messagesToClean = new ArrayList<>();
        embedEventBuilder.addAuthor(user);
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
                .flatMap(previousMessage -> {
                    messagesToClean.add(previousMessage.getId().asLong());
                    return awaitNameInput();
                })
                .timeout(Duration.ofSeconds(INTERACTION_TIMEOUT_SECONDS))
                .onErrorResume(TimeoutException.class, ignore -> {
                    System.out.println("SelectMenu Timed out - sendNamePrompt()");
                    return privateChannelMono.flatMap(privateChannel -> {
                        cleanupMessages();
                        return privateChannel.createMessage("Interaction timed out, please start over!");
                    });
                });
    }

    private Mono<Message> awaitNameInput() {
        return eventDispatcher.on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().equals(Optional.of(user)))
                .next()
                .flatMap(message -> {
                    embedEventBuilder.addName(message);
                    /*embedBuilder.getMapper().mapNameFromMessage(message);*/
                    return sendDescriptionPrompt();
                });
    }
    private Mono<Message> sendDescriptionPrompt() {
        String messagePrompt = "**Step 2**\nEnter description of the event!";
        return privateChannelMono
                .flatMap(messageChannel -> messageChannel.createMessage(messagePrompt))
                .flatMap(previousMessage -> {
                    messagesToClean.add(previousMessage.getId().asLong());
                    return awaitDescriptionInput();
                });
    }

    private Mono<Message> awaitDescriptionInput() {
        return eventDispatcher.on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().equals(Optional.of(user)))
                .next()
                .flatMap(message -> {
                    embedEventBuilder.addDescription(message);
                    /*embedBuilder.getMapper().mapDescriptionFromMessage(message);*/
                    return sendDatePrompt();
                });
    }

    private Mono<Message> sendDatePrompt() {
        String messagePrompt = "**Step 3**\nEnter the date (format: yyyy-MM-dd)";
        return privateChannelMono
                .flatMap(channel -> channel.createMessage(messagePrompt))
                .flatMap(previousMessage -> {
                    messagesToClean.add(previousMessage.getId().asLong());
                    return awaitDateInput();
                });
    }

    private Mono<Message> awaitDateInput() {
        return eventDispatcher.on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().equals(Optional.of(user)))
                .next()
                .flatMap(message -> {
                    embedEventBuilder.addDate(message);
                    /*embedBuilder.getMapper().mapDateFromMessage(message);*/
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
                .flatMap(previousMessage -> {
                    messagesToClean.add(previousMessage.getId().asLong());
                    return awaitTimeInput();
                });
    }

    private Mono<Message> awaitTimeInput() {
        return eventDispatcher.on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().equals(Optional.of(user)))
                .next()
                .flatMap(message -> {
                    embedEventBuilder.addTime(message);
                    /*embedBuilder.getMapper().mapTimeFromMessage(message);*/
                    return cleanupMessages()
                            .then(raidSelectPrompt());
                })
                .onErrorResume(DateTimeParseException.class, onError -> privateChannelMono
                        .flatMap(channel -> channel.createMessage("Invalid input, try again!"))
                        .then(sendTimePrompt())
                );
    }

    private Mono<Message> raidSelectPrompt() {
        String messagePrompt = "**Step 5**\nChoose which raids is this signup for:\nRequired 1 selection, maximum 3";

        return privateChannelMono
                .flatMap(channel -> channel.createMessage(messagePrompt)
                        .withComponents(ActionRows.getRaidSelectMenu()))
                .flatMap(this::awaitRaidSelectInteraction);
    }

    private Mono<Message> awaitRaidSelectInteraction(Message message) {
        return eventDispatcher.on(SelectMenuInteractionEvent.class)
                .filter(event -> event.getCustomId().equals("raid-select") && event.getInteraction().getUser().equals(user))
                .next()
                .flatMap(event -> {
                    embedEventBuilder.addInstances(event.getValues());
                    /*embedBuilder.getMapper().mapInstancesFromList(event.getValues());*/
                    return sendRaidSizePrompt(event, message);
                })
                .timeout(Duration.ofSeconds(INTERACTION_TIMEOUT_SECONDS))
                .onErrorResume(TimeoutException.class, ignore -> {
                    System.out.println("SelectMenu Timed out - raid-select");
                    return message.edit(MessageEditSpec.builder()
                            .contentOrNull("Timed out, please start over")
                            .components(Collections.emptyList())
                            .embeds(Collections.emptyList())
                            .contentOrNull("Second Line")
                            .build());
                });
    }

    private Mono<Message> sendRaidSizePrompt(SelectMenuInteractionEvent event, Message message) {
        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                                .addEmbed(embedGenerator.getPreviewEmbed(embedEventBuilder))
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
                    embedEventBuilder.addMemberSize(event.getValues(), "25");
                    /*embedBuilder.getMapper().mapMemberSizeFromList(event.getValues(), "25");*/
                    return sendGuildChannelPrompt(event, message);
                });
    }

    private Mono<Message> sendGuildChannelPrompt(SelectMenuInteractionEvent event, Message message) {
        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                        .addEmbed(embedGenerator.getPreviewEmbed(embedEventBuilder))
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
                    embedEventBuilder.addDestinationChannel(event.getValues(), defaultChannelId);
                    /*embedBuilder.getMapper().mapDestChannelIdFromList(event.getValues(), defaultChannelId);*/
                    return sendSoftReservePrompt(event, message);
                });
    }

    private Mono<Message> sendSoftReservePrompt(SelectMenuInteractionEvent event, Message message) {
        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                        .addEmbed(embedGenerator.getPreviewEmbed(embedEventBuilder))
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
                    embedEventBuilder.addReservingEnabled(event.getCustomId().equals("reserveYes"));
                    /*embedBuilder.getMapper().mapReservingFromBoolean(event.getCustomId().equals("reserveYes"));*/
                    return sendConfirmationPrompt(event, message);
                });
    }

    private Mono<Message> sendConfirmationPrompt(ButtonInteractionEvent event, Message message) {
        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                        .addEmbed(embedGenerator.getPreviewEmbed(embedEventBuilder))
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
                        return finalizeProcess(event, message);
                    } else {
                        return finalizeProcess(event, message);
                    }
                });
    }

    private Mono<Message> finalizeProcess(ButtonInteractionEvent event, Message message) {
        Snowflake destinationChannelId = Snowflake.of(embedEventBuilder.getDestinationChannelId());

        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                        .addEmbed(embedGenerator.getPreviewEmbed(embedEventBuilder))
                        .build())
                )
                .flatMap(event1 -> this.event.getInteraction()
                        .getGuild()
                        .flatMap(guild -> guild.getChannelById(destinationChannelId)
                                .cast(MessageChannel.class)
                                .flatMap(channel -> channel.createMessage("Generating event..."))
                                .flatMap(finalMessage -> {

                                    embedEventBuilder.addEmbedId(finalMessage.getId());
                                    EmbedEvent embedEvent = embedEventBuilder.build();
                                    /*embedBuilder.getMapper().mapEmbedIdFromMessage(finalMessage);*/

                                    return finalMessage.edit(MessageEditSpec.builder()
                                            .contentOrNull("")
                                            .addEmbed(embedGenerator.generateEmbed(embedEvent))
                                            .addAllComponents(embedGenerator.getLayoutComponents(embedEvent))
                                            .build());
                                })
                                .flatMap(process -> {
                                    embedGenerator.saveEmbedEvent(embedEventBuilder.getEmbedEvent());
                                    embedGenerator.subscribeInteractions(eventDispatcher, embedEventBuilder.getEmbedEvent());
                                    return Mono.empty();
                                })
                        )
                );
    }

    private Mono<Void> cleanupMessages() {
        for (Long id : messagesToClean) {
            privateChannelMono
                    .flatMap(privateChannel -> privateChannel.getMessageById(Snowflake.of(id)))
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
