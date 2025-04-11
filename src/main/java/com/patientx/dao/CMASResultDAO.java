package com.patientx.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.patientx.model.CMASResult;

// Using CMASResultDAO temporarily for table analysis
public class CMASResultDAO {
    private static CMASResultDAO instance;
    private DatabaseManager dbManager;
    private PatientDAO patientDAO; // For accessing PatientDAO
    private Connection conn;

    private CMASResultDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.patientDAO = new PatientDAO(); // Create PatientDAO instance
        this.conn = DatabaseConnection.getConnection();
        createTable(); 
    }

    public static CMASResultDAO getInstance() {
        if (instance == null) {
            instance = new CMASResultDAO();
        }
        return instance;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS CMASResults (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "patientId TEXT NOT NULL," +
                "testDate TEXT NOT NULL," +
                "score INTEGER NOT NULL," +
                "FOREIGN KEY (patientId) REFERENCES Patients(id)" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            // Create table with correct and precise schema
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // addCMASResult - Final Version with Separated Transactions
    public boolean addCMASResult(String patientId, String testDate, int score, int scoreType) {
        String sql = "INSERT INTO CMAS (PatientID, TestDate, Score, ScoreType) VALUES (?, ?, ?, ?)";
        boolean cmasAdded = false; // Flag for final result

        // --- Step 1: Ensure Patient Exists in 'Patient' Table (Separate Operation) ---
        boolean patientReady = false;
        try {
            // This method now manages its own connection and retries
            patientReady = patientDAO.ensurePatientExistsInPatientTable(patientId); 
        } catch (Exception e) { 
            // Catch broader exceptions just in case ensure method throws RuntimeException
             System.err.println("Critical error while ensuring patient exists: " + e.getMessage());
             e.printStackTrace();
             // Cannot proceed if patient check fails critically
             System.out.println("Failed to add CMAS result due to patient check error.");
             return false;
        }
        
        if (!patientReady) {
            // If patient couldn't be found or copied
            System.err.println("CMAS result cannot be saved because patient could not be ensured in 'Patient' table.");
             System.out.println("Failed to add CMAS result. Please check logs for errors."); // Final user message
            return false;
        }
        
        // --- Step 2: Add CMAS Result (Separate Transaction) ---
        System.out.println("Patient ready in 'Patient' table. Proceeding to add CMAS result...");
        
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false); // Start transaction for CMAS insert
            
            try {
                 // Determine the descriptive ScoreType string
                 String scoreTypeDescription;
                 if (scoreType == 1) { scoreTypeDescription = "Score > 10"; }
                 else if (scoreType == 2) { scoreTypeDescription = "Score 4-9"; }
                 else { scoreTypeDescription = "Unknown"; 
                      System.err.println("Warning: Invalid integer scoreType: " + scoreType);
                 }

                // Insert the CMAS result with the descriptive string
                System.out.println("Inserting CMAS values (ID Auto-Assigned):");
                System.out.println("  PatientID: " + patientId);
                System.out.println("  TestDate: " + testDate);
                System.out.println("  Score: " + score);
                System.out.println("  ScoreType (as text): " + scoreTypeDescription);
            
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, patientId);
                    pstmt.setString(2, testDate);
                    pstmt.setInt(3, score);
                    pstmt.setString(4, scoreTypeDescription); 
                    
                    int affected = pstmt.executeUpdate();
                    
                    if (affected > 0) {
                        conn.commit(); 
                        cmasAdded = true;
                    } else {
                         System.err.println("CMAS result insertion affected 0 rows.");
                         conn.rollback(); 
                    }
                } 

            } catch (SQLException e) {
                 System.err.println("SQL error during CMAS insert: " + e.getMessage());
                 e.printStackTrace();
                 try { conn.rollback(); } catch (SQLException rbEx) { 
                     System.err.println("Error during CMAS insert rollback: " + rbEx.getMessage());
                 } 
            }
        } catch (SQLException e) {
            System.err.println("Database connection error for CMAS insert: " + e.getMessage());
            e.printStackTrace();
        }

        // Final status message
        if (cmasAdded) {
            System.out.println("CMAS result added successfully.");
        } else {
            System.out.println("Failed to add CMAS result. Please check logs for errors.");
        }
        return cmasAdded; 
    }

    // getPatientCMASResults - Handles both old string and new numeric ScoreType
    public List<CMASResult> getPatientCMASResults(String patientId) {
        List<CMASResult> results = new ArrayList<>();
        String sql = "SELECT ID, PatientID, TestDate, Score, ScoreType FROM CMAS WHERE PatientID = ? ORDER BY TestDate DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                 try {
                    long id = rs.getLong("ID");
                    int score = rs.getInt("Score");
                    String scoreTypeStr = rs.getString("ScoreType");
                    int scoreType = 0; // Default to unknown

                    if (scoreTypeStr != null) {
                        try {
                            // Try parsing as integer first (new format "1", "2")
                            scoreType = Integer.parseInt(scoreTypeStr);
                        } catch (NumberFormatException nfe) {
                            // If parsing fails, check for old string formats
                            if (scoreTypeStr.equalsIgnoreCase("Score > 10")) {
                                scoreType = 1; // High
                            } else if (scoreTypeStr.equalsIgnoreCase("Score 4-9")) {
                                scoreType = 2; // Medium
                            } else {
                                // Log if it's an unrecognized string format
                                System.err.println("Unrecognized ScoreType string format found: '" + scoreTypeStr + "' for ID: " + id);
                            }
                        }
                    } else {
                         System.err.println("Null ScoreType found for ID: " + id);
                    }
                    
                    CMASResult result = new CMASResult(id, rs.getString("PatientID"), rs.getString("TestDate"), score, scoreType);
                    results.add(result);
                 } catch (SQLException ex) { // Catch only SQLException here
                     System.err.println("Error reading CMAS result row: " + ex.getMessage());
                 }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient CMAS results: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    // getLatestCMASResults - Handles both old string and new numeric ScoreType
    public List<CMASResult> getLatestCMASResults(int limit) {
        List<CMASResult> results = new ArrayList<>();
        String sql = "SELECT ID, PatientID, TestDate, Score, ScoreType FROM CMAS ORDER BY TestDate DESC LIMIT ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                 try {
                    long id = rs.getLong("ID");
                    int score = rs.getInt("Score");
                    String scoreTypeStr = rs.getString("ScoreType");
                    int scoreType = 0; // Default to unknown

                    if (scoreTypeStr != null) {
                        try {
                            // Try parsing as integer first
                            scoreType = Integer.parseInt(scoreTypeStr);
                        } catch (NumberFormatException nfe) {
                            // If parsing fails, check for old string formats
                            if (scoreTypeStr.equalsIgnoreCase("Score > 10")) {
                                scoreType = 1; // High
                            } else if (scoreTypeStr.equalsIgnoreCase("Score 4-9")) {
                                scoreType = 2; // Medium
                            } else {
                                System.err.println("Unrecognized ScoreType string format found: '" + scoreTypeStr + "' for ID: " + id);
                            }
                        }
                    } else {
                         System.err.println("Null ScoreType found for ID: " + id);
                    }
                    
                    CMASResult result = new CMASResult(id, rs.getString("PatientID"), rs.getString("TestDate"), score, scoreType);
                    results.add(result);
                 } catch (SQLException ex) { // Catch only SQLException here
                     System.err.println("Error reading latest CMAS result row: " + ex.getMessage());
                 }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching latest CMAS results: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    // getAverageScore (English)
    public double getAverageScore(String patientId) {
        String sql = "SELECT AVG(Score) as avg_score FROM CMAS WHERE PatientID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) { return rs.getDouble("avg_score"); }
        } catch (SQLException e) {
             // Keep critical errors in English
            System.err.println("Error calculating average CMAS score: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0; 
    }

    // getCMASResults
    public List<CMASResult> getCMASResults(String patientId) {
        return getPatientCMASResults(patientId);
    }
} 