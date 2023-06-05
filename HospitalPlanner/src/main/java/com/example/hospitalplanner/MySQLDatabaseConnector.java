package com.example.hospitalplanner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class MySQLDatabaseConnector {
    private final String url;
    private final String username;
    private final String password;
    private Connection connection;

    public MySQLDatabaseConnector(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC driver not found.");
            }
            connection = DriverManager.getConnection(url, username, password);
        }
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}

