package com.github.havlli.raidsignupbot.embedevent;

import com.github.havlli.raidsignupbot.signupuser.SignupUser;
import com.github.havlli.raidsignupbot.signupuser.SignupUserDAO;

import java.util.HashSet;
import java.util.List;

public class EmbedEventPersistence {
    private static HashSet<EmbedEvent> embedEventHashSet;
    private final EmbedEventDAO embedEventDAO;
    private final SignupUserDAO signupUserDAO;
    public EmbedEventPersistence(EmbedEventDAO embedEventDAO, SignupUserDAO signupUserDAO) {
        this.embedEventDAO = embedEventDAO;
        this.signupUserDAO = signupUserDAO;
        embedEventHashSet = new HashSet<>();
        populateEmbedEventHashSet();
        forEachEmbedEventPopulateSignupUserList();
    }

    private void populateEmbedEventHashSet() {
        embedEventHashSet = embedEventDAO.fetchActiveEmbedEvents();
    }

    private void forEachEmbedEventPopulateSignupUserList() {
        embedEventHashSet.forEach(embedEvent -> {
            String embedEventId = embedEvent.getEmbedId();
            List<SignupUser> signupUserList = signupUserDAO.selectSignupUsersById(embedEventId);
            embedEvent.setSignupUsers(signupUserList);
        });
    }

    public HashSet<EmbedEvent> getData() {
        return embedEventHashSet;
    }

    public void addEmbedEvent(EmbedEvent embedEvent) {
        embedEventHashSet.add(embedEvent);
    }

    public void removeEmbedEvent(EmbedEvent embedEvent) {
        embedEventHashSet.remove(embedEvent);
    }
}
