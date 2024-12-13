package com.blockchain.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private DatabaseConnection() {
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection(String dbUrl) throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }
}
