package com.github.havlli.raidsignupbot.embedevent;

import java.util.HashSet;
import java.util.stream.Collectors;

public class EmbedEventService {

    private final EmbedEventDAO embedEventDAO;
    private final EmbedEventPersistence embedEventPersistence;

    public EmbedEventService(EmbedEventDAO embedEventDAO, EmbedEventPersistence embedEventPersistence) {
        this.embedEventDAO = embedEventDAO;
        this.embedEventPersistence = embedEventPersistence;
    }

    public void addEmbedEvent(EmbedEvent embedEvent) {
        embedEventPersistence.addEmbedEvent(embedEvent);
        embedEventDAO.insertEmbedEvent(embedEvent);
    }

    public void removeEmbedEvent(EmbedEvent embedEvent) {
        embedEventPersistence.removeEmbedEvent(embedEvent);
        embedEventDAO.updateExpiredEmbedEvent(embedEvent, false);
    }

    public void removeEmbedEvents(HashSet<String> messageIdsToDelete) {
        embedEventPersistence.removeEmbedEvents(messageIdsToDelete);
    }

    public void updateEmbedEvent(EmbedEvent embedEvent) {
        embedEventPersistence.updateEmbedEvent(embedEvent);
        embedEventDAO.updateEmbedEvent(embedEvent);
    }

    public HashSet<EmbedEvent> getActiveEmbedEvents() {
        return embedEventPersistence.getData();
    }

    public HashSet<EmbedEvent> getExpiredEmbedEvents() {
        return getActiveEmbedEvents().stream()
                .filter(EmbedEventValidator::isExpired)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
