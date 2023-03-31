package com.github.havlli.raidsignupbot.database;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;

public class DatabaseOperations {

    private static final JdbcConnectionProvider jdbcProvider = new JdbcConnectionProvider();
    public static void insertEmbedEvent(EmbedEvent embedEvent) {
        try (Connection connection = DatabaseConnection.getConnection(jdbcProvider);
             PreparedStatement insertEmbedEventPrep = connection.prepareStatement(Query.INSERT_EMBED_EVENT)) {
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

    public static HashSet<EmbedEvent> fetchActiveEmbedEvents() {
        HashSet<EmbedEvent> embedEventHashSet = new HashSet<>();
        try (Connection connection = DatabaseConnection.getConnection(jdbcProvider);
             PreparedStatement selectActiveEmbedEvents = connection.prepareStatement(Query.SELECT_ACTIVE_EMBED_EVENTS);
             ResultSet resultSet = selectActiveEmbedEvents.executeQuery()) {

            while (resultSet.next()) {
                String id = resultSet.getString(TableStructure.CLM_EMBED_EVENT_ID);
                String name = resultSet.getString(TableStructure.CLM_EMBED_EVENT_NAME);
                String desc = resultSet.getString(TableStructure.CLM_EMBED_EVENT_DESC);
                String[] datetime = resultSet.getString(TableStructure.CLM_EMBED_EVENT_DATETIME).split(" ");
                String date = datetime[0];
                String time = datetime[1];
                String instances = resultSet.getString(TableStructure.CLM_EMBED_EVENT_INSTANCES);
                String size = resultSet.getString(TableStructure.CLM_EMBED_EVENT_SIZE);
                int reserve = resultSet.getInt(TableStructure.CLM_EMBED_EVENT_RESERVE);
                String channel = resultSet.getString(TableStructure.CLM_EMBED_EVENT_DEST_CHANNEL);
                String author = resultSet.getString(TableStructure.CLM_EMBED_EVENT_AUTHOR);
                int active = resultSet.getInt(TableStructure.CLM_EMBED_EVENT_ACTIVE);

                EmbedEvent embedEvent = new EmbedEvent();
                embedEvent.setEmbedId(Long.parseLong(id));
                embedEvent.setName(name);
                embedEvent.setDescription(desc);
                embedEvent.setDate(LocalDate.parse(date));
                embedEvent.setTime(LocalTime.parse(time));
                embedEvent.setInstances(Arrays.stream(instances.split(", ")).toList());
                embedEvent.setMemberSize(size);
                embedEvent.setReservingEnabled(reserve == 1);
                embedEvent.setDestinationChannelId(Long.parseLong(channel));
                embedEvent.setAuthor(author);
                embedEvent.setActive(active == 1);

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
}
