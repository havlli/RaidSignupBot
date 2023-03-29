package com.github.havlli.raidsignupbot.events.onreadyevent;

import com.github.havlli.raidsignupbot.events.EventHandler;
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
        System.out.println("Scheduler registered");
        Scheduler scheduler = Schedulers.newSingle("EmbedScheduler");

        return Mono.fromRunnable(OnReadyEvent::scheduledTimeCheck)
                .delaySubscription(Duration.ofSeconds(5))
                .repeat()
                .subscribeOn(scheduler)
                .then();
    }

    private static void scheduledTimeCheck() {
        System.out.println("Scheduled check running");
    }
}
