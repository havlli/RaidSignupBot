package com.github.havlli.raidsignupbot.embedevent;

import com.github.havlli.raidsignupbot.database.JdbcConnectionProvider;
import com.github.havlli.raidsignupbot.events.createevent.SignupUser;
import com.github.havlli.raidsignupbot.events.createevent.SignupUserDAO;

import java.util.HashSet;
import java.util.List;

public class EmbedEventDataset {
    private static EmbedEventDataset singleton = null;
    private EmbedEventDataset() {
        populateEmbedEventHashSet();
        forEachEmbedEventPopulateSignupUserList();
    }

    public static EmbedEventDataset getInstance() {
        if(singleton == null) singleton = new EmbedEventDataset();
        return singleton;
    }

    private static HashSet<EmbedEvent> embedEventHashSet = new HashSet<>();

    private void populateEmbedEventHashSet() {
        EmbedEventDAO embedEventDAO = new EmbedEventDAO(new JdbcConnectionProvider());
        embedEventHashSet = embedEventDAO.fetchActiveEmbedEvents();
    }

    private void forEachEmbedEventPopulateSignupUserList() {
        embedEventHashSet.forEach(embedEvent -> {
            String embedEventId = embedEvent.getEmbedId().toString();
            List<SignupUser> signupUserList = SignupUserDAO.selectSignupUsersById(embedEventId);
            embedEvent.setSignupUsers(signupUserList);
        });
    }

    public HashSet<EmbedEvent> getData() {
        return embedEventHashSet;
    }

    public void addEmbedEvent(EmbedEvent embedEvent) {
        embedEventHashSet.add(embedEvent);
    }
}
