package com.github.havlli.raidsignupbot.database.structure;

public enum EmbedEventColumn {
    ID("id"),
    NAME("name"),
    DESCRIPTION("desc"),
    DATE_TIME("datetime"),
    INSTANCES("instances"),
    MEMBER_SIZE("size"),
    RESERVE_ENABLED("reserve_enabled"),
    DESTINATION_CHANNEL("dest_channel"),
    AUTHOR("author"),
    ACTIVE("active");

    private final String columnName;

    EmbedEventColumn(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public String toString() {
        return columnName;
    }
}
