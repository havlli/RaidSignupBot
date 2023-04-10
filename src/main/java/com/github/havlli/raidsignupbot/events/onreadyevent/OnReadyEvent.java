package com.github.havlli.raidsignupbot.events.onreadyevent;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventPersistence;
import com.github.havlli.raidsignupbot.events.EventHandler;
import com.github.havlli.raidsignupbot.events.createevent.EmbedBuilder;
import com.github.havlli.raidsignupbot.logger.Logger;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public class OnReadyEvent implements EventHandler {

    @Override
    public Class<? extends Event> getEventType() {
        return ReadyEvent.class;
    }

    @Override
    public Mono<?> handleEvent(Event event) {
        Logger logger = Dependencies.getInstance().getLogger();
        EmbedEventPersistence.getInstance().getData().forEach(embedEvent -> {
            EmbedBuilder embedBuilder = new EmbedBuilder(
                    embedEvent,
                    Dependencies.getInstance().getSignupUserDAO(),
                    Dependencies.getInstance().getEmbedEventDAO()
            );
            embedBuilder.subscribeInteractions(event.getClient().getEventDispatcher());
        });

        logger.log("EmbedEvent data subscribed!");

        logger.log("Scheduler registered");
        Scheduler scheduler = Schedulers.newSingle("EmbedScheduler");

        return Mono.fromRunnable(OnReadyEvent::scheduledTimeCheck)
                .delaySubscription(Duration.ofSeconds(5))
                .repeat()
                .subscribeOn(scheduler)
                .then();
    }

    private static void scheduledTimeCheck() {
        Dependencies.getInstance().getLogger().log("Scheduled check running");
    }

    private static Mono<Void> loadSignupInteractions(Event event) {
        return Mono.empty();
    }
}
