package com.github.havlli.raidsignupbot.embedevent;

import com.github.havlli.raidsignupbot.database.ConnectionProvider;
import com.github.havlli.raidsignupbot.database.DatabaseConnection;
import com.github.havlli.raidsignupbot.database.Query;
import com.github.havlli.raidsignupbot.database.structure.EmbedEventColumn;
import com.github.havlli.raidsignupbot.logger.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;

public class EmbedEventDAO {
    private final ConnectionProvider provider;
    private final Logger logger;
    public EmbedEventDAO(ConnectionProvider provider, Logger logger) {
        this.provider = provider;
        this.logger = logger;
    }

    public void insertEmbedEvent(EmbedEvent embedEvent) {
        try (Connection connection = DatabaseConnection.getConnection(provider);
             PreparedStatement insertEmbedEventPrep = createInsertEmbedEventPrep(connection, embedEvent)) {

            int affectedRows = insertEmbedEventPrep.executeUpdate();
            if (affectedRows != 1) {
                logger.log("Couldn't insert new EmbedEvent");
            }
        } catch (SQLException e) {
            logger.log("Couldn't process PreparedStatement insertEmbedEventPrep: " + e.getMessage());
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

    public HashSet<EmbedEvent> fetchActiveEmbedEvents() {
        HashSet<EmbedEvent> embedEventHashSet = new HashSet<>();
        try (Connection connection = DatabaseConnection.getConnection(provider);
             PreparedStatement selectActiveEmbedEvents = connection.prepareStatement(Query.SELECT_ACTIVE_EMBED_EVENTS);
             ResultSet resultSet = selectActiveEmbedEvents.executeQuery()) {

            while (resultSet.next()) {
                EmbedEvent embedEvent = mapEmbedEventFromResultSet(resultSet);
                embedEventHashSet.add(embedEvent);
            }

            return embedEventHashSet;
        } catch (SQLException e) {
            logger.log("Couldn't process PreparedStatement selectActiveEmbedEvents: " + e.getMessage());

            return new HashSet<>();
        }
    }

    private static EmbedEvent mapEmbedEventFromResultSet(ResultSet resultSet) throws SQLException {
        EmbedEvent embedEvent = new EmbedEvent();
        embedEvent.setEmbedId(resultSet.getLong(EmbedEventColumn.ID.toString()));
        embedEvent.setName(resultSet.getString(EmbedEventColumn.NAME.toString()));
        embedEvent.setDescription(resultSet.getString(EmbedEventColumn.DESCRIPTION.toString()));
        String[] datetime = resultSet.getString(EmbedEventColumn.DATE_TIME.toString()).split(" ");
        embedEvent.setDate(LocalDate.parse(datetime[0]));
        embedEvent.setTime(LocalTime.parse(datetime[1]));
        embedEvent.setInstances(Arrays.stream(resultSet.getString(EmbedEventColumn.INSTANCES.toString()).split(", ")).toList());
        embedEvent.setMemberSize(resultSet.getString(EmbedEventColumn.MEMBER_SIZE.toString()));
        embedEvent.setReservingEnabled(resultSet.getInt(EmbedEventColumn.RESERVE_ENABLED.toString()) == 1);
        embedEvent.setDestinationChannelId(resultSet.getLong(EmbedEventColumn.DESTINATION_CHANNEL.toString()));
        embedEvent.setAuthor(resultSet.getString(EmbedEventColumn.AUTHOR.toString()));
        embedEvent.setActive(resultSet.getInt(EmbedEventColumn.ACTIVE.toString()) == 1);

        return embedEvent;
    }

    public void updateExpiredEmbedEvent(EmbedEvent embedEvent, boolean isActive) {
        try(Connection connection = DatabaseConnection.getConnection(provider);
            PreparedStatement preparedStatement = updateExpiredEmbedEventPrep(connection, embedEvent, isActive)) {

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 1) logger.log("EmbedEvent wasn't updated");
        } catch (SQLException e) {
            logger.log("There was error with processing updateExpiredEmbedEvent! " + e.getMessage());
        }
    }
    private static PreparedStatement updateExpiredEmbedEventPrep(Connection connection, EmbedEvent embedEvent, boolean isActive) throws SQLException {
        int active = isActive ? 1 : 0;
        PreparedStatement preparedStatement = connection.prepareStatement(Query.UPDATE_EXPIRED_EMBED_EVENT);
        preparedStatement.setInt(1, active);
        preparedStatement.setString(2, embedEvent.getEmbedId().toString());

        return preparedStatement;
    }
}
