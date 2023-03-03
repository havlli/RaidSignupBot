package com.github.havlli.raidsignupbot;

import com.github.havlli.raidsignupbot.client.InitiateClient;
import com.github.havlli.raidsignupbot.events.EventSubscriber;
import discord4j.core.GatewayDiscordClient;

public class Application {

    public static void main(String[] args) {

        GatewayDiscordClient gatewayClient = InitiateClient.getGateway();

        EventSubscriber.subscribeToEvents(gatewayClient);

        InitiateClient.onDisconnect();
    }
}
