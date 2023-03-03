package com.github.havlli.raidsignupbot.client;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

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
        printOnLogin();

        return gateway;
    }

    public static void onDisconnect() {
        getGateway().onDisconnect().block();
    }

    private static void printOnLogin() {
        gateway.on(ReadyEvent.class, event -> Mono.fromRunnable(() -> {
            final User self = event.getSelf();
            System.out.printf("Logged in as %s#%s%n", self.getUsername(), self.getDiscriminator());
        })).subscribe();
    }
}
