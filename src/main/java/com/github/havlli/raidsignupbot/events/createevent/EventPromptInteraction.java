package com.github.havlli.raidsignupbot.events.createevent;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.component.ButtonRow;
import com.github.havlli.raidsignupbot.component.ChanelSelectMenu;
import com.github.havlli.raidsignupbot.component.MemberSizeSelectMenu;
import com.github.havlli.raidsignupbot.component.RaidSelectMenu;
import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedGenerator;
import com.github.havlli.raidsignupbot.logger.Logger;
import com.github.havlli.raidsignupbot.prompts.*;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class EventPromptInteraction implements Prompt {

    private final ChatInputInteractionEvent event;
    private final Snowflake guildId;
    private final EmbedGenerator embedGenerator;
    private final GatewayDiscordClient client;
    private final MessageGarbageCollector garbageCollector;
    private final Logger logger;
    private final Mono<MessageChannel> messageChannel;
    private final EmbedEvent.Builder embedEventBuilder;
    private final InteractionFormatter formatter;

    public EventPromptInteraction(ChatInputInteractionEvent event,
                                  EmbedGenerator embedGenerator,
                                  Snowflake guildId) {
        this.event = event;
        this.guildId = guildId;
        this.embedGenerator = embedGenerator;
        this.client = event.getClient();
        this.messageChannel = fetchMessageChannel();
        this.logger = Dependencies.getInstance().getLogger();
        this.garbageCollector = new MessageGarbageCollector(logger);
        this.embedEventBuilder = EmbedEvent.builder();
        this.formatter = new InteractionFormatter();
    }

    @Override
    public Mono<Message> getMono() {
        return chainedPromptWithTimeout();
    }

    public Mono<Message> chainedPromptWithTimeout() {
        return chainedPrompt()
                .timeout(Duration.ofSeconds(120))
                .onErrorResume(TimeoutException.class, error -> messageChannel
                        .flatMap(channel -> channel.createMessage("Interaction timeout! Please try again."))
                        .then(garbageCollector.cleanup(messageChannel)
                                .cast(Message.class)));
    }

    private Mono<Message> chainedPrompt() {
        addUserToEvent();
        return promptName()
                .flatMap(msg -> promptDescription())
                .flatMap(msg -> promptDate())
                .flatMap(msg -> promptTime())
                .flatMap(msg -> promptRaidSelect())
                .flatMap(msg -> promptRaidSize())
                .flatMap(msg -> promptDestinationChannel())
                .flatMap(msg -> promptReserveOption())
                .flatMap(msg -> promptConfirmation());
    }

    private Mono<Message> promptName() {
        return PrivateTextPrompt.builder(event)
                .withPromptMessage("**Step 1**\nEnter your name")
                .withGarbageCollector(garbageCollector)
                .withInputHandler(embedEventBuilder::addName)
                .build()
                .getMono();
    }

    private Mono<Message> promptDescription() {
        return PrivateTextPrompt.builder(event)
                .withPromptMessage("**Step 2**\nEnter description")
                .withGarbageCollector(garbageCollector)
                .withInputHandler(embedEventBuilder::addDescription)
                .build()
                .getMono();
    }

    private Mono<Message> promptDate() {
        return PrivateTextPrompt.builder(event)
                .withPromptMessage("**Step 3**\nEnter the date (format: yyyy-MM-dd)")
                .withGarbageCollector(garbageCollector)
                .withInputHandler(embedEventBuilder::addDate)
                .withOnErrorMessage("Invalid format! Try again.")
                .build()
                .getMono();
    }

    private Mono<Message> promptTime() {
        Instant currentUtcTime = Instant.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("UTC"));
        String time = dateTimeFormatter.format(currentUtcTime);

        return PrivateTextPrompt.builder(event)
                .withPromptMessage("**Step 4**\nEnter the time of the event in UTC timezone " + time + " (format: HH:mm)")
                .withGarbageCollector(garbageCollector)
                .withInputHandler(embedEventBuilder::addTime)
                .withOnErrorMessage("Invalid format! Try again.")
                .build()
                .getMono();
    }

    private Mono<Message> promptRaidSelect() {
        return PrivateSelectPrompt.builder(event)
                .withPromptMessage("**Step 5**\nChoose which raids is this signup for:\nRequired 1 selection, maximum 3")
                .withGarbageCollector(garbageCollector)
                .withSelectMenuComponent(new RaidSelectMenu())
                .withInteractionHandler(event -> {
                    embedEventBuilder.addInstances(event.getValues());
                    String response = "Selected raids: " + formatter.formatResponse(event);

                    return event.deferEdit()
                            .then(event.editReply(InteractionReplyEditSpec.builder()
                                    .contentOrNull(response)
                                    .componentsOrNull(null)
                                    .build()));
                })
                .build()
                .getMono();
    }

    private Mono<Message> promptRaidSize() {
        return PrivateSelectPrompt.builder(event)
                .withPromptMessage("**Step 6**\nChoose maximum size for this raid")
                .withGarbageCollector(garbageCollector)
                .withSelectMenuComponent(new MemberSizeSelectMenu())
                .withInteractionHandler(event -> {
                    embedEventBuilder.addMemberSize(event.getValues(), "25");
                    String response = "Raid size: " + formatter.formatResponse(event);

                    return event.deferEdit()
                            .then(event.editReply(InteractionReplyEditSpec.builder()
                                    .contentOrNull(response)
                                    .componentsOrNull(null)
                                    .build()));
                })
                .build()
                .getMono();
    }

    private Mono<Message> promptDestinationChannel() {
        return PrivateSelectPrompt.builder(event)
                .withPromptMessage("**Step 7**\nChoose in which channel post this raid signup")
                .withGarbageCollector(garbageCollector)
                .withSelectMenuComponent(new ChanelSelectMenu(fetchGuildTextChannels()))
                .withInteractionHandler(event -> {
                    embedEventBuilder.addDestinationChannel(event.getValues(), fetchOriginChannelId());
                    Snowflake channelId = Snowflake.of(event.getValues().get(0));
                    String response = "Destination channel: " + formatter.channelURL(guildId, channelId);

                    return event.deferEdit()
                            .then(event.editReply(InteractionReplyEditSpec.builder()
                                    .contentOrNull(response)
                                    .componentsOrNull(null)
                                    .build()));
                })
                .build()
                .getMono();
    }

    private Mono<Message> promptReserveOption() {
        return PrivateButtonPrompt.builder(event)
                .withPromptMessage("**Step 8**\nEnable/Disable soft-reserve")
                .withGarbageCollector(garbageCollector)
                .withButtonRowComponent(ButtonRow.builder()
                        .addButton("enabled","Enable", ButtonRow.Builder.buttonType.PRIMARY)
                        .addButton("disabled", "Disable", ButtonRow.Builder.buttonType.SECONDARY)
                        .build())
                .withInteractionHandler(event -> {
                    String customId = event.getCustomId();
                    if (customId.equals("enabled")) embedEventBuilder.addReservingEnabled(true);
                    else if (customId.equals("disabled")) embedEventBuilder.addReservingEnabled(false);
                    String response = "Soft-Reserve: " + event.getCustomId();

                    return event.deferEdit()
                            .then(event.editReply(InteractionReplyEditSpec.builder()
                                    .contentOrNull(response)
                                    .componentsOrNull(null)
                                    .build()));
                })
                .build()
                .getMono();
    }

    private Mono<Message> promptConfirmation() {
        return PrivateButtonPrompt.builder(event)
                .withPromptMessage(MessageCreateSpec.builder()
                        .addEmbed(embedGenerator.getPreviewEmbed(embedEventBuilder))
                        .build())
                .withGarbageCollector(garbageCollector)
                .withButtonRowComponent(ButtonRow.builder()
                        .addButton("confirm","Confirm", ButtonRow.Builder.buttonType.PRIMARY)
                        .addButton("cancel", "Cancel", ButtonRow.Builder.buttonType.SECONDARY)
                        .build())
                .withInteractionHandler(event -> {
                    String customId = event.getCustomId();
                    if (customId.equals("confirm")) {
                        return event.deferReply()
                                .then(garbageCollector.cleanup(messageChannel))
                                .then(confirmMono())
                                .then(event.getInteractionResponse().deleteInitialResponse())
                                .then(Mono.empty());

                    } else if (customId.equals("cancel")) {
                        return garbageCollector.cleanup(messageChannel)
                                .then(Mono.empty());
                    }
                    return Mono.empty();
                })
                .build()
                .getMono();
    }

    private Mono<Message> confirmMono() {
        Snowflake destinationChannel = Snowflake.of(embedEventBuilder.getDestinationChannelId());
        return event.getInteraction()
                .getGuild()
                .flatMap(guild -> guild.getChannelById(destinationChannel)
                        .cast(MessageChannel.class)
                        .flatMap(channel -> channel.createMessage("Generating event..."))
                        .flatMap(message -> {
                            Snowflake messageId = message.getId();
                            EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
                            embedEventBuilder.addEmbedId(messageId);
                            EmbedEvent embedEvent = embedEventBuilder.build();
                            embedGenerator.saveEmbedEvent(embedEvent);
                            embedGenerator.subscribeInteractions(eventDispatcher, embedEvent);

                            Mono<Message> finalMessage = messageChannel
                                    .flatMap(channel -> channel.createMessage("Event created in " +
                                            formatter.messageURL(guildId,destinationChannel,messageId)));

                            return message.edit(MessageEditSpec.builder()
                                    .contentOrNull(null)
                                    .addEmbed(embedGenerator.generateEmbed(embedEvent))
                                    .addAllComponents(embedGenerator.getLayoutComponents(embedEvent))
                                    .build())
                                    .then(finalMessage)
                                    .flatMap(ignored -> {
                                        logger.log("EmbedEvent %s created in %s"
                                                .formatted(messageId.asString(), destinationChannel.asString()));
                                        return Mono.just(ignored);
                                    });
                        })
                        /*.flatMap(message -> {
                            EmbedEvent embedEvent = embedEventBuilder.getEmbedEvent();
                            EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
                            embedGenerator.saveEmbedEvent(embedEvent);
                            embedGenerator.subscribeInteractions(eventDispatcher, embedEvent);
                            return Mono.just(message);
                        })*/
                );
    }

    private void addUserToEvent() {
        User user = event.getInteraction().getUser();
        embedEventBuilder.addAuthor(user);
    }

    public Mono<MessageChannel> fetchMessageChannel() {
        return event.getInteraction().getUser().getPrivateChannel().cast(MessageChannel.class);
    }

    private String fetchOriginChannelId() {
        return event.getInteraction().getChannel()
                .map(channel -> channel.getId().asString())
                .block();
    }

    private List<TextChannel> fetchGuildTextChannels() {
        return event.getInteraction()
                .getGuild()
                .map(Guild::getId)
                .flatMapMany(guildId -> client.getGuildChannels(guildId).ofType(TextChannel.class))
                .collectList()
                .block();
    }
}
