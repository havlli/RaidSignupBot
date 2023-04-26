package com.github.havlli.raidsignupbot.events.onreadyevent;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.component.ActionRowComponent;
import com.github.havlli.raidsignupbot.component.ExpiredSelectMenu;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventService;
import com.github.havlli.raidsignupbot.logger.Logger;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public class ScheduledTasks implements Tasks {

    private final Logger logger;
    private final Event event;
    private final EmbedEventService embedEventService;

    public ScheduledTasks(Event event, Logger logger) {
        this.logger = logger;
        this.event = event;
        this.embedEventService = Dependencies.getInstance()
                .getEmbedEventService();

    }

    @Override
    public void run() {
        printExpiredEmbedEvents();
        handleExpiredEmbedEvents();
    }

    public Mono<Void> getSchedulerMono() {
        final int INTERVAL_SECONDS = 5;
        Duration duration = Duration.ofSeconds(INTERVAL_SECONDS);

        logger.log("Scheduler registered");
        Scheduler scheduler = Schedulers.newSingle("EmbedScheduler");

        return Mono.fromRunnable(this::run)
                .delaySubscription(duration)
                .repeat()
                .subscribeOn(scheduler)
                .then();
    }

    private void printExpiredEmbedEvents() {
        logger.log("Scheduled check for expired EmbedEvents...");
        embedEventService.getExpiredEmbedEvents()
                .forEach(embedEvent -> logger.log(embedEvent.getEmbedId()));
    }

    private void handleExpiredEmbedEvents() {
        ActionRowComponent expiredSelectMenu = new ExpiredSelectMenu();

        embedEventService.getExpiredEmbedEvents()
                .forEach(embedEvent -> {
                    Snowflake messageId = Snowflake.of(embedEvent.getEmbedId());
                    Snowflake channelId = Snowflake.of(embedEvent.getDestinationChannelId());
                    event.getClient()
                            .getMessageById(channelId, messageId)
                            .flatMap(message -> message.edit(MessageEditSpec.builder()
                                    .addComponent(expiredSelectMenu.getActionRow())
                                    .build()))
                            .subscribe();

                    embedEventService.removeEmbedEvent(embedEvent);
                });
    }
}
