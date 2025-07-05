package com.kerem.ordersystem.carrentalsystem.database;

import com.kerem.ordersystem.carrentalsystem.model.CarCategory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Car Categories
 */
public class CarCategoryDAO {
    
    /**
     * Get all car categories as CarCategory objects
     */
    public static List<CarCategory> getAllCategoriesAsObjects() {
        List<CarCategory> categories = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT CategoryID, CategoryName, Description FROM CarCategories ORDER BY CategoryName";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    CarCategory category = new CarCategory(
                        rs.getInt("CategoryID"),
                        rs.getString("CategoryName"),
                        rs.getString("Description")
                    );
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting car categories: " + e.getMessage());
            // Add default categories if database query fails
            categories.add(new CarCategory(1, "Economy", "Budget-friendly cars"));
            categories.add(new CarCategory(2, "Compact", "Small and efficient cars"));
            categories.add(new CarCategory(3, "SUV", "Sport Utility Vehicles"));
            categories.add(new CarCategory(4, "Luxury", "Premium vehicles"));
        }
        
        return categories;
    }
    
    /**
     * Get all car categories as strings (for backwards compatibility)
     */
    public static List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT DISTINCT CategoryName FROM CarCategories ORDER BY CategoryName";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    categories.add(rs.getString("CategoryName"));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting car categories: " + e.getMessage());
            // Add default categories if database query fails
            categories.add("Economy");
            categories.add("Compact");
            categories.add("SUV");
            categories.add("Luxury");
        }
        
        return categories;
    }
    
    /**
     * Get category by ID
     */
    public static String getCategoryById(int categoryId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT CategoryName FROM CarCategories WHERE CategoryID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, categoryId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("CategoryName");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting category by ID: " + e.getMessage());
        }
        
        return "Unknown";
    }
    
    /**
     * Get category ID by name
     */
    public static int getCategoryIdByName(String categoryName) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT CategoryID FROM CarCategories WHERE CategoryName = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, categoryName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("CategoryID");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting category ID by name: " + e.getMessage());
        }
        
        return 1; // Default category ID
    }
    
    /**
     * Add a new category
     */
    public static boolean addCategory(String categoryName, String description) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "INSERT INTO CarCategories (CategoryName, Description) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, categoryName);
                stmt.setString(2, description);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding category: " + e.getMessage());
            return false;
        }
    }
} 