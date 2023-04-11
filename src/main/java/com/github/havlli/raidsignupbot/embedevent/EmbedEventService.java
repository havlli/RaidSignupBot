package com.github.havlli.raidsignupbot.embedevent;

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
}
