package com.github.havlli.raidsignupbot.embedevent;

import com.github.havlli.raidsignupbot.signupuser.SignupUser;
import com.github.havlli.raidsignupbot.signupuser.SignupUserDAO;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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

    public void removeEmbedEvents(HashSet<String> messageIdsToDelete) {
        embedEventHashSet.removeIf(embedEvent -> messageIdsToDelete.contains(embedEvent.getEmbedId()));
    }

    public EmbedEvent updateEmbedEvent(EmbedEvent updatedEmbedEvent) {
        Iterator<EmbedEvent> iterator = embedEventHashSet.iterator();
        while (iterator.hasNext()) {
            EmbedEvent embedEvent = iterator.next();
            if(embedEvent.getEmbedId().equals(updatedEmbedEvent.getEmbedId())) {
                iterator.remove();
                embedEventHashSet.add(updatedEmbedEvent);
                return embedEvent;
            }
        }
        return null;
    }

    public Optional<EmbedEvent> getEmbedEventById(String embedId) {
        return embedEventHashSet.stream()
                .filter(embedEvent -> embedEvent.getEmbedId().equals(embedId))
                .findFirst();
    }
}
