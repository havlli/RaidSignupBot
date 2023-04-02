package com.github.havlli.raidsignupbot.database;

public enum Table {
    EMBED_EVENT("embed_event");

    private final String tableName;

    Table(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return tableName;
    }
}
