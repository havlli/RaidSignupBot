package com.github.havlli.raidsignupbot.events;

import com.github.havlli.raidsignupbot.events.createevent.CreateEvent;
import com.github.havlli.raidsignupbot.events.deleteevent.DeleteEvent;
import com.github.havlli.raidsignupbot.events.test.TestEvent;
import discord4j.core.GatewayDiscordClient;

import java.util.ArrayList;
import java.util.List;

public class EventSubscriber {
    private static EventSubscriber singleton = null;
    private static List<EventHandler> eventHandlers = null;

    private EventSubscriber() {
    }

    private static EventSubscriber getInstance() {
        if (singleton == null) singleton = new EventSubscriber();
        return singleton;
    }

    private static List<EventHandler> getEventHandlers() {
        getInstance();
        if (eventHandlers == null) eventHandlers = new ArrayList<>();
        return eventHandlers;
    }

    private static void addEvent(EventHandler handler) {
        getEventHandlers().add(handler);
    }

    public static void subscribeEvents(GatewayDiscordClient gatewayDiscordClient) {
        registerEvents();
        for (EventHandler handler : eventHandlers) {
            gatewayDiscordClient.on(handler.getEventType(), handler::handleEvent)
                    .subscribe();
        }
    }

    private static void registerEvents() {
        addEvent(new TestEvent());
        addEvent(new CreateEvent());
        addEvent(new DeleteEvent());
    }
}
