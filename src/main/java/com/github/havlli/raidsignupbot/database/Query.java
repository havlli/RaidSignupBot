package com.github.havlli.raidsignupbot.database;

import com.github.havlli.raidsignupbot.database.structure.EmbedEventColumn;
import com.github.havlli.raidsignupbot.database.structure.Table;

public class Query {
    public static final String INSERT_EMBED_EVENT =
            "INSERT INTO "+ Table.EMBED_EVENT +
            " (" + EmbedEventColumn.ID + "," +
            EmbedEventColumn.NAME + "," +
            EmbedEventColumn.DESCRIPTION + "," +
            EmbedEventColumn.DATE_TIME + "," +
            EmbedEventColumn.INSTANCES + "," +
            EmbedEventColumn.MEMBER_SIZE + "," +
            EmbedEventColumn.RESERVE_ENABLED + "," +
            EmbedEventColumn.DESTINATION_CHANNEL + "," +
            EmbedEventColumn.AUTHOR + "," +
            EmbedEventColumn.ACTIVE + ")" +
            " VALUES (?,?,?,?,?,?,?,?,?,?)";

    public static final String SELECT_ACTIVE_EMBED_EVENTS =
            "SELECT * FROM " + Table.EMBED_EVENT +
            " WHERE " + EmbedEventColumn.ACTIVE + " = 1";
}
