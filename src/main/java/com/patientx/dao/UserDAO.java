package com.patientx.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {
    private final DatabaseManager dbManager;

    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Users ("
                + "UserID TEXT PRIMARY KEY,"
                + "Username TEXT NOT NULL UNIQUE,"
                + "Password TEXT NOT NULL,"
                + "Role TEXT NOT NULL"
                + ")";

        try (Statement stmt = dbManager.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating Users table: " + e.getMessage());
        }
    }

    public boolean registerUser(String userId, String username, String password, String role) {
        String sql = "INSERT INTO Users (UserID, Username, Password, Role) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, role);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public boolean login(String username, String password, String role) {
        String sql = "SELECT * FROM Users WHERE Username = ? AND Password = ? AND Role = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // true if user exists with given credentials
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
            return false;
        }
    }

    public String getUserId(String username) {
        String sql = "SELECT UserID FROM Users WHERE Username = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("UserID");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
        }
        return null;
    }
} 