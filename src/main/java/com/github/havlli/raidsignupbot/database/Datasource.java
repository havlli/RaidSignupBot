package com.github.havlli.raidsignupbot.database;

import com.github.havlli.raidsignupbot.model.EmbedEvent;

import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Datasource {
    private static Datasource singleton = null;
    private Datasource() { }

    public static Datasource getInstance() {
        if (singleton == null) singleton = new Datasource();
        return singleton;
    }

    public static final String DB_FILE = "database.db";
    public static final String RESOURCE_PATH = FileSystems.getDefault()
            .getPath(".","src","main","resources")
            .normalize()
            .toAbsolutePath() + "\\";
    public static final String CONNECTION_STRING = "jdbc:sqlite:" + RESOURCE_PATH + DB_FILE;
    private Connection connection;

    public boolean open() {
        try {
            connection = DriverManager.getConnection(CONNECTION_STRING);
            return true;
        } catch (SQLException e) {
            System.out.println("Couldn't open connection: " + e.getMessage());
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.out.println("Couldn't close connection: " + e.getMessage());
        }
    }

    public boolean insertEmbedEvent(EmbedEvent embedEvent) {
        PreparedStatement insertEmbedEventPrep;
        try {
            if (!open()) return false;
            insertEmbedEventPrep = connection.prepareStatement(Query.INSERT_EMBED_EVENT);
            insertEmbedEventPrep.setString(1, embedEvent.getEmbedId().toString());
            insertEmbedEventPrep.setString(2, embedEvent.getName());
            insertEmbedEventPrep.setString(3, embedEvent.getDescription());
            insertEmbedEventPrep.setString(4, embedEvent.getDate().toString() + " " + embedEvent.getTime().toString());
            insertEmbedEventPrep.setString(5, String.join(", ", embedEvent.getInstances()));
            insertEmbedEventPrep.setString(6, embedEvent.getMemberSize());
            insertEmbedEventPrep.setInt(7, embedEvent.isReservingEnabled() ? 1 : 0);
            insertEmbedEventPrep.setString(8, embedEvent.getDestinationChannelId().toString());
            insertEmbedEventPrep.setString(9, embedEvent.getAuthor().getUsername() + "#" + embedEvent.getAuthor().getDiscriminator());
            int affectedRows = insertEmbedEventPrep.executeUpdate();
            if (affectedRows != 1) {
                System.out.println("Couldn't insert new category");
                return false;
            }
            insertEmbedEventPrep.close();
            return true;
        } catch (SQLException e) {
            System.out.println("Couldn't open connection: " + e.getMessage());
            return false;
        } finally {
            closeConnection();
        }
    }
}
