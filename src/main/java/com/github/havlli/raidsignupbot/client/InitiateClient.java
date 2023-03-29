package com.github.havlli.raidsignupbot.client;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;

public class InitiateClient {

    private static InitiateClient singleton = null;
    private static DiscordClient client = null;
    private static GatewayDiscordClient gateway = null;
    private final String token;


    private InitiateClient() {
        this.token = Config.token;
    }

    private static InitiateClient getInstance() {
        if (singleton == null) singleton = new InitiateClient();

        return singleton;
    }

    private static DiscordClient getClient() {
        if (client == null) client = DiscordClient.create(getInstance().token);

        return client;
    }

    public static GatewayDiscordClient getGateway() {
        if (gateway == null) gateway = getClient()
                                            .login()
                                            .block();

        return gateway;
    }

    public static void onDisconnect() {
        getGateway().onDisconnect().block();
    }
}
