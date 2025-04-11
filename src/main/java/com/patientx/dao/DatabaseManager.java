package com.patientx.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:PatientXdatabase.db";
    private static DatabaseManager instance;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
             System.err.println("Failed to load SQLite JDBC driver.");
             // This error could be critical if the application needs to continue.
             // throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    // Constructor private
    private DatabaseManager() {
        // Constructor left empty, initial setup can be called in getInstance
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
            // Perform initial database setup/update here
            instance.performInitialSetup();
        }
        return instance;
    }
    
    // Helper method for configuring connection settings
    private void setupConnection(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // First set timeout, then other pragmas
            stmt.execute("PRAGMA busy_timeout = 5000;"); // 5 second wait time
            stmt.execute("PRAGMA foreign_keys = ON;");
            // System.out.println("Database connection configured: Foreign Keys ON, Busy Timeout 5000ms"); // Optional log
        }
    }

    // Method to return a new, configured connection each time
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        setupConnection(conn); // Always configure new connections
        return conn;
    }
    
    // Method for initial setup and potential schema updates
    // This method should handle its own transaction management
    private void performInitialSetup() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load SQLite JDBC driver.");
            // This error could be critical if the application needs to continue
            // throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL); // Separate connection for setup
             Statement stmt = conn.createStatement()) {

            // Also set up connection settings here (timeout might be important)
            setupConnection(conn);

            // Create tables if they don't exist
            stmt.execute(CREATE_PATIENTS_TABLE);
            stmt.execute(CREATE_LAB_RESULTS_TABLE);

            // Update existing records if needed
            updateExistingRecords(conn);

        } catch (SQLException e) {
            System.err.println("Error during initial setup: " + e.getMessage());
            // System.out.println("Initial Setup: No patient names needed updating."); // Optional log
            e.printStackTrace();
            
            // Additional handling can be done here if this error is critical
            // throw new RuntimeException("Failed to perform initial setup", e);
        }
    }
    // Application continuation might be dangerous
} 