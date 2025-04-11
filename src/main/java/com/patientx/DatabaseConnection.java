package com.patientx;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_FILENAME = "PatientXdatabase.db";
    private static String dbPath;
    
    static {
        try {
            // Önce mevcut dizinde veritabanını ara
            File currentDirDb = new File(DB_FILENAME);
            if (currentDirDb.exists()) {
                // Eğer mevcut dizinde varsa, onu kullan
                dbPath = currentDirDb.getAbsolutePath();
                System.out.println("Mevcut veritabanı kullanılıyor: " + dbPath);
            } else {
                // Mevcut dizinde yoksa, jar dosyasının olduğu dizine bak
                String jarPath = DatabaseConnection.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
                File jarDir = new File(jarPath).getParentFile();
                File dbFile = new File(jarDir, DB_FILENAME);
                
                if (dbFile.exists()) {
                    // Jar dizininde varsa, onu kullan
                    dbPath = dbFile.getAbsolutePath();
                    System.out.println("Jar dizinindeki veritabanı kullanılıyor: " + dbPath);
                } else {
                    // Hiçbir yerde yoksa, mevcut dizinde oluştur
                    dbPath = currentDirDb.getAbsolutePath();
                    System.out.println("Yeni veritabanı oluşturuluyor: " + dbPath);
                }
            }
            
            // Test bağlantısı
            testConnection();
            
        } catch (Exception e) {
            System.err.println("Veritabanı başlatma hatası: " + e.getMessage());
            // Hata durumunda varsayılan olarak mevcut dizini kullan
            dbPath = new File(DB_FILENAME).getAbsolutePath();
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