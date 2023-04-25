package com.github.havlli.raidsignupbot.promptkit;

import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;

public class InteractionFormatter {

    public String formatResponse(SelectMenuInteractionEvent event) {
        return String.join(", ", event.getValues());
    }
}
