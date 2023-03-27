package com.github.havlli.raidsignupbot.commands;

import com.github.havlli.raidsignupbot.client.InitiateClient;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandBuilder {
    private final GatewayDiscordClient client;
    private final long applicationId;
    private final long guildId;
    private static final Set<ApplicationCommandRequest> commandRequests = new HashSet<>();

    public CommandBuilder(GatewayDiscordClient client) {
        this.client = client;
        this.applicationId = getApplicationId();
        this.guildId = getGuildId();
    }

    private long getApplicationId() {
        return InitiateClient.getGateway()
                .getRestClient()
                .getApplicationId()
                .block();
    }

    private long getGuildId() {
        List<Long> guildIds = InitiateClient.getGateway()
                .getGuilds()
                .map(Guild::getId)
                .map(Snowflake::asLong)
                .collectList()
                .block();
        assert guildIds != null;
        return guildIds.get(0);
    }

    private Set<ApplicationCommandRequest> populateCommandSet() {
        commandRequests.add(buildCreateEventCommand());
        commandRequests.add(buildDeleteEventCommand());
        return commandRequests;
    }

    public void subscribeCommands() {
        for (ApplicationCommandRequest commandRequest : populateCommandSet()) {
            client.getRestClient().getApplicationService()
                    .createGuildApplicationCommand(applicationId, guildId, commandRequest)
                    .subscribe();
        }
    }

    private ApplicationCommandRequest buildCreateEventCommand() {
        return ApplicationCommandRequest.builder()
                .name("create-event")
                .description("Create sign-up for event")
                .build();
    }

    private ApplicationCommandRequest buildDeleteEventCommand() {
        return ApplicationCommandRequest.builder()
                .name("delete-event")
                .description("Delete event by message ID")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("message-id")
                        .description("Message ID of the event")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                ).build();
    }
}
