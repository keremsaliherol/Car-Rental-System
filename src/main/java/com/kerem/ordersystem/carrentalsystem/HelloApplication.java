package com.kerem.ordersystem.carrentalsystem;

import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database and create default users
        initializeDatabase();
        
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("Car Rental System");
        stage.setScene(scene);
        stage.show();
    }

    private void initializeDatabase() {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            // Test connection
            if (!dbManager.testConnection()) {
                System.err.println("❌ Database connection failed!");
                return;
            }
            
            System.out.println("✅ Database connection successful");
            
            // Create default admin user if not exists
            createDefaultAdminUser();
            
            // Fix car statuses that might be stuck
            fixStuckCarStatuses();
            
        } catch (Exception e) {
            System.err.println("❌ Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createDefaultAdminUser() {
        String checkUserSQL = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        String insertUserSQL = "INSERT INTO Users (Username, PasswordHash, RoleID, IsActive) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Check if admin user exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSQL)) {
                checkStmt.setString(1, "admin");
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next() && rs.getInt(1) == 0) {
                    // Admin user doesn't exist, create it
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSQL)) {
                        insertStmt.setString(1, "admin");
                        insertStmt.setString(2, "admin123"); // Plain password for admin
                        insertStmt.setInt(3, 1); // Role ID 1 = Admin
                        insertStmt.setBoolean(4, true);
                        
                        int result = insertStmt.executeUpdate();
                        if (result > 0) {
                            System.out.println("✅ Default admin user created: admin/admin123");
                        }
                    }
                } else {
                    System.out.println("ℹ️ Admin user already exists");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error creating default admin user: " + e.getMessage());
        }
    }
    
    private void fixStuckCarStatuses() {
        System.out.println("🔧 Checking for cars with incorrect status...");
        
        String findStuckCarsSQL = """
            SELECT c.CarID, c.PlateNumber, c.Brand, c.Model, c.StatusID
            FROM Cars c
            WHERE c.StatusID = 2 
            AND NOT EXISTS (
                SELECT 1 FROM Rentals r 
                WHERE r.CarID = c.CarID 
                AND ISNULL(r.Status, 'Active') = 'Active'
            )
            """;
            
        String updateCarStatusSQL = "UPDATE Cars SET StatusID = 1 WHERE CarID = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            try (PreparedStatement findStmt = conn.prepareStatement(findStuckCarsSQL);
                 ResultSet rs = findStmt.executeQuery()) {
                
                int fixedCount = 0;
                while (rs.next()) {
                    int carId = rs.getInt("CarID");
                    String plateNumber = rs.getString("PlateNumber");
                    String brand = rs.getString("Brand");
                    String model = rs.getString("Model");
                    
                    // Update car status to Available (StatusID = 1)
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateCarStatusSQL)) {
                        updateStmt.setInt(1, carId);
                        int updated = updateStmt.executeUpdate();
                        
                        if (updated > 0) {
                            System.out.println("✅ Fixed status for: " + brand + " " + model + " (" + plateNumber + ")");
                            fixedCount++;
                        }
                    }
                }
                
                if (fixedCount > 0) {
                    System.out.println("🔧 Fixed " + fixedCount + " car(s) with incorrect status");
                } else {
                    System.out.println("ℹ️ No cars with incorrect status found");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error fixing car statuses: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
