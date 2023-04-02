package com.github.havlli.raidsignupbot.database;

public enum EmbedEventColumn {
    ID("id"),
    NAME("name"),
    DESCRIPTION("description"),
    DATE_TIME("date_time"),
    INSTANCES("instances"),
    MEMBER_SIZE("member_size"),
    RESERVE_ENABLED("reserve_enabled"),
    DESTINATION_CHANNEL("destination_channel"),
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
