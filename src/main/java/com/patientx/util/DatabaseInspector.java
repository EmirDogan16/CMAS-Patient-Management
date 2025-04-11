package com.patientx.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.patientx.dao.DatabaseManager;

public class DatabaseInspector {
    private static DatabaseManager dbManager = DatabaseManager.getInstance();

    public static void main(String[] args) {
        try (Connection conn = dbManager.getConnection()) {
            System.out.println("\n=== LabResult Table Structure ===");
            showTableStructure(conn, "LabResult");
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void showTableStructure(Connection conn, String tableName) {
        String sql = "SELECT * FROM " + tableName + " LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            System.out.println("Column names in " + tableName + " table:");
            for (int i = 1; i <= columnCount; i++) {
                System.out.println(i + ". " + metaData.getColumnName(i));
            }
        } catch (SQLException e) {
            System.out.println("Error reading " + tableName + " structure: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 