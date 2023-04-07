package com.github.havlli.raidsignupbot.embedevent;

import com.github.havlli.raidsignupbot.database.ConnectionProvider;
import com.github.havlli.raidsignupbot.database.JdbcConnectionProvider;
import com.github.havlli.raidsignupbot.signupuser.SignupUser;
import com.github.havlli.raidsignupbot.signupuser.SignupUserDAO;

import java.util.HashSet;
import java.util.List;

public class EmbedEventPersistence {
    private static EmbedEventPersistence singleton = null;
    private static HashSet<EmbedEvent> embedEventHashSet;
    private final EmbedEventDAO embedEventDAO;
    private final SignupUserDAO signupUserDAO;
    private EmbedEventPersistence(EmbedEventDAO embedEventDAO, SignupUserDAO signupUserDAO) {
        this.embedEventDAO = embedEventDAO;
        this.signupUserDAO = signupUserDAO;
        embedEventHashSet = new HashSet<>();
        populateEmbedEventHashSet();
        forEachEmbedEventPopulateSignupUserList();
    }

    public static EmbedEventPersistence getInstance() {
        if(singleton == null) {
            ConnectionProvider provider = new JdbcConnectionProvider();
            singleton = new EmbedEventPersistence(new EmbedEventDAO(provider), new SignupUserDAO(provider));
        }
        return singleton;
    }

    private void populateEmbedEventHashSet() {
        embedEventHashSet = embedEventDAO.fetchActiveEmbedEvents();
    }

    private void forEachEmbedEventPopulateSignupUserList() {
        embedEventHashSet.forEach(embedEvent -> {
            String embedEventId = embedEvent.getEmbedId().toString();
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
}
