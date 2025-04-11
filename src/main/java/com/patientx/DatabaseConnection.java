package com.patientx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_FILENAME = "PatientXdatabase.db";
    private static final String DB_BACKUP = "PatientXdatabase.backup.db";
    private static String dbPath;
    
    static {
        // Uygulama başladığında çalışacak kod
        try {
            // Veritabanı dosyasının yolunu belirle
            dbPath = new File(".").getCanonicalPath() + File.separator + DB_FILENAME;
            
            // Yedek dosya yolu
            String backupPath = new File(".").getCanonicalPath() + File.separator + DB_BACKUP;
            
            // Eğer veritabanı dosyası yoksa ve yedek varsa, yedeği geri yükle
            if (!new File(dbPath).exists() && new File(backupPath).exists()) {
                Files.copy(Paths.get(backupPath), Paths.get(dbPath), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Veritabanı yedeği geri yüklendi.");
            }
            
            // Veritabanı bağlantısını test et
            testConnection();
            
            // Bağlantı başarılıysa ve yedek yoksa, yedek al
            if (!new File(backupPath).exists()) {
                Files.copy(Paths.get(dbPath), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Veritabanı yedeği oluşturuldu.");
            }
        } catch (IOException e) {
            System.err.println("Veritabanı dosya işlemleri hatası: " + e.getMessage());
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
    
    public static void testConnection() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {
            
            stmt.setQueryTimeout(30);
            
            // Test query to get table names
            ResultSet rs = connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
            System.out.println("Veritabanındaki tablolar:");
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                System.out.println("- " + tableName);
            }
            
        } catch (SQLException e) {
            System.err.println("Veritabanı bağlantı hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testConnection();
    }
} 