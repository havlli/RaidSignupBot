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

    private PreparedStatement createInsertEmbedEventPrep(Connection connection, EmbedEvent embedEvent) throws SQLException {
        PreparedStatement insertEmbedEventPrep = connection.prepareStatement(Query.INSERT_EMBED_EVENT);
        insertEmbedEventPrep.setString(1, embedEvent.getEmbedId());
        insertEmbedEventPrep.setString(2, embedEvent.getName());
        insertEmbedEventPrep.setString(3, embedEvent.getDescription());
        insertEmbedEventPrep.setString(4, embedEvent.getDateTimeString(" "));
        insertEmbedEventPrep.setString(5, embedEvent.getInstances());
        insertEmbedEventPrep.setString(6, embedEvent.getMemberSize());
        insertEmbedEventPrep.setInt(7, embedEvent.isReservingEnabled() ? 1 : 0);
        insertEmbedEventPrep.setString(8, embedEvent.getDestinationChannelId());
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

    private EmbedEvent mapEmbedEventFromResultSet(ResultSet resultSet) throws SQLException {
        String embedId = resultSet.getString(EmbedEventColumn.ID.toString());
        String name = resultSet.getString(EmbedEventColumn.NAME.toString());
        String description = resultSet.getString(EmbedEventColumn.DESCRIPTION.toString());
        String instances = resultSet.getString(EmbedEventColumn.INSTANCES.toString());
        String memberSize = resultSet.getString(EmbedEventColumn.MEMBER_SIZE.toString());
        int reserving = resultSet.getInt(EmbedEventColumn.RESERVE_ENABLED.toString());
        String destinationChannel = resultSet.getString(EmbedEventColumn.DESTINATION_CHANNEL.toString());
        String author = resultSet.getString(EmbedEventColumn.AUTHOR.toString());
        String[] datetime = resultSet.getString(EmbedEventColumn.DATE_TIME.toString()).split(" ");
        String date = datetime[0];
        String time = datetime[1];

        return EmbedEvent.builder()
                .addEmbedId(embedId)
                .addName(name)
                .addDescription(description)
                .addInstances(instances)
                .addMemberSize(memberSize)
                .addReservingEnabled(reserving)
                .addDestinationChannel(destinationChannel)
                .addAuthor(author)
                .addDate(date)
                .addTime(time)
                .build();
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
    private PreparedStatement updateExpiredEmbedEventPrep(Connection connection, EmbedEvent embedEvent, boolean isActive) throws SQLException {
        int active = isActive ? 1 : 0;
        PreparedStatement preparedStatement = connection.prepareStatement(Query.UPDATE_EXPIRED_EMBED_EVENT);
        preparedStatement.setInt(1, active);
        preparedStatement.setString(2, embedEvent.getEmbedId());

        return preparedStatement;
    }

    public void updateEmbedEvent(EmbedEvent embedEvent) {
        try(Connection connection = DatabaseConnection.getConnection(provider);
            PreparedStatement preparedStatement = updateEmbedEventPrep(connection, embedEvent)) {

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 1) logger.log("EmbedEvent wasn't updated");
        } catch (SQLException e) {
            logger.log("There was error with processing updateEmbedEvent! " + e.getMessage());
        }
    }

    private PreparedStatement updateEmbedEventPrep(Connection connection, EmbedEvent embedEvent) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(Query.UPDATE_EMBED_EVENT);
        preparedStatement.setString(1, embedEvent.getName());
        preparedStatement.setString(2, embedEvent.getDescription());
        preparedStatement.setString(3, embedEvent.getDateTimeString(" "));
        preparedStatement.setString(4, embedEvent.getInstances());
        preparedStatement.setString(5, embedEvent.getMemberSize());
        preparedStatement.setInt(6, embedEvent.isReservingEnabled() ? 1 : 0);

        preparedStatement.setString(7, embedEvent.getEmbedId());

        return preparedStatement;
    }
}
