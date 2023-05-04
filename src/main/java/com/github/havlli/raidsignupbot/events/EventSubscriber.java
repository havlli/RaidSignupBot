package com.github.havlli.raidsignupbot.events;

import com.github.havlli.raidsignupbot.events.clearexpired.ClearExpired;
import com.github.havlli.raidsignupbot.events.createevent.CreateEvent;
import com.github.havlli.raidsignupbot.events.deleteevent.DeleteEvent;
import com.github.havlli.raidsignupbot.events.editevent.EditEvent;
import com.github.havlli.raidsignupbot.events.onreadyevent.OnReadyEvent;
import com.github.havlli.raidsignupbot.events.test.TestEvent;
import discord4j.core.GatewayDiscordClient;

import java.util.ArrayList;
import java.util.List;

public class EventSubscriber {
    private static List<EventHandler> eventHandlers = null;

    private static List<EventHandler> getEventHandlers() {
        if (eventHandlers == null) eventHandlers = new ArrayList<>();
        return eventHandlers;
    }

    private void addEvent(EventHandler handler) {
        getEventHandlers().add(handler);
    }

    public void subscribeEvents(GatewayDiscordClient gatewayDiscordClient) {
        registerEvents();
        for (EventHandler handler : eventHandlers) {
            gatewayDiscordClient.on(handler.getEventType(), handler::handleEvent)
                    .subscribe();
        }
    }

    private void registerEvents() {
        addEvent(new OnReadyEvent());
        addEvent(new TestEvent());
        addEvent(new CreateEvent());
        addEvent(new DeleteEvent());
        addEvent(new EditEvent());
        addEvent(new ClearExpired());
    }
}
