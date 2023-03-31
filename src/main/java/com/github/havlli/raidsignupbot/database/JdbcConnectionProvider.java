package com.github.havlli.raidsignupbot.database;

import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnectionProvider implements ConnectionProvider{
    private static final String DB_FILE = "database.db";
    private static final String RESOURCE_PATH = FileSystems.getDefault()
            .getPath(".", "src", "main", "resources")
            .normalize()
            .toAbsolutePath() + "\\";
    private static final String CONNECTION_STRING = "jdbc:sqlite:" + RESOURCE_PATH + DB_FILE;

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_STRING);
    }

    @Override
    public void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Couldn't close connection: " + e.getMessage());
        }
    }
}
