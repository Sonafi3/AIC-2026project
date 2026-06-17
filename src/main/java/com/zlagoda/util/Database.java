package com.zlagoda.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:postgresql://localhost:5432/zlagoda_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1qw23e";

    private static Connection connection;

    private Database() {
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Нове підключення до бази даних відкрито");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Помилка підключення до БД: " + e.getMessage());
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Закриття з'єднання з базою даних");
                }
            } catch (SQLException e) {
                System.err.println("Помилка при закритті з'єднання: " + e.getMessage());
            }
        }
    }
}