package com.github.havlli.raidsignupbot;

import com.github.havlli.raidsignupbot.client.InitiateClient;
import com.github.havlli.raidsignupbot.commands.CommandBuilder;
import com.github.havlli.raidsignupbot.events.EventSubscriber;
import discord4j.core.GatewayDiscordClient;

public class Application {

    public static void main(String[] args) {

        GatewayDiscordClient gatewayClient = InitiateClient.getGateway();

        CommandBuilder commandBuilder = new CommandBuilder(gatewayClient);
        commandBuilder.subscribeCommands();

        EventSubscriber.subscribeEvents(gatewayClient);


        InitiateClient.onDisconnect();
    }
}
