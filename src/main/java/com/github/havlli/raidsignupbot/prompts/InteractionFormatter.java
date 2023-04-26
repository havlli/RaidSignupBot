package com.github.havlli.raidsignupbot.prompts;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;

public class InteractionFormatter {

    public String formatResponse(SelectMenuInteractionEvent event) {
        return String.join(", ", event.getValues());
    }

    public String messageURL(Snowflake guildId, Snowflake channelId, Snowflake messageId) {
        return "https://discord.com/channels/" +
                guildId.asString() + "/" +
                channelId.asString() + "/" +
                messageId.asString();
    }

    public String channelURL(Snowflake guildId, Snowflake channelId) {
        return "https://discord.com/channels/" +
                guildId.asString() + "/" +
                channelId.asString();
    }
}
