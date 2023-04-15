package com.github.havlli.raidsignupbot.events.onreadyevent;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.events.EventHandler;
import com.github.havlli.raidsignupbot.logger.Logger;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import reactor.core.publisher.Mono;

public class OnReadyEvent implements EventHandler {

    private final Logger logger = Dependencies.getInstance().getLogger();

    @Override
    public Class<? extends Event> getEventType() {
        return ReadyEvent.class;
    }

    @Override
    public Mono<?> handleEvent(Event event) {

        StartupTasks startupTasks = new StartupTasks(event, logger);
        startupTasks.run();

        ScheduledTasks scheduledTasks = new ScheduledTasks(event, logger);

        return scheduledTasks.getSchedulerMono();
    }
}
