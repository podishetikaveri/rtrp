package com.smartqueue.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String HOST = System.getenv("DB_HOST");
    private static final String PORT = System.getenv("DB_PORT"); // usually 5432
    private static final String NAME = System.getenv("DB_NAME");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (HOST == null || PORT == null || NAME == null || USER == null || PASSWORD == null) {
            throw new SQLException("Database environment variables not set");
        }

        String url = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + NAME + "?sslmode=require";
        return DriverManager.getConnection(url, USER, PASSWORD);
    }
}

