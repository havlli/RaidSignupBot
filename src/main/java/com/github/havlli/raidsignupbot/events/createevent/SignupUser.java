package com.github.havlli.raidsignupbot.events.createevent;

import discord4j.core.object.entity.User;

public class SignupUser {
    private final int fieldIndex;
    private final int order;
    private final User user;

    public SignupUser(int order, User user, int fieldIndex) {
        this.fieldIndex = fieldIndex;
        this.order = order;
        this.user = user;
    }

    public int getFieldIndex() {
        return fieldIndex;
    }

    public int getOrder() {
        return order;
    }

    public User getUser() {
        return user;
    }
}
