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
             // Uygulamanın devam etmesi isteniyorsa bu hata kritik olabilir.
             // throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    // Constructor private
    private DatabaseManager() {
        // Constructor boş bırakıldı, ilk kurulum getInstance içinde çağrılabilir
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                    // İlk veritabanı kurulum/güncelleme işlemini burada yap
                    instance.performInitialSetup(); 
                }
            }
        }
        return instance;
    }
    
    // Bağlantı ayarlarını yapan yardımcı metod
    private void setupConnection(Connection conn) throws SQLException {
         try (Statement stmt = conn.createStatement()) {
            // Önce timeout ayarla, sonra diğer pragma'lar
            stmt.execute("PRAGMA busy_timeout = 5000;"); // 5 saniye bekleme süresi
            stmt.execute("PRAGMA foreign_keys = ON;");
            // System.out.println("Database connection configured: Foreign Keys ON, Busy Timeout 5000ms"); // Logu kaldırabiliriz
        } catch (SQLException e) {
            System.err.println("Failed to configure database connection settings: " + e.getMessage());
            throw e; 
        }
    }

    // Her seferinde yeni, ayarlanmış bir bağlantı döndüren metod
    public Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            setupConnection(conn); // Yeni bağlantıyı her zaman ayarla
            return conn;
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
            // conn null ise veya hata oluştuysa, hata fırlatılır
            throw e; 
        }
    }

    // İlk kurulum ve potansiyel şema güncellemeleri için metod
    // Bu metodun transaction yönetimi kendi içinde yapılmalı
    public void performInitialSetup() {
         String alterTableSQL = "ALTER TABLE Patients ADD COLUMN Name TEXT DEFAULT 'Patient1'";
         String updateNamesSQL = "WITH RECURSIVE cnt(x) AS (" +
                               "  SELECT 1" +
                               "  UNION ALL" +
                               "  SELECT x+1 FROM cnt LIMIT 1000" +
                               ")" +
                               "UPDATE Patients SET Name = 'Patient' || x " +
                               "FROM (SELECT PatientID, ROW_NUMBER() OVER (ORDER BY PatientID) as x FROM Patients WHERE Name IS NULL OR Name = 'Patient1') t " +
                               "WHERE Patients.PatientID = t.PatientID";

         try (Connection conn = DriverManager.getConnection(DB_URL); // Kurulum için ayrı bağlantı
              Statement stmt = conn.createStatement()) {
            
             // Bağlantı ayarlarını burada da yapalım (timeout önemli olabilir)
             try {
                 setupConnection(conn);
             } catch (SQLException setupEx) {
                  System.err.println("Warning: Failed to configure connection during initial setup: " + setupEx.getMessage());
             }

            // Add Name column if it doesn't exist
            try {
                stmt.execute(alterTableSQL);
                System.out.println("Initial Setup: Added Name column to Patients table (if needed).");
            } catch (SQLException e) {
                 if (!e.getMessage().contains("duplicate column name")) {
                     System.err.println("Initial Setup: Error adding Name column (ignored if duplicate): " + e.getMessage());
                 }
            }

            // Update patient names
            try {
                int updatedRows = stmt.executeUpdate(updateNamesSQL);
                 if (updatedRows > 0) {
                     System.out.println("Initial Setup: Updated " + updatedRows + " patient names (where needed).");
                 } else {
                     // System.out.println("Initial Setup: No patient names needed updating."); // İsteğe bağlı log
                 }
            } catch (SQLException e) {
                 System.err.println("Initial Setup: Error updating patient names: " + e.getMessage());
                 // Bu hata kritikse, burada daha fazla işlem yapılabilir.
            }

        } catch (SQLException e) {
            System.err.println("Critical error during initial database setup: " + e.getMessage());
             // Uygulamanın devam etmesi tehlikeli olabilir
             // throw new RuntimeException("Critical error during initial database setup", e);
        }
    }
} 