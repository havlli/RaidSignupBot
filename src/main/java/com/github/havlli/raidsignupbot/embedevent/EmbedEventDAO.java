package com.github.havlli.raidsignupbot.embedevent;

import com.github.havlli.raidsignupbot.database.DatabaseConnection;
import com.github.havlli.raidsignupbot.database.JdbcConnectionProvider;
import com.github.havlli.raidsignupbot.database.Query;
import com.github.havlli.raidsignupbot.database.TableStructure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;

public class EmbedEventDAO {
    private static final JdbcConnectionProvider jdbcProvider = new JdbcConnectionProvider();

    public static void insertEmbedEvent(EmbedEvent embedEvent) {
        try (Connection connection = DatabaseConnection.getConnection(jdbcProvider);
             PreparedStatement insertEmbedEventPrep = createInsertEmbedEventPrep(connection, embedEvent)) {
            int affectedRows = insertEmbedEventPrep.executeUpdate();
            if (affectedRows != 1) {
                System.out.println("Couldn't insert new EmbedEvent");
            }
        } catch (SQLException e) {
            System.out.println("Couldn't process PreparedStatement insertEmbedEventPrep: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection(jdbcProvider);
        }
    }

    private static PreparedStatement createInsertEmbedEventPrep(Connection connection, EmbedEvent embedEvent) throws SQLException {
        PreparedStatement insertEmbedEventPrep = connection.prepareStatement(Query.INSERT_EMBED_EVENT);
        insertEmbedEventPrep.setString(1, embedEvent.getEmbedId().toString());
        insertEmbedEventPrep.setString(2, embedEvent.getName());
        insertEmbedEventPrep.setString(3, embedEvent.getDescription());
        insertEmbedEventPrep.setString(4, embedEvent.getDate().toString() + " " + embedEvent.getTime().toString());
        insertEmbedEventPrep.setString(5, String.join(", ", embedEvent.getInstances()));
        insertEmbedEventPrep.setString(6, embedEvent.getMemberSize());
        insertEmbedEventPrep.setInt(7, embedEvent.isReservingEnabled() ? 1 : 0);
        insertEmbedEventPrep.setString(8, embedEvent.getDestinationChannelId().toString());
        insertEmbedEventPrep.setString(9, embedEvent.getAuthor());
        insertEmbedEventPrep.setInt(10, 1);
        return insertEmbedEventPrep;
    }

    public static HashSet<EmbedEvent> fetchActiveEmbedEvents() {
        HashSet<EmbedEvent> embedEventHashSet = new HashSet<>();
        try (Connection connection = DatabaseConnection.getConnection(jdbcProvider);
             PreparedStatement selectActiveEmbedEvents = connection.prepareStatement(Query.SELECT_ACTIVE_EMBED_EVENTS);
             ResultSet resultSet = selectActiveEmbedEvents.executeQuery()) {

            while (resultSet.next()) {
                EmbedEvent embedEvent = mapEmbedEventFromResultSet(resultSet);
                embedEventHashSet.add(embedEvent);
            }

            return embedEventHashSet;
        } catch (SQLException e) {
            System.out.println("Couldn't process PreparedStatement selectActiveEmbedEvents: " + e.getMessage());
            return null;
        } finally {
            DatabaseConnection.closeConnection(jdbcProvider);
        }
    }

    private static EmbedEvent mapEmbedEventFromResultSet(ResultSet resultSet) throws SQLException {
        EmbedEvent embedEvent = new EmbedEvent();
        embedEvent.setEmbedId(resultSet.getLong(TableStructure.CLM_EMBED_EVENT_ID));
        embedEvent.setName(resultSet.getString(TableStructure.CLM_EMBED_EVENT_NAME));
        embedEvent.setDescription(resultSet.getString(TableStructure.CLM_EMBED_EVENT_DESC));
        String[] datetime = resultSet.getString(TableStructure.CLM_EMBED_EVENT_DATETIME).split(" ");
        embedEvent.setDate(LocalDate.parse(datetime[0]));
        embedEvent.setTime(LocalTime.parse(datetime[1]));
        embedEvent.setInstances(Arrays.stream(resultSet.getString(TableStructure.CLM_EMBED_EVENT_INSTANCES).split(", ")).toList());
        embedEvent.setMemberSize(resultSet.getString(TableStructure.CLM_EMBED_EVENT_SIZE));
        embedEvent.setReservingEnabled(resultSet.getInt(TableStructure.CLM_EMBED_EVENT_RESERVE) == 1);
        embedEvent.setDestinationChannelId(resultSet.getLong(TableStructure.CLM_EMBED_EVENT_DEST_CHANNEL));
        embedEvent.setAuthor(resultSet.getString(TableStructure.CLM_EMBED_EVENT_AUTHOR));
        embedEvent.setActive(resultSet.getInt(TableStructure.CLM_EMBED_EVENT_ACTIVE) == 1);
        return embedEvent;
    }
}
