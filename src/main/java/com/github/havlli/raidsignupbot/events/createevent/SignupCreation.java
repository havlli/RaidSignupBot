package com.github.havlli.raidsignupbot.events.createevent;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class SignupCreation {

    private static User user = null;
    private static String eventName = null;
    private static String eventDescription = null;
    private static LocalDate eventDate = null;
    private static LocalTime eventTime = null;
    private static List<Long> messagesToClean = new ArrayList<>();

    public static void setUser(User passedUser) {
        user = passedUser;
    }

    public static void sendNamePrompt(User user, ChatInputInteractionEvent event) {

        setUser(user);

        Mono<PrivateChannel> privateChannelMono = user.getPrivateChannel();
        GatewayDiscordClient client = event.getClient();

        String messagePrompt = "**Step 1**\nEnter name of the event!";

        privateChannelMono
                .flatMap(privateChannel -> privateChannel.createMessage(messagePrompt))
                .flatMap(message -> {
                    System.out.println(message.getContent());
                    return awaitNameInput(client, message);
                })
                .subscribe();
    }

    public static Mono<Message> awaitNameInput(GatewayDiscordClient client, Message previousMessage) {
        messagesToClean.add(previousMessage.getId().asLong());

        return client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(event -> getPreviousMessageMono(event)
                        .filter(message -> message.getId().equals(previousMessage.getId()))
                        .map(message -> saveField("eventName", message, event))
                        .flatMap(message -> sendDescriptionPrompt(message, event))
                        .flatMap(message -> awaitDescriptionInput(client, message)))
                .next();
    }

    public static Mono<Message> sendDescriptionPrompt(Message passMessage, MessageCreateEvent passEvent) {
        String messagePrompt = "**Step 2**\nEnter description of the event!";
        return passMessage.getChannel()
                .flatMap(messageChannel -> messageChannel.createMessage(messagePrompt));
    }

    public static Mono<Message> awaitDescriptionInput(GatewayDiscordClient client, Message previousMessage) {
        messagesToClean.add(previousMessage.getId().asLong());

        return client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(event -> getPreviousMessageMono(event)
                        .filter(message -> message.getId().equals(previousMessage.getId()))
                        .map(message -> saveField("eventDescription", message, event))
                        .flatMap(message -> sendDatePrompt(event))
                        /*.flatMap(message -> awaitDateInput(user, client, message))*/)
                .next();
    }

    public static Mono<Message> sendDatePrompt(MessageCreateEvent passEvent) {
        String messagePrompt = "**Step 3**\nEnter the date (format: yyyy-MM-dd)";
        return user.getPrivateChannel()
                .flatMap(privateChannel -> privateChannel.createMessage(messagePrompt))
                .flatMap(message -> awaitDateInput(passEvent, message));
        /*return passEvent.getMessage().getChannel()
                .flatMap(messageChannel -> messageChannel.createMessage(messagePrompt))
                .flatMap(message -> awaitDateInput(passEvent, message));*/
    }

    public static Mono<Message> awaitDateInput(MessageCreateEvent passEvent, Message previousMessage) {
        messagesToClean.add(previousMessage.getId().asLong());
        GatewayDiscordClient client = passEvent.getClient();

        return client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(event -> getPreviousMessageMono(event)
                        .filter(message -> message.getId().equals(previousMessage.getId()))
                        .map(message -> saveField("eventDate", message, event))

                        .onErrorResume(e -> {
                            System.out.println("Catched error");
                            if (e instanceof DateTimeParseException) {
                                return event.getMessage().getChannel().flatMap(channel -> channel.createMessage("Invalid Input!"))
                                        .then(sendDatePrompt(event));
                            }
                            return Mono.error(e);
                        })
                        .flatMap(message -> raidSelectPrompt(client)))
                .next();
    }

    public static Mono<Message> raidSelectPrompt(GatewayDiscordClient client) {

        System.out.printf("Sending private message to user %s.%s%n" , user.getUsername(), user.getDiscriminator());

        String messagePrompt = "**Step 3**\nChoose which raids is this signup for:\nRequired 1 selection, maximum 3";

        return user.getPrivateChannel()
                .flatMap(channel -> channel.createMessage(messagePrompt)
                        .withComponents(RaidSelectMenu.getRaidSelectMenu()))
                .flatMap(message -> {
                    System.out.printf("Select Menu message id is %s%n", message.getId().asString());

                    return awaitRaidSelectInteraction(user, message, client);
                });
    }

    private static Mono<Message> awaitRaidSelectInteraction(User user, Message message, GatewayDiscordClient client) {
        return client.getEventDispatcher().on(SelectMenuInteractionEvent.class)
                .filter(event -> event.getCustomId().equals("raid-select") && event.getInteraction().getUser().equals(user))
                .next()
                .flatMap(event -> {
                    System.out.printf("User %s.%s - SelectMenuInteraction invoked in private channel %s%n",
                            user.getUsername(), user.getDiscriminator(), event.getInteraction().getChannelId().asString());

                    return sendRaidSizePrompt(event, message)
                            .doOnNext(message1 -> System.out.println("Do on Next invoked - " + message1.getId().asLong()));
                })
                .timeout(Duration.ofSeconds(20))
                .onErrorResume(TimeoutException.class, ignore -> {
                    System.out.println("SelectMenu Timed out");
                    return message.edit(MessageEditSpec
                            .builder()
                            .contentOrNull("Timed out, please start over")
                            .components(Collections.emptyList())
                            .build());
                });
    }

    private static Mono<Message> sendRaidSizePrompt(SelectMenuInteractionEvent event, Message message) {
        String messageId = message.getId().asString(); // Store ID of message before deleting it
        System.out.println(messageId);

        Button newButton = Button.primary("new-button", "New Button");
        ActionRow newRow = ActionRow.of(newButton);

        String valueConcat = String.join(", ", event.getValues());


        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                        .color(Color.DEEP_LILAC)
                        .title(eventName)
                        .author("Author", null, null)
                        .description(eventDescription)
                        .addField("Raids",valueConcat,false)
                        .build();

        /*event.acknowledge().block();*/

        return event.deferReply()
                .then(deleteMessage(message))
                .then(event.createFollowup(InteractionFollowupCreateSpec.builder()
                        .content("Choose maximum raid size!")
                        .addComponent(newRow)
                        .addComponent(RaidSelectMenu.getRaidSizeSelect())
                        .addEmbed(embed)
                .build()));
    }

    public static Mono<Message> getPreviousMessageMono(MessageCreateEvent event) {
        return event.getMessage().getChannel()
                .flatMap(messageChannel -> {
                    Snowflake messageSnowflake = event.getMessage().getId();
                    return messageChannel.getMessagesBefore(messageSnowflake)
                            .take(1)
                            .next();
                });
    }

    private static Mono<Void> deleteMessage(Message message) {
        return message.delete().onErrorResume(error -> {
            System.out.println("Failed to delete message " + message.getId().asString() + " : " + error.getMessage());
            return Mono.empty();
        });
    }

    public static Message saveField(String fieldName, Message passMessage, MessageCreateEvent passEvent) {
        switch (fieldName) {
            case "eventName" -> {
                eventName = passEvent.getMessage().getContent();
                System.out.println(eventName);
            }
            case "eventDescription" -> {
                eventDescription = passEvent.getMessage().getContent();
                System.out.println(eventDescription);
            }
            case "eventDate" -> {
                String dateString = passEvent.getMessage().getContent();
                eventDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                System.out.println(eventDate);
            }
        }
        return passMessage;
    }
}
