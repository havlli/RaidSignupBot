package com.github.havlli.raidsignupbot.database;

import com.github.havlli.raidsignupbot.database.structure.EmbedEventColumn;
import com.github.havlli.raidsignupbot.database.structure.SignupUserColumn;
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
                    EmbedEventColumn.ACTIVE + ") " +
            "VALUES (?,?,?,?,?,?,?,?,?,?)";

    public static final String SELECT_ACTIVE_EMBED_EVENTS =
            "SELECT * FROM " + Table.EMBED_EVENT +
                    " WHERE " + EmbedEventColumn.ACTIVE + " = 1";

    public static final String INSERT_SIGNUP_USER =
            "INSERT INTO " + Table.SIGNUP_USER +
             " (" + SignupUserColumn.USER_ID + "," +
                    SignupUserColumn.USERNAME + "," +
                    SignupUserColumn.FIELD_INDEX + "," +
                    SignupUserColumn.ORDER + "," +
                    SignupUserColumn.EMBED_EVENT_ID + ") "
            + "VALUES (?,?,?,?,?)";

    public static final String SELECT_SIGNUP_USER_BY_ID =
            "SELECT * FROM " + Table.SIGNUP_USER +
                    " WHERE " + SignupUserColumn.EMBED_EVENT_ID + " LIKE ?" +
                    " ORDER BY " + SignupUserColumn.ORDER;

    public static final String UPDATE_SIGNUP_USER_FIELD_INDEX =
            "UPDATE " + Table.SIGNUP_USER +
                    " SET " + SignupUserColumn.FIELD_INDEX + " = ?" +
                    " WHERE " + SignupUserColumn.USER_ID + " = ?" +
                    " AND " + SignupUserColumn.EMBED_EVENT_ID + " = ?";
}
