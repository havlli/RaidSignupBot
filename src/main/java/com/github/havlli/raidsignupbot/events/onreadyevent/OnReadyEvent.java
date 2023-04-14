package com.github.havlli.raidsignupbot.events.onreadyevent;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventService;
import com.github.havlli.raidsignupbot.events.EventHandler;
import com.github.havlli.raidsignupbot.events.createevent.EmbedBuilder;
import com.github.havlli.raidsignupbot.logger.Logger;
import discord4j.common.util.Snowflake;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashSet;

public class OnReadyEvent implements EventHandler {

    private final Logger logger = Dependencies.getInstance().getLogger();
    private final int INTERVAL_SECONDS = 5;

    @Override
    public Class<? extends Event> getEventType() {
        return ReadyEvent.class;
    }

    @Override
    public Mono<?> handleEvent(Event event) {

        subscribeActiveEmbedEventsFromDatabase(event);

        logger.log("Scheduler registered");
        Scheduler scheduler = Schedulers.newSingle("EmbedScheduler");

        return Mono.fromRunnable(() -> this.scheduledTimeCheck(event))
                .delaySubscription(Duration.ofSeconds(INTERVAL_SECONDS))
                .repeat()
                .subscribeOn(scheduler)
                .then();
    }


    private void scheduledTimeCheck(Event event) {

        logger.log("Scheduled check running");

        EmbedEventService embedEventService = Dependencies.getInstance().getEmbedEventService();
        embedEventService.getExpiredEmbedEvents()
                .forEach(embedEvent -> logger.log(embedEvent.getEmbedId()));


        embedEventService.getExpiredEmbedEvents()
                .forEach(embedEvent -> {

                    Snowflake messageId = Snowflake.of(embedEvent.getEmbedId());
                    Snowflake channelId = Snowflake.of(embedEvent.getDestinationChannelId());
                    event.getClient()
                            .getMessageById(channelId, messageId)
                            .flatMap(Message::delete)
                            .subscribe();

                    embedEventService.removeEmbedEvent(embedEvent);
                });
    }

    private void subscribeActiveEmbedEventsFromDatabase(Event event) {
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

        logger.log("EmbedEvent data subscribed!");
    }
}
