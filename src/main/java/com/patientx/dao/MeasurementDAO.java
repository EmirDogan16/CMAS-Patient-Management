package com.patientx.dao;

import com.patientx.database.DatabaseManager;
import com.patientx.model.Measurement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MeasurementDAO {
    private final DatabaseManager dbManager;

    public MeasurementDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public List<Measurement> getMeasurementsForPatient(String patientId) {
        List<Measurement> measurements = new ArrayList<>();
        String sql = "SELECT m.*, l.TestName, l.Value, l.Unit " +
                    "FROM Measurement m " +
                    "LEFT JOIN LabResult l ON m.LabResultID = l.ID " +
                    "WHERE m.PatientID = ? " +
                    "ORDER BY m.Date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Measurement measurement = new Measurement(
                    rs.getInt("ID"),
                    rs.getString("PatientID"),
                    rs.getString("Date"),
                    rs.getInt("LabResultID"),
                    rs.getString("TestName"),
                    rs.getString("Value"),
                    rs.getString("Unit")
                );
                measurements.add(measurement);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving measurements: " + e.getMessage());
        }
        
        return measurements;
    }
} 