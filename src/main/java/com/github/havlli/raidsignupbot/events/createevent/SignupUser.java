package com.github.havlli.raidsignupbot.events.createevent;

public class SignupUser {
    private int fieldIndex;
    private final int order;
    private final String username;
    private final String id;

    public SignupUser(int order, String userId, String username, int fieldIndex) {
        this.fieldIndex = fieldIndex;
        this.order = order;
        this.username = username;
        this.id = userId;
    }

    public int getFieldIndex() {
        return fieldIndex;
    }

    public void setFieldIndex(int index) {
        this.fieldIndex = index;
    }

    public int getOrder() {
        return order;
    }

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }
}
