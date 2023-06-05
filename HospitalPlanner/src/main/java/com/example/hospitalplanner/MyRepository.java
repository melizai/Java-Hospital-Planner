package com.example.hospitalplanner;

import java.sql.*;

public class MyRepository {
    private final Connection connection;

    public MyRepository(Connection connection) {
        this.connection = connection;
    }

    public ResultSet select(String query) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        return resultSet;
    }

    public int insert(String query) throws SQLException {
        Statement statement = connection.createStatement();
        int rowsAffected = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        }
        return 0;
    }

    public int update(String query) throws SQLException {
        Statement statement = connection.createStatement();
        int rowsAffected = statement.executeUpdate(query);
        return rowsAffected;
    }

    public int delete(String query) throws SQLException {
        Statement statement = connection.createStatement();
        int rowsAffected = statement.executeUpdate(query);
        return rowsAffected;
    }

}

