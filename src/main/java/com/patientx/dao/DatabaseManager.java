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
        // Initialize database setup/update here
        setupDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }
    
    // Helper method for connection settings
    private void setupConnection(Connection conn) throws SQLException {
        if (conn != null) {
            Statement stmt = conn.createStatement();
            stmt.setQueryTimeout(30);
            stmt.execute("PRAGMA foreign_keys = ON");
        }
    }

    // Returns a new configured connection each time
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        setupConnection(conn); // Always configure new connections
        return conn;
    }
    
    private void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL); // Separate connection for setup
             Statement stmt = conn.createStatement()) {
            
            // Set connection settings here (timeout is important)
            setupConnection(conn);
            
            // Create tables if they don't exist
            createTables(stmt);
            
        } catch (SQLException e) {
            System.err.println("Database setup error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTables(Statement stmt) throws SQLException {
        // Create tables...
    }
} 