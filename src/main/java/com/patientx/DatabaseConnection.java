package com.patientx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private static String dbPath;
    
    static {
        try {
            // Uygulama klasörünü bul
            String appDir = new File(System.getProperty("user.dir")).getAbsolutePath();
            dbPath = Paths.get(appDir, DB_FILENAME).toString();
            
            // Eğer veritabanı dosyası yoksa, resources'dan kopyala
            if (!Files.exists(Paths.get(dbPath))) {
                // Resources'dan veritabanını kopyala
                try (InputStream is = DatabaseConnection.class.getResourceAsStream("/PatientXdatabase.db")) {
                    if (is != null) {
                        Files.copy(is, Paths.get(dbPath), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Veritabanı resources'dan kopyalandı.");
                    } else {
                        System.err.println("Veritabanı resources'da bulunamadı!");
                    }
                }
            }
            
            // Veritabanı bağlantısını test et
            testConnection();
            
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