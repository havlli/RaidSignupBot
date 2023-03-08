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
import java.util.Collections;
import java.util.concurrent.TimeoutException;

public class SignupCreation {

    private static String eventName = null;
    private static String eventDescription = null;
    public static void setNameOfEvent(User user, ChatInputInteractionEvent event) {

        Mono<PrivateChannel> privateChannelMono = user.getPrivateChannel();
        GatewayDiscordClient client = event.getClient();


        privateChannelMono
                .flatMap(privateChannel -> privateChannel.createMessage("**Step 1**\nEnter name of the event!"))
                .flatMap(message -> {
                    System.out.println(message.getContent());
                    return waitForName(user, client, message);
                })
                .subscribe();


        /*privateChannelMono
                .flatMap(channel -> {
                    Mono<Message> sentMessageMono = channel.createMessage("**Step 1**\nEnter name of the event!");
                    Mono<Message> userMessageMono = channel.getMessagesAfter(Snowflake.of(Instant.now()))
                            .filter(message -> message.getAuthor()
                                    .map(User::getId)
                                    .orElse(Snowflake.of("1"))
                                    .equals(user.getId()))
                            .next();


                    return Mono.zip(sentMessageMono, userMessageMono);
                })
                .flatMap(tuple -> {
                    Message sentMessage = tuple.getT1();
                    Message userMessage = tuple.getT2();
                    String response = userMessage.getContent();

                    return sentMessage.getChannel().flatMap(messageChannel -> messageChannel.createMessage("You said: " + response));
                }).subscribe();*/
    }

    public static Mono<Message> waitForName(User user, GatewayDiscordClient client, Message previousMessage) {

        return client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(event -> event.getMessage().getChannel()
                        .flatMap(channel -> {
                            Snowflake messageId = event.getMessage().getId();
                            return channel.getMessagesBefore(messageId)
                                    .take(1)
                                    .next();
                        })
                        .filter(message -> message.getId().equals(previousMessage.getId()))
                        .flatMap(message -> message.getChannel()
                                .flatMap(messageChannel -> {
                                    eventName = event.getMessage().getContent();
                                    System.out.println(eventName);
                                    return messageChannel.createMessage("**Step 2**\nEnter description of the event!");
                                })
                        ).flatMap(message -> waitForDescription(user, client, message))).next();
    }

    public static Mono<Message> waitForDescription(User user, GatewayDiscordClient client, Message previousMessage) {

        return client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(event -> event.getMessage().getChannel()
                        .flatMap(channel -> {
                            Snowflake messageId = event.getMessage().getId();
                            return channel.getMessagesBefore(messageId)
                                    .take(1)
                                    .next();
                        })
                        .filter(message -> message.getId().equals(previousMessage.getId()))
                        .flatMap(message -> message.getChannel()
                                .flatMap(messageChannel -> {
                                    eventDescription = event.getMessage().getContent();
                                    System.out.println(eventDescription);
                                    return messageChannel.createMessage("Description set");
                                })
                        ).flatMap(message -> startSignupCreation(user, client))).next();
    }

    public static Mono<Message> startSignupCreation(User user, GatewayDiscordClient client) {


        System.out.printf("Sending private message to user %s.%s%n" , user.getUsername(), user.getDiscriminator());

        return user.getPrivateChannel()
                .flatMap(channel -> channel.createMessage("**Step 3**\nChoose which raids is this signup for:\nRequired 1 selection, maximum 3")
                        .withComponents(RaidSelectMenu.getRaidSelectMenu()))
                .flatMap(message -> {
                    System.out.printf("Select Menu message id is %s%n", message.getId().asString());

                    return waitForSelectMenuInteraction(user, message, client);
                });

        /*client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(clientO -> clientO.getMessage().getAuthor().map(User::getId).orElse(Snowflake.of("1")).equals(user.getId()))
                .doOnNext(messageCreateEvent -> {
                    System.out.println("Received message create event call in: " + messageCreateEvent.getMessage().getContent());
                })
                .filter(messageCreateEvent -> messageCreateEvent.getMessage().getContent().equals("confirm"))
                .next()
                .flatMap(response -> response.getMessage().getChannel())
                .flatMap(channel -> {
                    System.out.println("Sending confirmation message to channel: " + channel.getId().asString());
                    return channel.createMessage("Confirmed");
                }).subscribe();*/
    }

    private static Mono<Message> waitForSelectMenuInteraction(User user, Message message, GatewayDiscordClient client) {
        return client.getEventDispatcher().on(SelectMenuInteractionEvent.class)
                .filter(event -> event.getCustomId().equals("raid-select") && event.getInteraction().getUser().equals(user))
                .next()
                .flatMap(event -> {
                    System.out.printf("User %s.%s - SelectMenuInteraction invoked in private channel %s%n",
                            user.getUsername(), user.getDiscriminator(), event.getInteraction().getChannelId().asString());

                    return sendConfirmationMessage(event, message)
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

    private static Mono<Message> sendConfirmationMessage(SelectMenuInteractionEvent event, Message message) {
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
                        .addComponent(newRow)
                        .addEmbed(embed)
                .build()));

        /*return message.edit(MessageEditSpec.builder()
                .addComponent(newRow)
                .addEmbed(embed)
                .contentOrNull(null)
                .build());*/
        /*return event.reply("You selected these values: " + event.getValues() + " \ntype confirm to confirm choice")
                .then(deleteMessage(message))
                .flatMap(result -> {
                    return event.getInteraction().getChannel().flatMap(channel -> {
                        return channel.createMessage("Your second response goes here!");
                    });
                });*/

    }

    private static Mono<Void> deleteMessage(Message message) {
        return message.delete().onErrorResume(error -> {
            System.out.println("Failed to delete message " + message.getId().asString() + " : " + error.getMessage());
            return Mono.empty();
        });
    }
}
