package com.patientx.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:PatientXdatabase.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(URL);
            initializeTables();
            analyzeDatabase();
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }

    private void initializeTables() {
        try (Statement stmt = connection.createStatement()) {
            // Create LabResult table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS LabResult (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "PatientID TEXT NOT NULL," +
                "TestDate TEXT NOT NULL," +
                "TestName TEXT NOT NULL," +
                "Value TEXT NOT NULL," +
                "Unit TEXT," +
                "ReferenceRange TEXT" +
                ")"
            );

            // Create Measurement table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS Measurement (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "PatientID TEXT NOT NULL," +
                "Date TEXT NOT NULL," +
                "Value TEXT NOT NULL," +
                "LabResultID INTEGER," +
                "FOREIGN KEY (LabResultID) REFERENCES LabResult(ID)" +
                ")"
            );

            // Create CMAS table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS CMAS (" +
                "ID TEXT PRIMARY KEY," +
                "PatientID TEXT NOT NULL," +
                "TestDate TEXT NOT NULL," +
                "Score INTEGER NOT NULL," +
                "ScoreType TEXT" +
                ")"
            );

            // Create Patient table if not exists (for PatientX)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS Patient (" +
                "PatientID TEXT PRIMARY KEY," +
                "Name TEXT," +
                "Age INTEGER," +
                "Gender TEXT" +
                ")"
            );

            // Create Patients table if not exists (for other patients)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS Patients (" +
                "PatientID TEXT PRIMARY KEY," +
                "Name TEXT," +
                "Age INTEGER," +
                "Gender TEXT" +
                ")"
            );

        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public void analyzeDatabase() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[] {"TABLE"});
            
            // Just check if tables exist, no output needed
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                // Verify table structure silently
                metaData.getColumns(null, null, tableName, null);
            }
        } catch (SQLException e) {
            System.err.println("Error analyzing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DatabaseManager.getInstance().analyzeDatabase();
    }
} 