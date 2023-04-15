package com.github.havlli.raidsignupbot.events.onreadyevent;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.events.createevent.EmbedBuilder;
import com.github.havlli.raidsignupbot.logger.Logger;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;

import java.util.HashSet;

public class StartupTasks implements Tasks {

    private final Logger logger;
    private final Event event;

    public StartupTasks(Event event, Logger logger) {
        this.event = event;
        this.logger = logger;
    }

    @Override
    public void run() {
        subscribeActiveEmbedEventsFromDatabase();
    }

    private void subscribeActiveEmbedEventsFromDatabase() {
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        HashSet<EmbedEvent> activeEmbedEvents = Dependencies.getInstance()
                .getEmbedEventService()
                .getActiveEmbedEvents();

        activeEmbedEvents.forEach(embedEvent -> {
            EmbedBuilder embedBuilder = new EmbedBuilder(
                    Dependencies.getInstance().getEmbedEventService(),
                    Dependencies.getInstance().getSignupUserService()
            );
            embedBuilder.subscribeInteractions(eventDispatcher, embedEvent);
        });

        logger.log("Active EmbedEvent interactions subscribed!");
    }
}
