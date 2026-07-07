package com.hotelmanagement.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:sqlserver://localhost\\SQLEXPRESS;" +
                    "databaseName=HotelBookingSystem;" +
                    "encrypt=true;" +
                    "trustServerCertificate=true;";

    private static final String USER = "sa";
    private static final String PASSWORD = "280706";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kết nối SQL Server!", e);
        }
    }
}