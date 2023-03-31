package com.github.havlli.raidsignupbot.database;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection;

    private DatabaseConnection() {}

    public static Connection getConnection(ConnectionProvider provider) throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = provider.getConnection();
        }
        return connection;
    }

    public static void closeConnection(ConnectionProvider provider) {
        provider.closeConnection(connection);
        connection = null;
    }
}
