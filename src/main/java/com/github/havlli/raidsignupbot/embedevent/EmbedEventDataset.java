package com.github.havlli.raidsignupbot.embedevent;

import java.util.HashSet;

public class EmbedEventDataset {
    private static EmbedEventDataset singleton = null;
    private EmbedEventDataset() {
        embedEventHashSet = new HashSet<>();
        populateEmbedEventHashSet();
    }

    public static EmbedEventDataset getInstance() {
        if(singleton == null) singleton = new EmbedEventDataset();
        return singleton;
    }

    private static HashSet<EmbedEvent> embedEventHashSet;

    private void populateEmbedEventHashSet() {
        embedEventHashSet = EmbedEventDAO.fetchActiveEmbedEvents();
    }

    public HashSet<EmbedEvent> getData() {
        return embedEventHashSet;
    }

    public void addEmbedEvent(EmbedEvent embedEvent) {
        embedEventHashSet.add(embedEvent);
    }
}
