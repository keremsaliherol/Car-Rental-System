package com.kerem.ordersystem.carrentalsystem.database;

import com.kerem.ordersystem.carrentalsystem.model.Car;
import com.kerem.ordersystem.carrentalsystem.model.Customer;
import com.kerem.ordersystem.carrentalsystem.model.Rental;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced RentalDAO with integrated database objects:
 * Uses Views, Stored Procedures, Functions, and Indexes
 */
public class RentalDAO {

    private static final DatabaseService databaseService = new DatabaseService();

    // ========================================
    // VIEWS INTEGRATION - Rental Related Views
    // ========================================
    
    /**
     * Get overdue rentals using OverdueRentals view
     */
    public static List<Map<String, Object>> getOverdueRentals() {
        return databaseService.getOverdueRentals();
    }
    
    /**
     * Get active rentals for the controller
     */
    public static List<Map<String, Object>> getActiveRentals() {
        List<Map<String, Object>> rentals = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = """
                SELECT r.RentalID, r.CustomerID, r.CarID, r.RentDate, r.ReturnDate, r.TotalAmount,
                       c.FullName as CustomerName, c.Phone as CustomerPhone,
                       car.PlateNumber, car.Brand as CarBrand, car.Model as CarModel, car.DailyRate,
                       ISNULL(r.Status, 'Active') as Status,
                       r.RentDate as CreatedAt
                FROM Rentals r
                JOIN Customers c ON r.CustomerID = c.CustomerID
                JOIN Cars car ON r.CarID = car.CarID
                WHERE ISNULL(r.Status, 'Active') = 'Active'
                ORDER BY r.RentDate DESC
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Map<String, Object> rental = new HashMap<>();
                    rental.put("RentalID", rs.getInt("RentalID"));
                    rental.put("CustomerID", rs.getInt("CustomerID"));
                    rental.put("CarID", rs.getInt("CarID"));
                    rental.put("CustomerName", rs.getString("CustomerName"));
                    rental.put("CustomerPhone", rs.getString("CustomerPhone"));
                    rental.put("PlateNumber", rs.getString("PlateNumber"));
                    rental.put("CarBrand", rs.getString("CarBrand"));
                    rental.put("CarModel", rs.getString("CarModel"));
                    rental.put("DailyRate", rs.getDouble("DailyRate"));
                    rental.put("RentDate", rs.getDate("RentDate"));
                    rental.put("ReturnDate", rs.getDate("ReturnDate"));
                    rental.put("TotalAmount", rs.getDouble("TotalAmount"));
                    rental.put("Status", rs.getString("Status"));
                    rental.put("CreatedAt", rs.getDate("CreatedAt"));
                    rentals.add(rental);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting active rentals: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rentals;
    }
    
    /**
     * Get active reservations using ActiveReservations view
     */
    public static List<Map<String, Object>> getActiveReservations() {
        return databaseService.getActiveReservations();
    }
    
    /**
     * Get payments summary using PaymentsSummary view
     */
    public static List<Map<String, Object>> getPaymentsSummary() {
        return databaseService.getPaymentsSummary();
    }
    
    /**
     * Get branch income using BranchIncome view
     */
    public static List<Map<String, Object>> getBranchIncome() {
        return databaseService.getBranchIncome();
    }
    
    /**
     * Get employee rental count using EmployeeRentalCount view
     */
    public static List<Map<String, Object>> getEmployeeRentalCount() {
        return databaseService.getEmployeeRentalCount();
    }

    // ========================================
    // STORED PROCEDURES INTEGRATION
    // ========================================
    
    /**
     * Create rental using stored procedure with comprehensive validation
     */
    public static boolean createRentalWithValidation(int customerId, int carId, LocalDate rentDate, 
                                                   LocalDate returnDate, int employeeId, int branchId) {
        
        // 1. Validate customer eligibility
        Map<String, Object> customerValidation = CustomerDAO.validateCustomerForRental(customerId);
        if (!(Boolean) customerValidation.get("isEligible")) {
            System.out.println("❌ Customer validation failed: " + customerValidation.get("reason"));
            return false;
        }
        
        // 2. Check car availability using function
        boolean isCarAvailable = databaseService.getCarAvailability(carId);
        if (!isCarAvailable) {
            System.out.println("❌ Car is not available for rental");
            return false;
        }
        
        // 3. Calculate rental amount using function
        int rentalDays = databaseService.calculateRentalDays(rentDate, returnDate);
        if (rentalDays <= 0) {
            System.out.println("❌ Invalid rental dates");
            return false;
        }
        
        // 4. Get car details for rate calculation
        Map<String, Object> carDetails = CarDAO.getCarDetails(carId);
        if (carDetails.isEmpty()) {
            System.out.println("❌ Car not found");
        return false;
    }

        double dailyRate = (Double) carDetails.get("DailyRate");
        double totalAmount = dailyRate * rentalDays;
        
        // 5. Apply loyalty discount
        String loyaltyStatus = CustomerDAO.getCustomerLoyaltyStatus(customerId);
        double discount = calculateLoyaltyDiscount(loyaltyStatus);
        totalAmount = totalAmount * (1 - discount);
        
        System.out.println("💰 Rental calculation:");
        System.out.println("   Days: " + rentalDays);
        System.out.println("   Daily Rate: $" + dailyRate);
        System.out.println("   Loyalty Status: " + loyaltyStatus + " (Discount: " + (discount * 100) + "%)");
        System.out.println("   Total Amount: $" + totalAmount);
        
        // 6. Create rental using stored procedure
        boolean result = databaseService.addRental(customerId, carId, rentDate, returnDate, totalAmount);
        
        if (result) {
            // 7. Update car status to rented
            CarDAO.updateCarStatus(carId, 2); // Status 2 = Rented
            System.out.println("✅ Rental created successfully");
        }
        
        return result;
    }
    
    /**
     * Add payment using stored procedure
     */
    public static boolean addPaymentToRental(int rentalId, double amount, String status) {
        // Get outstanding balance first
        double outstandingBalance = databaseService.getOutstandingBalance(rentalId);
        
        if (amount > outstandingBalance) {
            System.out.println("⚠️ Payment amount ($" + amount + ") exceeds outstanding balance ($" + outstandingBalance + ")");
        }
        
        boolean result = databaseService.addPayment(rentalId, amount, status);
        
        if (result) {
            double newBalance = databaseService.getOutstandingBalance(rentalId);
            System.out.println("✅ Payment added successfully");
            System.out.println("   Amount: $" + amount);
            System.out.println("   Remaining Balance: $" + newBalance);
        }
        
        return result;
    }
    
    /**
     * Cancel reservation using stored procedure
     */
    public static boolean cancelReservation(int reservationId) {
        return databaseService.cancelReservation(reservationId);
    }

    // ========================================
    // FUNCTIONS INTEGRATION
    // ========================================
    
    /**
     * Calculate rental duration using function
     */
    public static int calculateRentalDuration(LocalDate startDate, LocalDate endDate) {
        return databaseService.calculateRentalDays(startDate, endDate);
    }
    
    /**
     * Get total rental amount using function
     */
    public static double getRentalTotalAmount(int rentalId) {
        return databaseService.getTotalRentalAmount(rentalId);
    }
    
    /**
     * Get outstanding balance using function
     */
    public static double getOutstandingBalance(int rentalId) {
        return databaseService.getOutstandingBalance(rentalId);
    }

    // ========================================
    // INDEX-OPTIMIZED QUERIES
    // ========================================
    
    /**
     * Get rentals by date (uses idx_Rentals_RentDate index)
     */
    public static List<Map<String, Object>> getRentalsByDate(LocalDate date) {
        return databaseService.getRentalsByDate(date);
    }
    
    /**
     * Get reservations by date range (uses idx_Reservations_Dates index)
     */
    public static List<Map<String, Object>> getReservationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return databaseService.getReservationsByDateRange(startDate, endDate);
    }
    
    /**
     * Get invoices by amount range (uses idx_Invoices_TotalAmount index)
     */
    public static List<Map<String, Object>> getInvoicesByAmountRange(double minAmount, double maxAmount) {
        return databaseService.getInvoicesByAmountRange(minAmount, maxAmount);
    }

    // ========================================
    // COMPREHENSIVE RENTAL OPERATIONS
    // ========================================
    
    /**
     * Get complete rental information with all related data
     */
    public static Map<String, Object> getCompleteRentalInfo(int rentalId) {
        Map<String, Object> rentalInfo = new HashMap<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = """
                SELECT r.*, c.FullName as CustomerName, c.Phone as CustomerPhone,
                       car.PlateNumber, car.Brand, car.Model, car.DailyRate,
                       e.FullName as EmployeeName, b.BranchName
                FROM Rentals r
                JOIN Customers c ON r.CustomerID = c.CustomerID
                JOIN Cars car ON r.CarID = car.CarID
                LEFT JOIN Employees e ON r.EmployeeID = e.EmployeeID
                LEFT JOIN Branches b ON r.BranchID = b.BranchID
                WHERE r.RentalID = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, rentalId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Basic rental info
                        rentalInfo.put("RentalID", rs.getInt("RentalID"));
                        rentalInfo.put("CustomerID", rs.getInt("CustomerID"));
                        rentalInfo.put("CarID", rs.getInt("CarID"));
                        rentalInfo.put("RentDate", rs.getDate("RentDate"));
                        rentalInfo.put("ReturnDate", rs.getDate("ReturnDate"));
                        rentalInfo.put("TotalAmount", rs.getDouble("TotalAmount"));
                        
                        // Customer info
                        rentalInfo.put("CustomerName", rs.getString("CustomerName"));
                        rentalInfo.put("CustomerPhone", rs.getString("CustomerPhone"));
                        
                        // Car info
                        rentalInfo.put("PlateNumber", rs.getString("PlateNumber"));
                        rentalInfo.put("CarBrand", rs.getString("Brand"));
                        rentalInfo.put("CarModel", rs.getString("Model"));
                        rentalInfo.put("DailyRate", rs.getDouble("DailyRate"));
                        
                        // Employee and branch info
                        rentalInfo.put("EmployeeName", rs.getString("EmployeeName"));
                        rentalInfo.put("BranchName", rs.getString("BranchName"));
                        
                        // Enrich with function results
                        Date rentDate = rs.getDate("RentDate");
                        Date returnDate = rs.getDate("ReturnDate");
                        
                        if (rentDate != null && returnDate != null) {
                            int duration = databaseService.calculateRentalDays(
                                rentDate.toLocalDate(), returnDate.toLocalDate());
                            rentalInfo.put("RentalDuration", duration);
                        }
                        
                        // Get outstanding balance
                        rentalInfo.put("OutstandingBalance", databaseService.getOutstandingBalance(rentalId));
                        
                        // Get payment history
                        rentalInfo.put("PaymentHistory", getPaymentHistoryForRental(rentalId));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting complete rental info: " + e.getMessage());
        }
        
        return rentalInfo;
    }

    /**
     * Get rental statistics for a date range
     */
    public static Map<String, Object> getRentalStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = """
                SELECT 
                    COUNT(*) as TotalRentals,
                    SUM(TotalAmount) as TotalRevenue,
                    AVG(TotalAmount) as AverageRentalAmount,
                    COUNT(DISTINCT CustomerID) as UniqueCustomers,
                    COUNT(DISTINCT CarID) as CarsRented
                FROM Rentals 
                WHERE RentDate BETWEEN ? AND ?
                """;
        
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(startDate));
                stmt.setDate(2, Date.valueOf(endDate));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.put("TotalRentals", rs.getInt("TotalRentals"));
                        stats.put("TotalRevenue", rs.getDouble("TotalRevenue"));
                        stats.put("AverageRentalAmount", rs.getDouble("AverageRentalAmount"));
                        stats.put("UniqueCustomers", rs.getInt("UniqueCustomers"));
                        stats.put("CarsRented", rs.getInt("CarsRented"));
                    }
                }
            }
            
            // Add period info
            stats.put("StartDate", startDate);
            stats.put("EndDate", endDate);
            stats.put("PeriodDays", databaseService.calculateRentalDays(startDate, endDate));
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting rental statistics: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Complete a rental (same as return rental)
     */
    public static boolean completeRental(int rentalId) {
        return returnRental(rentalId);
    }

    /**
     * Cancel a rental and update car status
     */
    public static boolean cancelRental(int rentalId) {
        System.out.println("🔄 Starting rental cancellation process - RentalID: " + rentalId);
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Get rental info first
                System.out.println("📋 Getting rental information...");
                Map<String, Object> rentalInfo = getCompleteRentalInfo(rentalId);
                if (rentalInfo.isEmpty()) {
                    System.err.println("❌ Rental not found - RentalID: " + rentalId);
                    return false;
                }
                
                int carId = (Integer) rentalInfo.get("CarID");
                String plateNumber = (String) rentalInfo.get("PlateNumber");
                System.out.println("📋 Rental info found - CarID: " + carId + ", Plate: " + plateNumber);
                
                // Update rental status to cancelled
                System.out.println("🔄 Updating rental status to 'Cancelled'...");
                String updateRentalSql = "UPDATE Rentals SET Status = 'Cancelled' WHERE RentalID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateRentalSql)) {
            stmt.setInt(1, rentalId);
                    int rows = stmt.executeUpdate();
            
                    if (rows == 0) {
                        System.err.println("❌ No rental found to cancel - RentalID: " + rentalId);
                        return false;
                    }
                    System.out.println("✅ Rental status updated to 'Cancelled'");
                }
                
                // Update car status to available (since rental is cancelled)
                System.out.println("🔄 Updating car status to 'Available'...");
                boolean carStatusUpdated = CarDAO.updateCarStatus(conn, carId, 1); // Status 1 = Available
                
                if (!carStatusUpdated) {
                    System.err.println("❌ Failed to update car status - rolling back transaction");
                    conn.rollback();
                    return false;
                }
                
                conn.commit();
                
                System.out.println("✅ Rental cancellation completed successfully!");
                System.out.println("   Rental ID: " + rentalId);
                System.out.println("   Car ID: " + carId);
                System.out.println("   Plate Number: " + plateNumber);
                System.out.println("   Car Status: Available");
                
                return true;
                
            } catch (SQLException e) {
                System.err.println("❌ SQL Error during rental cancellation - rolling back");
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error cancelling rental: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Return a rental and update car status
     */
    public static boolean returnRental(int rentalId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Get rental info
                Map<String, Object> rentalInfo = getCompleteRentalInfo(rentalId);
                if (rentalInfo.isEmpty()) {
                    System.out.println("❌ Rental not found");
        return false;
    }

                int carId = (Integer) rentalInfo.get("CarID");
                
                // Update rental return date and status
                String updateRentalSql = "UPDATE Rentals SET ReturnDate = GETDATE(), Status = 'Completed' WHERE RentalID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateRentalSql)) {
                    stmt.setInt(1, rentalId);
                    stmt.executeUpdate();
                }
                
                // Update car status to available (triggers will handle this automatically)
                CarDAO.updateCarStatus(conn, carId, 1); // Status 1 = Available
                
                conn.commit();
                
                System.out.println("✅ Rental returned successfully");
                System.out.println("   Rental ID: " + rentalId);
                System.out.println("   Car: " + rentalInfo.get("PlateNumber"));
                
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error returning rental: " + e.getMessage());
            return false;
        }
    }

    // ========================================
    // LEGACY METHODS (Updated to use new system)
    // ========================================
    
    public static boolean insertRental(int customerId, int carId, Date rentDate, Date returnDate, double totalAmount) {
        return createRentalWithValidation(
            customerId, 
            carId, 
            rentDate.toLocalDate(), 
            returnDate.toLocalDate(),
            1, // Default employee
            1  // Default branch
        );
    }
    
    public static List<Map<String, Object>> getAllRentals() {
        List<Map<String, Object>> rentals = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = """
                SELECT r.RentalID, r.CustomerID, r.CarID, r.RentDate, r.ReturnDate, r.TotalAmount,
                       c.FullName as CustomerName, c.Phone as CustomerPhone,
                       car.PlateNumber, car.Brand as CarBrand, car.Model as CarModel, car.DailyRate,
                       ISNULL(r.Status, 
                           CASE 
                               WHEN r.ReturnDate IS NULL THEN 'Active'
                               WHEN r.ReturnDate > CAST(GETDATE() AS DATE) THEN 'Active'
                               WHEN r.ReturnDate <= CAST(GETDATE() AS DATE) THEN 'Completed'
                               ELSE 'Active'
                           END
                       ) as Status,
                       r.RentDate as CreatedAt
                FROM Rentals r
                JOIN Customers c ON r.CustomerID = c.CustomerID
                JOIN Cars car ON r.CarID = car.CarID
                ORDER BY r.RentDate DESC
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Map<String, Object> rental = new HashMap<>();
                    rental.put("RentalID", rs.getInt("RentalID"));
                    rental.put("CustomerID", rs.getInt("CustomerID"));
                    rental.put("CarID", rs.getInt("CarID"));
                    rental.put("CustomerName", rs.getString("CustomerName"));
                    rental.put("CustomerPhone", rs.getString("CustomerPhone"));
                    rental.put("PlateNumber", rs.getString("PlateNumber"));
                    rental.put("CarBrand", rs.getString("CarBrand"));
                    rental.put("CarModel", rs.getString("CarModel"));
                    rental.put("DailyRate", rs.getDouble("DailyRate"));
                    rental.put("RentDate", rs.getDate("RentDate"));
                    rental.put("ReturnDate", rs.getDate("ReturnDate"));
                    rental.put("TotalAmount", rs.getDouble("TotalAmount"));
                    rental.put("Status", rs.getString("Status"));
                    rental.put("CreatedAt", rs.getDate("CreatedAt"));
                    rentals.add(rental);
                }
            }
            
            System.out.println("✅ Retrieved " + rentals.size() + " rentals from database");
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting all rentals: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rentals;
    }

    // ========================================
    // HELPER METHODS
    // ========================================
    
    private static double calculateLoyaltyDiscount(String loyaltyStatus) {
        return switch (loyaltyStatus) {
            case "Platinum" -> 0.15; // 15% discount
            case "Gold" -> 0.10;     // 10% discount
            case "Silver" -> 0.05;   // 5% discount
            default -> 0.0;          // No discount
        };
    }
    
    private static List<Map<String, Object>> getPaymentHistoryForRental(int rentalId) {
        List<Map<String, Object>> payments = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT * FROM Payments WHERE RentalID = ? ORDER BY PaymentDate";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rentalId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> payment = new HashMap<>();
                        payment.put("PaymentID", rs.getInt("PaymentID"));
                        payment.put("Amount", rs.getDouble("Amount"));
                        payment.put("PaymentDate", rs.getDate("PaymentDate"));
                        payment.put("Status", rs.getString("Status"));
                        payments.add(payment);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting payment history: " + e.getMessage());
        }
        
        return payments;
            }
            
    /**
     * Calculate total amount for a rental period
     */
    public double calculateTotalAmount(int carId, LocalDate startDate, LocalDate endDate) {
        double totalAmount = 0.0;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Get car's daily rate
            String carSql = "SELECT DailyRate FROM Cars WHERE CarID = ?";
            try (PreparedStatement carStmt = conn.prepareStatement(carSql)) {
                carStmt.setInt(1, carId);
                
                try (ResultSet carRs = carStmt.executeQuery()) {
                    if (carRs.next()) {
                        double dailyRate = carRs.getDouble("DailyRate");
                        
                        // Calculate number of days
                        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
                        
                        totalAmount = dailyRate * days;
                        
                        System.out.println("💰 Rental calculation:");
                        System.out.println("   Daily Rate: ₺" + dailyRate);
                        System.out.println("   Days: " + days);
                        System.out.println("   Total: ₺" + totalAmount);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error calculating total amount: " + e.getMessage());
        }
        
        return totalAmount;
    }
    
    /**
     * Create a new rental record
     */
    public boolean createRental(Rental rental) {
        String sql = "INSERT INTO Rentals (CustomerID, CarID, RentDate, ReturnDate, TotalAmount) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, rental.getCustomerId());
            stmt.setInt(2, rental.getCarId());
            stmt.setDate(3, Date.valueOf(rental.getStartDate()));
            stmt.setDate(4, Date.valueOf(rental.getEndDate()));
            stmt.setDouble(5, rental.getTotalAmount());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int rentalId = generatedKeys.getInt(1);
                        rental.setRentalId(rentalId);
                        
                        // Update car status to rented
                        updateCarStatusToRented(rental.getCarId());
                        
                        System.out.println("✅ Rental created successfully with ID: " + rentalId);
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error creating rental: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Update car status to rented
     */
    private void updateCarStatusToRented(int carId) {
        String sql = "UPDATE Cars SET StatusID = 2 WHERE CarID = ?"; // 2 = Rented
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, carId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating car status: " + e.getMessage());
        }
    }
}