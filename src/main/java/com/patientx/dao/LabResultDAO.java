package com.patientx.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.patientx.database.DatabaseManager;
import com.patientx.model.LabResult;

public class LabResultDAO {
    private final DatabaseManager dbManager;

    public LabResultDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public List<LabResult> getLabResults(String patientId) {
        List<LabResult> results = new ArrayList<>();
        String query = 
            "SELECT lr.LabResultID, lr.PatientID, m.DateTime as TestDate, " +
            "lr.ResultName_English as TestName, m.Value, lr.Unit, '' as ReferenceRange " +
            "FROM LabResult lr " +
            "JOIN Measurement m ON lr.LabResultID = m.LabResultID " +
            "WHERE lr.PatientID = ? " +
            "ORDER BY m.DateTime DESC";
        
        System.out.println("Executing query for patient: " + patientId);
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, patientId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LabResult result = new LabResult(
                        0, // Using 0 as ID since we don't need it internally
                        rs.getString("PatientID"),
                        rs.getString("TestDate"),
                        rs.getString("TestName"),
                        rs.getString("Value"),
                        rs.getString("Unit"),
                        rs.getString("ReferenceRange")
                    );
                    results.add(result);
                }
            }
            
            if (results.isEmpty()) {
                System.out.println("No laboratory results found for patient ID: " + patientId);
            } else {
                System.out.println("Found " + results.size() + " results for patient ID: " + patientId);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving lab results: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }

    public void printAllLabResults() {
        String query = 
            "SELECT lr.LabResultID, lr.PatientID, m.DateTime as TestDate, " +
            "lr.ResultName_English as TestName, m.Value, lr.Unit " +
            "FROM LabResult lr " +
            "JOIN Measurement m ON lr.LabResultID = m.LabResultID " +
            "ORDER BY m.DateTime DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                LabResult result = new LabResult(
                    "0", // Using "0" as ID since we don't need it internally
                    rs.getString("PatientID"),
                    rs.getString("TestDate"),
                    rs.getString("TestName"),
                    rs.getString("Value"),
                    rs.getString("Unit"),
                    ""  // No reference range in database
                );
                System.out.println(result);
            }
        } catch (SQLException e) {
            System.err.println("Error printing lab results: " + e.getMessage());
        }
    }

    public void createLabResultsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS LabResult (" +
                    "LabResultID TEXT PRIMARY KEY," +
                    "PatientID TEXT NOT NULL," +
                    "ResultName_English TEXT NOT NULL," +
                    "Unit TEXT)";
        
        String measurementSql = "CREATE TABLE IF NOT EXISTS Measurement (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "LabResultID TEXT NOT NULL," +
                    "DateTime TEXT NOT NULL," +
                    "Value TEXT NOT NULL," +
                    "FOREIGN KEY (LabResultID) REFERENCES LabResult(LabResultID))";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            stmt.executeUpdate(measurementSql);
            System.out.println("LabResult and Measurement tables created successfully");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public boolean addLabResult(String patientId, String testDate, String testName, String value, String unit, String referenceRange) {
        String labResultId = java.util.UUID.randomUUID().toString();
        
        int maxRetries = 3;
        int retryCount = 0;
        int retryDelayMs = 1000;
        
        while (retryCount < maxRetries) {
            Connection conn = null;
            PreparedStatement labPstmt = null;
            PreparedStatement measurePstmt = null;
            
            try {
                conn = dbManager.getConnection();
                conn.setAutoCommit(false);
                
                // Insert into LabResult table
                String labSql = "INSERT INTO LabResult (LabResultID, PatientID, ResultName_English, Unit) VALUES (?, ?, ?, ?)";
                labPstmt = conn.prepareStatement(labSql);
                labPstmt.setString(1, labResultId);
                labPstmt.setString(2, patientId);
                labPstmt.setString(3, testName);
                labPstmt.setString(4, unit.isEmpty() ? null : unit);
                labPstmt.executeUpdate();
                
                // Insert into Measurement table
                String measureSql = "INSERT INTO Measurement (LabResultID, DateTime, Value) VALUES (?, ?, ?)";
                measurePstmt = conn.prepareStatement(measureSql);
                measurePstmt.setString(1, labResultId);
                measurePstmt.setString(2, testDate);
                measurePstmt.setString(3, value);
                measurePstmt.executeUpdate();
                
                conn.commit();
                System.out.println("Lab result added successfully");
                return true;
                
            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        System.err.println("Error rolling back transaction: " + ex.getMessage());
                    }
                }
                
                if (e.getMessage().contains("database is locked")) {
                    retryCount++;
                    if (retryCount < maxRetries) {
                        System.out.println("Database is locked, retrying in 1 second... (Attempt " + retryCount + " of " + maxRetries + ")");
                        try {
                            Thread.sleep(retryDelayMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        System.err.println("Failed to add lab result after " + maxRetries + " attempts: Database is locked");
                    }
                } else {
                    System.err.println("Error adding lab result: " + e.getMessage());
                    return false;
                }
            } finally {
                try {
                    if (measurePstmt != null) measurePstmt.close();
                    if (labPstmt != null) labPstmt.close();
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Error closing database resources: " + e.getMessage());
                }
            }
        }
        return false;
    }
} 