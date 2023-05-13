package com.github.havlli.raidsignupbot.events.onreadyevent;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedGenerator;
import com.github.havlli.raidsignupbot.logger.Logger;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;

import java.util.Set;

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
        Set<EmbedEvent> activeEmbedEvents = Dependencies.getInstance()
                .getEmbedEventService()
                .getActiveEmbedEvents();

        activeEmbedEvents.forEach(embedEvent -> {
            EmbedGenerator embedGenerator = new EmbedGenerator(
                    Dependencies.getInstance().getEmbedEventService(),
                    Dependencies.getInstance().getSignupUserService()
            );
            embedGenerator.subscribeInteractions(eventDispatcher, embedEvent);
        });

        logger.log("Active EmbedEvent interactions subscribed!");
    }
}
