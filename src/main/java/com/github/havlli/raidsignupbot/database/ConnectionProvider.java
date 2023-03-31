package com.github.havlli.raidsignupbot.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
    void closeConnection(Connection connection);
}
