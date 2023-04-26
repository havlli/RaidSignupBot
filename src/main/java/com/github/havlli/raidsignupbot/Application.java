package com.github.havlli.raidsignupbot;

import com.github.havlli.raidsignupbot.client.InitiateClient;
import com.github.havlli.raidsignupbot.commands.CommandRegistrar;
import com.github.havlli.raidsignupbot.commands.Commands;
import com.github.havlli.raidsignupbot.events.EventSubscriber;
import discord4j.core.GatewayDiscordClient;

public class Application {

    public static void main(String[] args) {
        GatewayDiscordClient gatewayClient = InitiateClient.getGateway();

        CommandRegistrar commandRegistrar = new CommandRegistrar(gatewayClient);
        commandRegistrar.registerCommands(Commands.getCommandsList());

        EventSubscriber eventSubscriber = new EventSubscriber();
        eventSubscriber.subscribeEvents(gatewayClient);

        InitiateClient.onDisconnect();
    }
}
