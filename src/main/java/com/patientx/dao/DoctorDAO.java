package com.patientx.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DoctorDAO {
    private DatabaseManager dbManager;

    public DoctorDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void createDoctorsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Doctors ("
                + "DoctorID TEXT PRIMARY KEY,"
                + "Username TEXT UNIQUE NOT NULL,"
                + "Password TEXT NOT NULL"
                + ")";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creating Doctors table: " + e.getMessage());
        }
    }

    public boolean registerDoctor(String doctorId, String username, String password) {
        String sql = "INSERT INTO Doctors (DoctorID, Username, Password) VALUES (?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
            // If executeUpdate completes without exception, registration is successful
            return true; 
        } catch (SQLException e) {
            // Check if the error is due to a unique constraint violation (specific to SQLite)
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed: Doctors.Username")) {
                System.err.println("Registration failed: Username already exists.");
            } else {
                // Log other SQL errors
                System.err.println("Error registering doctor: " + e.getMessage());
            }
            return false; // Return false if any exception occurs
        }
    }

    public boolean login(String username, String password) {
        String sql = "SELECT * FROM Doctors WHERE Username = ? AND Password = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true if credentials are correct
        } catch (SQLException e) {
            System.out.println("Error during doctor login: " + e.getMessage());
            return false;
        }
    }

    public String getDoctorId(String username) {
        String sql = "SELECT DoctorID FROM Doctors WHERE Username = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("DoctorID");
            }
        } catch (SQLException e) {
            System.out.println("Error getting doctor ID: " + e.getMessage());
        }
        return null;
    }
} 