package com.kerem.ordersystem.carrentalsystem.database;

import com.kerem.ordersystem.carrentalsystem.model.Activity;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO {

    // Create Activities table if not exists
    public static void createTableIfNotExists() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check if table exists
            String checkTableSQL = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES 
                WHERE TABLE_NAME = 'Activities'
            """;
            
            try (ResultSet rs = stmt.executeQuery(checkTableSQL)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Table doesn't exist, create it
                    String createTableSQL = """
                        CREATE TABLE Activities (
                            ActivityID INT IDENTITY(1,1) PRIMARY KEY,
                            ActivityType NVARCHAR(50) NOT NULL,
                            Description NVARCHAR(255) NOT NULL,
                            Icon NVARCHAR(10),
                            Details NVARCHAR(500),
                            CreatedAt DATETIME2 DEFAULT GETDATE()
                        )
                    """;
                    stmt.execute(createTableSQL);
                    System.out.println("Activities table created successfully.");
                } else {
                    System.out.println("Activities table already exists.");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("ActivityDAO.createTableIfNotExists - Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Add new activity
    public static boolean addActivity(String activityType, String description, String icon, String details) {
        String sql = "INSERT INTO Activities (ActivityType, Description, Icon, Details) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, activityType);
            stmt.setString(2, description);
            stmt.setString(3, icon);
            stmt.setString(4, details);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("ActivityDAO.addActivity - Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get recent activities (last 10)
    public static List<Activity> getRecentActivities() {
        return getRecentActivities(10);
    }

    // Get recent activities with limit
    public static List<Activity> getRecentActivities(int limit) {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT TOP (?) * FROM Activities ORDER BY CreatedAt DESC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Activity activity = new Activity();
                    activity.setActivityId(rs.getInt("ActivityID"));
                    activity.setActivityType(rs.getString("ActivityType"));
                    activity.setDescription(rs.getString("Description"));
                    activity.setIcon(rs.getString("Icon"));
                    activity.setDetails(rs.getString("Details"));
                    
                    Timestamp timestamp = rs.getTimestamp("CreatedAt");
                    if (timestamp != null) {
                        activity.setCreatedAt(timestamp.toLocalDateTime());
                    }
                    
                    activities.add(activity);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("ActivityDAO.getRecentActivities - Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return activities;
    }

    // Helper methods for common activities
    public static boolean logCarAdded(String plateNumber, String brand, String model) {
        return addActivity(
            "CAR_ADDED",
            "New car added",
            "🚙",
            String.format("%s %s (%s) added to system", brand, model, plateNumber)
        );
    }

    public static boolean logCarRented(String customerName, String plateNumber, String brand, String model) {
        return addActivity(
            "CAR_RENTED",
            "Car rented",
            "🔑",
            String.format("%s rented %s %s (%s)", customerName, brand, model, plateNumber)
        );
    }

    public static boolean logCarReturned(String customerName, String plateNumber, String brand, String model) {
        return addActivity(
            "CAR_RETURNED",
            "Car returned",
            "✅",
            String.format("%s returned %s %s (%s)", customerName, brand, model, plateNumber)
        );
    }

    public static boolean logCustomerAdded(String customerName) {
        return addActivity(
            "CUSTOMER_ADDED",
            "New customer registered",
            "👤",
            String.format("%s registered to system", customerName)
        );
    }

    public static boolean logPaymentReceived(double amount, String customerName) {
        return addActivity(
            "PAYMENT_RECEIVED",
            "Payment received",
            "💰",
            String.format("₺%.2f payment received (%s)", amount, customerName)
        );
    }

}