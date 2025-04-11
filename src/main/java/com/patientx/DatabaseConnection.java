package com.patientx;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_FILENAME = "PatientXdatabase.db";
    private static String dbPath;
    
    static {
        try {
            // Check for database in current directory
            File currentDirDb = new File(DB_FILENAME);
            if (currentDirDb.exists()) {
                // Use existing database if found
                dbPath = currentDirDb.getAbsolutePath();
                System.out.println("Using existing database: " + dbPath);
            } else {
                // Look for database in JAR directory
                String jarPath = DatabaseConnection.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
                File jarDir = new File(jarPath).getParentFile();
                File dbFile = new File(jarDir, DB_FILENAME);
                
                if (dbFile.exists()) {
                    // Use database from JAR directory
                    dbPath = dbFile.getAbsolutePath();
                    System.out.println("Using database from JAR directory: " + dbPath);
                } else {
                    // Create new database in current directory
                    dbPath = currentDirDb.getAbsolutePath();
                    System.out.println("Creating new database: " + dbPath);
                }
            }
            
            // Test connection
            testConnection();
            
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            // Use current directory as fallback
            dbPath = new File(DB_FILENAME).getAbsolutePath();
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
    
    public static void testConnection() {
        try (Connection connection = getConnection();
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