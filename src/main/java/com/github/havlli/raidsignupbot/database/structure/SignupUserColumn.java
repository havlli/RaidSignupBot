package com.github.havlli.raidsignupbot.database.structure;

public enum SignupUserColumn {
    USER_ID("user_id"),
    USERNAME("username"),
    FIELD_INDEX("field_index"),
    ORDER("ordering"),
    EMBED_EVENT_ID("embed_event_id");

    private final String columnName;

    SignupUserColumn(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public String toString() {
        return columnName;
    }
}
