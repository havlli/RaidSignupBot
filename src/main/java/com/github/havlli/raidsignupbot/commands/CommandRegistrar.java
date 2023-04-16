package com.github.havlli.raidsignupbot.commands;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.client.InitiateClient;
import com.github.havlli.raidsignupbot.logger.Logger;
import discord4j.common.JacksonResources;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistrar {
    private final Logger LOGGER = Dependencies.getInstance().getLogger();
    private final RestClient restClient;
    private static final String commandsFolderName = "commands/";

    public CommandRegistrar(GatewayDiscordClient client) {
        this.restClient = client.getRestClient();
    }

    public void registerCommands(List<String> fileNames) {
        final JacksonResources d4jMapper = JacksonResources.create();

        final ApplicationService applicationService = restClient.getApplicationService();
        final long applicationId = restClient.getApplicationId().block();
        final long guildId = getGuildId();

        List<ApplicationCommandRequest> commands = new ArrayList<>();

        try {
            for (String json : getCommandsJson(fileNames)) {
                ApplicationCommandRequest request = d4jMapper.getObjectMapper()
                        .readValue(json, ApplicationCommandRequest.class);

                commands.add(request);
            }
        } catch (IOException e) {
            LOGGER.log("Error reading command files: " + e.getMessage());
        }

        applicationService.bulkOverwriteGuildApplicationCommand(applicationId, guildId, commands)
                .doOnNext(command -> LOGGER.log("Successfully registered Guild Command " + command.name()))
                .doOnError(error -> LOGGER.log("Failed to register guild command: " + error.getMessage()))
                .subscribe();
    }

    public static List<String> getCommandsJson(List<String> fileNames) throws IOException {
        URL url = CommandRegistrar.class.getClassLoader()
                .getResources(commandsFolderName)
                .nextElement();
        Objects.requireNonNull(Objects.requireNonNull(url, commandsFolderName + " could not be found"));

        List<String> list = new ArrayList<>();
        for (String file : fileNames) {
            String resourceFileAsString = getResourceFileAsString(commandsFolderName + file);
            list.add(Objects.requireNonNull(resourceFileAsString, "Command file not found: " + file));
        }
        return list;
    }

    private static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream resourceAsStream = classLoader.getResourceAsStream(fileName)) {
            if (resourceAsStream == null) return null;
            try (InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream);
                 BufferedReader reader = new BufferedReader(inputStreamReader)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
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
}
