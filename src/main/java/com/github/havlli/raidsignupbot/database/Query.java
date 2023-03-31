package com.github.havlli.raidsignupbot.database;

public class Query extends TableStructure {
    public static final String INSERT_EMBED_EVENT =
            "INSERT INTO "+ TBL_EMBED_EVENT +
            " (" + CLM_EMBED_EVENT_ID + "," +
            CLM_EMBED_EVENT_NAME + "," +
            CLM_EMBED_EVENT_DESC + "," +
            CLM_EMBED_EVENT_DATETIME + "," +
            CLM_EMBED_EVENT_INSTANCES + "," +
            CLM_EMBED_EVENT_SIZE + "," +
            CLM_EMBED_EVENT_RESERVE + "," +
            CLM_EMBED_EVENT_DEST_CHANNEL + "," +
            CLM_EMBED_EVENT_AUTHOR + "," +
            CLM_EMBED_EVENT_ACTIVE + ")" +
            " VALUES (?,?,?,?,?,?,?,?,?,?)";

    public static final String SELECT_ACTIVE_EMBED_EVENTS =
            "SELECT * FROM " + TBL_EMBED_EVENT +
            " WHERE " + CLM_EMBED_EVENT_ACTIVE + " = 1";
}
