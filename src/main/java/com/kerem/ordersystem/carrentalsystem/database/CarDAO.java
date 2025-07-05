package com.kerem.ordersystem.carrentalsystem.database;

import com.kerem.ordersystem.carrentalsystem.model.Car;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced CarDAO with integrated database objects:
 * Uses Views, Stored Procedures, Functions, and Indexes
 */
public class CarDAO {
    
    private static final DatabaseService databaseService = new DatabaseService();

    // ========================================
    // VIEWS INTEGRATION - Car Related Views
    // ========================================
    
    /**
     * Get available cars using AvailableCarsView
     */
    public static List<Car> getAvailableCars() {
        List<Map<String, Object>> results = databaseService.getAvailableCars();
        List<Car> cars = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Car car = new Car();
            car.setCarId((Integer) row.get("CarID"));
            car.setPlateNumber((String) row.get("PlateNumber"));
            car.setBrand((String) row.get("Brand"));
            car.setModel((String) row.get("Model"));
            car.setDailyRate(((Number) row.get("DailyRate")).doubleValue());
            cars.add(car);
        }
        
        return cars;
    }
    
    /**
     * Get car usage statistics using CarUsageStats view
     */
    public static List<Map<String, Object>> getCarUsageStatistics() {
        return databaseService.getCarUsageStats();
    }
    
    /**
     * Get maintenance schedule using MaintenanceSchedule view
     */
    public static List<Map<String, Object>> getMaintenanceSchedule() {
        return databaseService.getMaintenanceSchedule();
    }

    // ========================================
    // STORED PROCEDURES INTEGRATION
    // ========================================
    
    /**
     * Update car status using direct SQL (fallback method)
     */
    public static boolean updateCarStatus(int carId, int statusId) {
        System.out.println("🔄 Updating car status - CarID: " + carId + ", StatusID: " + statusId);
        
        String sql = "UPDATE Cars SET StatusID = ? WHERE CarID = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, statusId);
            stmt.setInt(2, carId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Car status updated successfully - CarID: " + carId + ", StatusID: " + statusId);
        
                // Log the status name for clarity
                String statusName = getStatusName(statusId);
                System.out.println("   Status name: " + statusName);
                
                return true;
            } else {
                System.err.println("❌ No car found with CarID: " + carId);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating car status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update car status using existing connection (for transactions)
     */
    public static boolean updateCarStatus(Connection conn, int carId, int statusId) {
        System.out.println("🔄 Updating car status (with existing connection) - CarID: " + carId + ", StatusID: " + statusId);
        
        String sql = "UPDATE Cars SET StatusID = ? WHERE CarID = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, statusId);
            stmt.setInt(2, carId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Car status updated successfully - CarID: " + carId + ", StatusID: " + statusId);
        
                // Log the status name for clarity
                String statusName = getStatusName(statusId);
                System.out.println("   Status name: " + statusName);
                
                return true;
            } else {
                System.err.println("❌ No car found with CarID: " + carId);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating car status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Helper method to get status name by ID
     */
    private static String getStatusName(int statusId) {
        switch (statusId) {
            case 1: return "Available";
            case 2: return "Rented";
            case 3: return "Maintenance";
            case 4: return "Deleted";
            default: return "Unknown";
        }
    }
    
    /**
     * Add maintenance record using stored procedure
     */
    public static boolean addMaintenanceRecord(int carId, LocalDate startDate, LocalDate endDate, String description) {
        // Check car availability using function first
        boolean isAvailable = databaseService.getCarAvailability(carId);
        
        if (!isAvailable) {
            System.out.println("⚠️ Car is not available for maintenance");
            return false;
        }
        
        // Add maintenance using stored procedure
        return databaseService.addMaintenance(carId, startDate, endDate, description);
    }

    // ========================================
    // FUNCTIONS INTEGRATION
    // ========================================
    
    /**
     * Check if car is available using function
     */
    public static boolean isCarAvailable(int carId) {
        return databaseService.getCarAvailability(carId);
    }
    
    /**
     * Get car category name using function
     */
    public static String getCarCategoryName(int categoryId) {
        return databaseService.getCategoryName(categoryId);
    }
    
    /**
     * Get car status name using function
     */
    public static String getCarStatusName(int statusId) {
        return databaseService.getCarStatusName(statusId);
    }

    // ========================================
    // INDEX-OPTIMIZED QUERIES
    // ========================================
    
    /**
     * Find car by plate number (uses idx_Cars_PlateNumber index)
     */
    public static Car findCarByPlateNumber(String plateNumber) {
        Map<String, Object> result = databaseService.getCarByPlateNumber(plateNumber);
        
        if (result.isEmpty()) {
            return null;
        }
        
        Car car = new Car();
        car.setCarId((Integer) result.get("CarID"));
        car.setPlateNumber((String) result.get("PlateNumber"));
        car.setBrand((String) result.get("Brand"));
        car.setModel((String) result.get("Model"));
        car.setYear((Integer) result.get("Year"));
        car.setDailyRate(((Number) result.get("DailyRate")).doubleValue());
        
        return car;
    }
    
    /**
     * Search cars by brand and model (uses idx_Cars_Brand_Model index)
     */
    public static List<Car> searchCarsByBrandAndModel(String brand, String model) {
        List<Map<String, Object>> results = databaseService.getCarsByBrandAndModel(brand, model);
        List<Car> cars = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            Car car = new Car();
            car.setCarId((Integer) row.get("CarID"));
            car.setPlateNumber((String) row.get("PlateNumber"));
            car.setBrand((String) row.get("Brand"));
            car.setModel((String) row.get("Model"));
            car.setYear((Integer) row.get("Year"));
            car.setDailyRate(((Number) row.get("DailyRate")).doubleValue());
            cars.add(car);
        }
        
        return cars;
    }
    
    /**
     * Get maintenance records for a car (uses idx_Maintenance_CarID index)
     */
    public static List<Map<String, Object>> getCarMaintenanceHistory(int carId) {
        return databaseService.getMaintenanceRecordsByCarId(carId);
    }
    
    // ========================================
    // COMPREHENSIVE CAR OPERATIONS
    // ========================================
    
    /**
     * Insert a new car into the database
     */
    public static boolean insertCar(String plateNumber, String brand, String model, int year, 
                                  double dailyRate, int categoryId, int mileage, String description) {
        String sql = "INSERT INTO Cars (PlateNumber, Brand, Model, Year, DailyRate, CategoryID, StatusID, Mileage, Description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, plateNumber);
            stmt.setString(2, brand);
            stmt.setString(3, model);
            stmt.setInt(4, year);
            stmt.setDouble(5, dailyRate);
            stmt.setInt(6, categoryId);
            stmt.setInt(7, 1); // Default status: Available (StatusID = 1)
            stmt.setInt(8, mileage);
            stmt.setString(9, description);

            int result = stmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("✅ Car inserted successfully: " + brand + " " + model + " (" + plateNumber + ")");
                return true;
            } else {
                System.out.println("❌ Car insertion failed");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error inserting car: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Add new car with comprehensive validation and logging
     */
    public static boolean addCarWithValidation(String plateNumber, String brand, String model, 
                                             int year, double dailyRate, int categoryId, int statusId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // 1. Validate using functions
            String categoryName = databaseService.getCategoryName(categoryId);
            String statusName = databaseService.getCarStatusName(statusId);
            
            if (categoryName.isEmpty() || statusName.isEmpty()) {
                System.out.println("❌ Invalid category or status ID");
                return false;
            }
            
            // 2. Check if plate number already exists (uses unique index)
            Car existingCar = findCarByPlateNumber(plateNumber);
            if (existingCar != null) {
                System.out.println("❌ Car with plate number " + plateNumber + " already exists");
                return false;
            }
            
            // 3. Insert car (triggers will automatically fire)
            String sql = "INSERT INTO Cars (PlateNumber, Brand, Model, Year, DailyRate, CategoryID, StatusID) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, plateNumber);
                stmt.setString(2, brand);
                stmt.setString(3, model);
                stmt.setInt(4, year);
                stmt.setDouble(5, dailyRate);
                stmt.setInt(6, categoryId);
                stmt.setInt(7, statusId);
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    System.out.println("✅ Car added successfully");
                    System.out.println("   Plate: " + plateNumber);
                    System.out.println("   Brand/Model: " + brand + " " + model);
                    System.out.println("   Category: " + categoryName);
                    System.out.println("   Status: " + statusName);
                    return true;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error adding car: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get comprehensive car information
     */
    public static Map<String, Object> getCarDetails(int carId) {
        Map<String, Object> carDetails = new HashMap<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT * FROM Cars WHERE CarID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, carId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        carDetails.put("CarID", rs.getInt("CarID"));
                        carDetails.put("PlateNumber", rs.getString("PlateNumber"));
                        carDetails.put("Brand", rs.getString("Brand"));
                        carDetails.put("Model", rs.getString("Model"));
                        carDetails.put("Year", rs.getInt("Year"));
                        carDetails.put("DailyRate", rs.getDouble("DailyRate"));
                        
                        // Enrich with function results
                        int categoryId = rs.getInt("CategoryID");
                        int statusId = rs.getInt("StatusID");
                        
                        carDetails.put("CategoryName", databaseService.getCategoryName(categoryId));
                        carDetails.put("StatusName", databaseService.getCarStatusName(statusId));
                        carDetails.put("IsAvailable", databaseService.getCarAvailability(carId));
                        
                        // Get maintenance history
                        carDetails.put("MaintenanceHistory", getCarMaintenanceHistory(carId));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting car details: " + e.getMessage());
        }
        
        return carDetails;
    }
    
    /**
     * Delete car with protection (checks for active rentals only)
     * Uses soft delete approach to maintain referential integrity
     */
    public static boolean deleteCar(int carId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // 1. Check if there are any ACTIVE rentals for this car
            String checkActiveRentalsSql = "SELECT COUNT(*) FROM Rentals WHERE CarID = ? AND Status = 'Active'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkActiveRentalsSql)) {
                checkStmt.setInt(1, carId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.err.println("❌ Car cannot be deleted: This car has active rentals");
                        return false;
                    }
                }
            }
            
            // 2. Ensure "Deleted" status exists in CarStatuses table
            ensureDeletedStatusExists(conn);
            
            // 3. If no active rentals, use soft delete (mark as deleted with StatusID = 4)
            // This maintains referential integrity with existing rentals
            String softDeleteSql = "UPDATE Cars SET StatusID = 4 WHERE CarID = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(softDeleteSql)) {
                deleteStmt.setInt(1, carId);
                
                int result = deleteStmt.executeUpdate();
                if (result > 0) {
                    System.out.println("✅ Car successfully deleted (soft delete - marked as deleted)");
                    return true;
                } else {
                    System.err.println("❌ Car not found");
                    return false;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting car: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ensure "Deleted" status exists in CarStatuses table
     */
    private static void ensureDeletedStatusExists(Connection conn) throws SQLException {
        // Check if StatusID = 4 exists
        String checkSql = "SELECT COUNT(*) FROM CarStatuses WHERE StatusID = 4";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             ResultSet rs = checkStmt.executeQuery()) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                // StatusID = 4 doesn't exist, add it using IDENTITY_INSERT
                try (Statement stmt = conn.createStatement()) {
                    // Enable IDENTITY_INSERT to allow explicit StatusID insertion
                    stmt.execute("SET IDENTITY_INSERT CarStatuses ON");
                    
                    // Insert the specific StatusID = 4
                    stmt.execute("INSERT INTO CarStatuses (StatusID, StatusName) VALUES (4, 'Deleted')");
                    
                    // Disable IDENTITY_INSERT
                    stmt.execute("SET IDENTITY_INSERT CarStatuses OFF");
                    
                    System.out.println("✅ Added 'Deleted' status to CarStatuses table with StatusID = 4");
                }
            }
        }
    }
    
    // ========================================
    // LEGACY METHODS (Updated to use new system)
    // ========================================
    
    public static List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT c.CarID, c.PlateNumber, c.Brand, c.Model, c.Year, c.DailyRate, " +
                    "c.CategoryID, c.StatusID, c.Mileage, c.Description, " +
                    "cat.CategoryName, " +
                    "CASE " +
                    "   WHEN c.StatusID = 1 THEN 'Available' " +
                    "   WHEN c.StatusID = 2 THEN 'Rented' " +
                    "   WHEN c.StatusID = 3 THEN 'Maintenance' " +
                    "   WHEN c.StatusID = 4 THEN 'Deleted' " +
                    "   ELSE 'Unknown' " +
                    "END as StatusName " +
                    "FROM Cars c " +
                    "LEFT JOIN CarCategories cat ON c.CategoryID = cat.CategoryID " +
                    "WHERE c.StatusID != 4";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Car car = new Car();
                car.setCarId(rs.getInt("CarID"));
                car.setPlateNumber(rs.getString("PlateNumber"));
                car.setBrand(rs.getString("Brand"));
                car.setModel(rs.getString("Model"));
                car.setYear(rs.getInt("Year"));
                car.setDailyRate(rs.getDouble("DailyRate"));
                car.setCategoryId(rs.getInt("CategoryID"));
                car.setStatus(rs.getString("StatusName"));  // Bu önemli!
                car.setMileage(rs.getInt("Mileage"));
                car.setDescription(rs.getString("Description") != null ? rs.getString("Description") : "");
                car.setCategoryName(rs.getString("CategoryName") != null ? rs.getString("CategoryName") : "");
                
                // Araç resmi atama - brand'e göre resim map et
                String imagePath = getCarImagePath(car.getBrand());
                car.setImagePath(imagePath);
                
                cars.add(car);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting all cars: " + e.getMessage());
            e.printStackTrace();
        }
        
        return cars;
    }
    
    /**
     * Get appropriate image path for a car based on its brand
     */
    private static String getCarImagePath(String brand) {
        if (brand == null) {
            return "/images/indir.jpg"; // varsayılan resim
        }
        
        String brandLower = brand.toLowerCase();
        if (brandLower.contains("bmw")) {
            return "/images/bmw.jpg";
        } else if (brandLower.contains("ferrari")) {
            return "/images/ferrari.jpg";
        } else if (brandLower.contains("alfa")) {
            return "/images/alfa.jpg";
        } else if (brandLower.contains("renault") || brandLower.contains("e-tech")) {
            return "/images/etech.jpg";
        } else if (brandLower.contains("opel") || brandLower.contains("mokka")) {
            return "/images/mocca.jpg";
        } else if (brandLower.contains("fiat")) {
            return "/images/indir.jpg";
        } else {
            return "/images/indir.jpg"; // varsayılan resim
        }
    }
    
    public static Car getCarById(int carId) {
        Map<String, Object> details = getCarDetails(carId);
        if (details.isEmpty()) {
            return null;
        }
        
        Car car = new Car();
        car.setCarId((Integer) details.get("CarID"));
        car.setPlateNumber((String) details.get("PlateNumber"));
        car.setBrand((String) details.get("Brand"));
        car.setModel((String) details.get("Model"));
        car.setYear((Integer) details.get("Year"));
        car.setDailyRate((Double) details.get("DailyRate"));
        
        return car;
    }
} 