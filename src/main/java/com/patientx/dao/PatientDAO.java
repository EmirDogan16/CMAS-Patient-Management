package com.patientx.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.patientx.model.Patient;

public class PatientDAO {
    private final DatabaseManager databaseManager;

    public PatientDAO() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public void createPatientsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Patients (" +
                    "PatientID TEXT PRIMARY KEY," +
                    "Name TEXT," +
                    "Age INTEGER," +
                    "Gender TEXT)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
            // syncPatientsFromLabResults(); // Temporarily disabled to test initial lock
        } catch (SQLException e) {
            System.err.println("Error creating Patients table: " + e.getMessage());
        }
    }

    public void syncPatientsFromLabResults() {
        String selectSql = "SELECT DISTINCT PatientID FROM LabResult";
        String insertSql = "INSERT OR IGNORE INTO Patients (PatientID) VALUES (?)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            
            ResultSet rs = selectStmt.executeQuery();
            int count = 0;
            
            while (rs.next()) {
                String patientId = rs.getString("PatientID");
                insertStmt.setString(1, patientId);
                insertStmt.executeUpdate();
                count++;
            }
            
        } catch (SQLException e) {
            System.err.println("Error syncing patients from LabResult: " + e.getMessage());
        }
    }

    public List<String> getAllPatientIds() {
        List<String> patientIds = new ArrayList<>();
        String sql = "SELECT PatientID FROM Patient " +
                    "UNION " +
                    "SELECT PatientID FROM Patients ORDER BY PatientID"; 
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                patientIds.add(rs.getString("PatientID"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all patient IDs: " + e.getMessage());
        }
        return patientIds;
    }

    public Patient getPatientById(String patientId) {
        String sqlPatient = "SELECT PatientID, Name FROM Patient WHERE PatientID = ?";
        String sqlPatients = "SELECT PatientID, Name FROM Patients WHERE PatientID = ?";
        
        try (Connection conn = databaseManager.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sqlPatient)) {
                pstmt.setString(1, patientId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new Patient(rs.getString("PatientID"), rs.getString("Name"));
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sqlPatients)) {
                pstmt.setString(1, patientId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new Patient(rs.getString("PatientID"), rs.getString("Name"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting patient by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT PatientID, Name FROM Patient " +
                    "UNION " +
                    "SELECT PatientID, Name FROM Patients ORDER BY Name";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Patient patient = new Patient(rs.getString("PatientID"), rs.getString("Name")); 
                patients.add(patient);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all patients: " + e.getMessage());
            e.printStackTrace();
        }
        return patients;
    }

    public boolean addPatient(String name) {
        String patientId = java.util.UUID.randomUUID().toString();
        String sql = "INSERT INTO Patients (PatientID, Name) VALUES (?, ?)";
        boolean success = false;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patientId);
            pstmt.setString(2, name);
            int affected = pstmt.executeUpdate();
            
            if (affected > 0) {
                System.out.println("Patient added successfully to Patients table with ID: " + patientId);
                success = true;
            } else {
                 System.err.println("Patient insertion (Patients table) affected 0 rows.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding patient to Patients table: " + e.getMessage());
        }
        return success;
    }

    public List<Patient> searchPatients(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT PatientID, Name FROM Patient WHERE Name LIKE ? OR PatientID LIKE ? " +
                    "UNION " +
                    "SELECT PatientID, Name FROM Patients WHERE Name LIKE ? OR PatientID LIKE ? " +
                    "ORDER BY Name";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Patient patient = new Patient(rs.getString("PatientID"), rs.getString("Name"));
                    patients.add(patient);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching patients: " + e.getMessage());
        }
        return patients;
    }

    // Helper method to add patient to 'Patient' table with Retry for BUSY
    // Uses its own connection within the retry loop
    private boolean addPatientToPatientTableWithRetry(String patientId, String name) {
        String sql = "INSERT OR IGNORE INTO Patient (PatientID, Name) VALUES (?, ?)";
        int maxRetries = 3;
        int retryDelayMs = 500; 

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            // Get a fresh connection for each attempt
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, patientId);
                pstmt.setString(2, name);
                pstmt.executeUpdate();
                // Success (inserted or ignored)
                return true; 
            } catch (SQLException e) {
                if (attempt < maxRetries && e.getMessage() != null && e.getMessage().contains("[SQLITE_BUSY]")) {
                    System.err.printf("Database locked inserting into Patient table. Retrying in %d ms... (Attempt %d/%d)\n", retryDelayMs, attempt, maxRetries);
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); 
                        System.err.println("Retry sleep interrupted.");
                        // Re-throw as an unchecked exception or handle differently if needed
                        throw new RuntimeException("Retry interrupted", e); 
                    }
                } else {
                    // Final attempt failed or different error
                    System.err.println("Failed to insert into Patient table after %d attempts: %s" + attempt + e.getMessage());
                     // No re-throw needed here, just return false after loop
                     break; // Exit loop on non-BUSY error or final attempt
                }
            }
        }
        return false; // Failed after all retries
    }

    // Helper method to get patient from 'Patients' table (Uses its own connection)
    private Patient getPatientFromPatientsTable(String patientId) {
        String sql = "SELECT PatientID, Name FROM Patients WHERE PatientID = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("Name");
                return new Patient(rs.getString("PatientID"), name != null ? name : "Unknown"); 
            }
        } catch (SQLException e) {
             System.err.println("Error fetching patient from Patients table: " + e.getMessage());
        }
        return null;
    }
    
    // Helper to check existence in Patient table (Uses its own connection)
    private boolean checkPatientExistsInPatientTable(String patientId) {
         String checkPatientSql = "SELECT 1 FROM Patient WHERE PatientID = ?";
         try(Connection conn = databaseManager.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkPatientSql)) {
             checkStmt.setString(1, patientId);
             ResultSet rs = checkStmt.executeQuery();
             return rs.next();
         } catch (SQLException e) {
              System.err.println("Error checking patient in Patient table: " + e.getMessage());
              return false; // Assume not exists on error
         }
    }

    // Public method called by CMASResultDAO - Manages its own connections
    // Ensures the patient exists in the 'Patient' table
    public boolean ensurePatientExistsInPatientTable(String patientId) {
        // Check if already exists in 'Patient' table
        if (checkPatientExistsInPatientTable(patientId)) {
            return true; // Already exists, we're good
        }

        // If not, get info from 'Patients' table
        Patient patientFromPatients = getPatientFromPatientsTable(patientId);
        
        if (patientFromPatients != null) {
            // If found in 'Patients', try to add/ignore into 'Patient' table with retry
            return addPatientToPatientTableWithRetry(patientFromPatients.getPatientId(), patientFromPatients.getName());
        } else {
            // Not found in either table
            System.err.println("ensurePatientExistsInPatientTable: Patient not found in 'Patients' table either: " + patientId);
            return false;
        }
    }

    // validatePatient method added back
    public boolean validatePatient(String patientId) {
        // Check in both Patient and Patients tables
        String sql = "SELECT 1 FROM Patient WHERE PatientID = ? " +
                    "UNION " +
                    "SELECT 1 FROM Patients WHERE PatientID = ?";
        
        // Bu metod kendi bağlantısını yönetmeli
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patientId);
            pstmt.setString(2, patientId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true if patient exists in any table
            
        } catch (SQLException e) {
            System.err.println("Error validating patient existence: " + e.getMessage());
            return false; // Return false in case of error
        }
    }

    // New method: Check if the given name exists
    public boolean checkPatientNameExists(String name) {
        // Check in both Patient and Patients tables
        String sql = "SELECT 1 FROM Patient WHERE Name = ? COLLATE NOCASE " +
                    "UNION " +
                    "SELECT 1 FROM Patients WHERE Name = ? COLLATE NOCASE LIMIT 1";
        
        // Uses its own connection
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, name);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true if name exists in either table (case-insensitive)
            
        } catch (SQLException e) {
            System.err.println("Error checking for existing patient name: " + e.getMessage());
            // Return false on error to allow proceeding, but log the issue
            return false; 
        }
    }

    // This method should manage its own connection
    private boolean isNameExists(String name) {
        // Implementation of the method
        return false; // Placeholder return, actual implementation needed
    }
} 