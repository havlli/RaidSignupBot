package com.discord.havlli.raidsignupbot;

import com.discord.havlli.raidsignupbot.discordclient.InitiateClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class Application {

    public static void main(String[] args) {

        GatewayDiscordClient gatewayClient = InitiateClient.getGateway();

        //testing code
        gatewayClient.on(MessageCreateEvent.class, event -> {
           if (event.getMessage().getContent().equals("test")) {
               return event.getMessage().getChannel()
                       .flatMap(channel -> channel.createMessage("Tested!"));
           }

           return Mono.empty();
        }).subscribe();

        InitiateClient.onDisconnect();
    }
}
