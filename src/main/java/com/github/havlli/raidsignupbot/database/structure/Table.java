package com.github.havlli.raidsignupbot.database.structure;

public enum Table {
    EMBED_EVENT("Embed_event"),
    SIGNUP_USER("Signup_user");

    private final String tableName;

    Table(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return tableName;
    }
}
