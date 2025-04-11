package com.patientx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:PatientXdatabase.db";
    
    public static void testConnection() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement stmt = connection.createStatement()) {
            
            stmt.setQueryTimeout(30);
            
            // Test query to get table names
            ResultSet rs = connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
            System.out.println("Available tables in database:");
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                System.out.println("- " + tableName);
            }
            
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testConnection();
    }
} 