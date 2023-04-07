package com.github.havlli.raidsignupbot.signupuser;

import com.github.havlli.raidsignupbot.database.ConnectionProvider;
import com.github.havlli.raidsignupbot.database.DatabaseConnection;
import com.github.havlli.raidsignupbot.database.Query;
import com.github.havlli.raidsignupbot.database.structure.SignupUserColumn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SignupUserDAO {

    private final ConnectionProvider provider;
    public SignupUserDAO(ConnectionProvider provider) {
        this.provider = provider;
    }

    public void insertSignupUser(SignupUser signupUser, String embedEventId) {
        try (Connection connection = DatabaseConnection.getConnection(provider);
             PreparedStatement preparedStatement = insertSignupUserPrep(signupUser, embedEventId, connection)) {

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 1) System.out.println("Couldn't insert SignupUser!");
        } catch (SQLException e) {
            System.out.println("Couldn't process PreparedStatement insertSignupUserPrep: " + e.getMessage());
        }
    }

    private static PreparedStatement insertSignupUserPrep(SignupUser signupUser, String embedEventId, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(Query.INSERT_SIGNUP_USER);
        preparedStatement.setString(1, signupUser.getId());
        preparedStatement.setString(2, signupUser.getUsername());
        preparedStatement.setInt(3, signupUser.getFieldIndex());
        preparedStatement.setInt(4, signupUser.getOrder());
        preparedStatement.setString(5, embedEventId);
        return preparedStatement;
    }

    public List<SignupUser> selectSignupUsersById(String embedEventId) {
        try (Connection connection = DatabaseConnection.getConnection(provider);
             PreparedStatement preparedStatement = selectSignupUsersByIdPrep(embedEventId, connection);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            List<SignupUser> signupUserHashSet = new ArrayList<>();
            while(resultSet.next()) {
                SignupUser signupUser = mapSignupUserFromResultSet(resultSet);
                signupUserHashSet.add(signupUser);
            }

            return signupUserHashSet;
        } catch (SQLException e) {
            System.out.println("Couldn't process PreparedStatement selectSignupUsersByIdPrep: " + e.getMessage());
            return null;
        }
    }

    private static SignupUser mapSignupUserFromResultSet(ResultSet resultSet) throws SQLException {
        String userId = resultSet.getString(SignupUserColumn.USER_ID.toString());
        String username = resultSet.getString(SignupUserColumn.USERNAME.toString());
        int fieldIndex = resultSet.getInt(SignupUserColumn.FIELD_INDEX.toString());
        int order = resultSet.getInt(SignupUserColumn.ORDER.toString());
        return new SignupUser(order, userId, username, fieldIndex);
    }

    private static PreparedStatement selectSignupUsersByIdPrep(String embedEventId, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(Query.SELECT_SIGNUP_USER_BY_ID);
        preparedStatement.setString(1, embedEventId);
        return preparedStatement;
    }

    public void updateSignupUserFieldIndex(String userId, int fieldIndex, String embedEventId) {
        try (Connection connection = DatabaseConnection.getConnection(provider);
             PreparedStatement preparedStatement = updateSignupUserFieldIndex(userId, fieldIndex, embedEventId, connection)) {

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows != 1) System.out.println("Couldn't update SignupUser!");
        } catch (SQLException e) {
            System.out.println("Couldn't process PreparedStatement updateSignupUserFieldIndex: " + e.getMessage());
        }
    }

    private static PreparedStatement updateSignupUserFieldIndex(String userId, int fieldIndex, String embedEventId, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(Query.UPDATE_SIGNUP_USER_FIELD_INDEX);
        preparedStatement.setInt(1, fieldIndex);
        preparedStatement.setString(2, userId);
        preparedStatement.setString(3, embedEventId);
        return preparedStatement;
    }
}
