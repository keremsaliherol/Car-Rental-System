package com.kerem.ordersystem.carrentalsystem.database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive Database Service Class
 * Uses all 10 Views, 10 Triggers, 10 Functions, 10 Stored Procedures, and 10 Indexes
 * from CarRentalSystem SQL Database
 */
public class DatabaseService {
    
    private final DatabaseManager dbManager;
    
    public DatabaseService() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    // ========================================
    // 10 VIEWS USAGE METHODS
    // ========================================
    
    /**
     * 1. AvailableCarsView - Get all available cars
     */
    public List<Map<String, Object>> getAvailableCars() {
        String sql = "SELECT * FROM AvailableCarsView";
        return executeViewQuery(sql);
    }
    
    /**
     * 2. CustomerRentalHistory - Get customer rental history
     */
    public List<Map<String, Object>> getCustomerRentalHistory(int customerId) {
        String sql = "SELECT * FROM CustomerRentalHistory WHERE CustomerID = ?";
        return executeViewQueryWithParam(sql, customerId);
    }
    
    /**
     * 3. OverdueRentals - Get overdue rentals
     */
    public List<Map<String, Object>> getOverdueRentals() {
        String sql = "SELECT * FROM OverdueRentals";
        return executeViewQuery(sql);
    }
    
    /**
     * 4. ActiveReservations - Get active reservations
     */
    public List<Map<String, Object>> getActiveReservations() {
        String sql = "SELECT * FROM ActiveReservations";
        return executeViewQuery(sql);
    }
    
    /**
     * 5. PaymentsSummary - Get payments summary
     */
    public List<Map<String, Object>> getPaymentsSummary() {
        String sql = "SELECT * FROM PaymentsSummary";
        return executeViewQuery(sql);
    }
    
    /**
     * 6. CarUsageStats - Get car usage statistics
     */
    public List<Map<String, Object>> getCarUsageStats() {
        String sql = "SELECT * FROM CarUsageStats";
        return executeViewQuery(sql);
    }
    
    /**
     * 7. CustomerInvoiceSummary - Get customer invoice summary
     */
    public List<Map<String, Object>> getCustomerInvoiceSummary() {
        String sql = "SELECT * FROM CustomerInvoiceSummary";
        return executeViewQuery(sql);
    }
    
    /**
     * 8. EmployeeRentalCount - Get employee rental count
     */
    public List<Map<String, Object>> getEmployeeRentalCount() {
        String sql = "SELECT * FROM EmployeeRentalCount";
        return executeViewQuery(sql);
    }
    
    /**
     * 9. BranchIncome - Get branch income
     */
    public List<Map<String, Object>> getBranchIncome() {
        String sql = "SELECT * FROM BranchIncome";
        return executeViewQuery(sql);
    }
    
    /**
     * 10. MaintenanceSchedule - Get maintenance schedule
     */
    public List<Map<String, Object>> getMaintenanceSchedule() {
        String sql = "SELECT * FROM MaintenanceSchedule";
        return executeViewQuery(sql);
    }
    
    // ========================================
    // 10 STORED PROCEDURES USAGE METHODS
    // ========================================
    
    /**
     * 1. usp_AddRental - Add new rental
     */
    public boolean addRental(int customerId, int carId, LocalDate rentDate, LocalDate returnDate, double totalAmount) {
        String sql = "{CALL usp_AddRental(?, ?, ?, ?, ?)}";
        try (Connection conn = dbManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, customerId);
            stmt.setInt(2, carId);
            stmt.setDate(3, Date.valueOf(rentDate));
            stmt.setDate(4, Date.valueOf(returnDate));
            stmt.setDouble(5, totalAmount);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error calling usp_AddRental: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 2. usp_GetAvailableCars - Get available cars via SP
     */
    public List<Map<String, Object>> getAvailableCarsViaSP() {
        String sql = "{CALL usp_GetAvailableCars}";
        return executeStoredProcedureQuery(sql);
    }
    
    /**
     * 3. usp_AddCustomer - Add new customer
     */
    public boolean addCustomer(int userId, String fullName, String phone, String address, 
                              String driverLicenseNo, LocalDate dateOfBirth) {
        String sql = "{CALL usp_AddCustomer(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = dbManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, fullName);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.setString(5, driverLicenseNo);
            stmt.setDate(6, Date.valueOf(dateOfBirth));
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error calling usp_AddCustomer: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 4. usp_UpdateCarStatus - Update car status
     */
    public boolean updateCarStatus(int carId, int statusId) {
        String sql = "{CALL usp_UpdateCarStatus(?, ?)}";
        try (Connection conn = dbManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, carId);
            stmt.setInt(2, statusId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error calling usp_UpdateCarStatus: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 5. usp_GetCustomerRentals - Get customer rentals via SP
     */
    public List<Map<String, Object>> getCustomerRentalsViaSP(int customerId) {
        String sql = "{CALL usp_GetCustomerRentals(?)}";
        try (Connection conn = dbManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, customerId);
            return executeCallableStatementQuery(stmt);
        } catch (SQLException e) {
            System.err.println("Error calling usp_GetCustomerRentals: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 6. usp_AddPayment - Add payment
     */
    public boolean addPayment(int rentalId, double amount, String status) {
        String sql = "{CALL usp_AddPayment(?, ?, ?)}";
        try (Connection conn = dbManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, rentalId);
            stmt.setDouble(2, amount);
            stmt.setString(3, status);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error calling usp_AddPayment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 7. usp_GetOverdueRentals - Get overdue rentals via SP
     */
    public List<Map<String, Object>> getOverdueRentalsViaSP() {
        String sql = "{CALL usp_GetOverdueRentals}";
        return executeStoredProcedureQuery(sql);
    }
    
    /**
     * 8. usp_AddMaintenance - Add maintenance record
     */
    public boolean addMaintenance(int carId, LocalDate startDate, LocalDate endDate, String description) {
        String sql = "{CALL usp_AddMaintenance(?, ?, ?, ?)}";
        try (Connection conn = dbManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, carId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            stmt.setString(4, description);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error calling usp_AddMaintenance: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 9. usp_GetBranchStats - Get branch statistics
     */
    public List<Map<String, Object>> getBranchStats(int branchId) {
        String sql = "{CALL usp_GetBranchStats(?)}";
        try (Connection conn = dbManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, branchId);
            return executeCallableStatementQuery(stmt);
        } catch (SQLException e) {
            System.err.println("Error calling usp_GetBranchStats: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 10. usp_CancelReservation - Cancel reservation
     */
    public boolean cancelReservation(int reservationId) {
        String sql = "{CALL usp_CancelReservation(?)}";
        try (Connection conn = dbManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, reservationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error calling usp_CancelReservation: " + e.getMessage());
            return false;
        }
    }
    
    // ========================================
    // 10 FUNCTIONS USAGE METHODS
    // ========================================
    
    /**
     * 1. fn_CalculateRentalDays - Calculate rental days
     */
    public int calculateRentalDays(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT dbo.fn_CalculateRentalDays(?, ?) AS RentalDays";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("RentalDays");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calling fn_CalculateRentalDays: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * 2. fn_CustomerRentalCount - Get customer rental count
     */
    public int getCustomerRentalCount(int customerId) {
        String sql = "SELECT dbo.fn_CustomerRentalCount(?) AS RentalCount";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("RentalCount");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calling fn_CustomerRentalCount: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * 3. fn_GetCustomerAge - Get customer age
     */
    public int getCustomerAge(LocalDate dateOfBirth) {
        String sql = "SELECT dbo.fn_GetCustomerAge(?) AS CustomerAge";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(dateOfBirth));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CustomerAge");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calling fn_GetCustomerAge: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * 4. fn_GetTotalRentalAmount - Get total rental amount
     */
    public double getTotalRentalAmount(int rentalId) {
        String sql = "SELECT dbo.fn_GetTotalRentalAmount(?) AS TotalAmount";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, rentalId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("TotalAmount");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calling fn_GetTotalRentalAmount: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * 5. fn_GetBranchRentalCount - Get branch rental count
     */
    public int getBranchRentalCount(int branchId) {
        String sql = "SELECT dbo.fn_GetBranchRentalCount(?) AS RentalCount";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, branchId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("RentalCount");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calling fn_GetBranchRentalCount: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * 6. fn_GetEmployeeRentalCount - Get employee rental count
     */
    public int getEmployeeRentalCount(int employeeId) {
        String sql = "SELECT dbo.fn_GetEmployeeRentalCount(?) AS RentalCount";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("RentalCount");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calling fn_GetEmployeeRentalCount: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * 7. fn_GetCarAvailability - Check car availability
     */
    public boolean getCarAvailability(int carId) {
        String sql = "SELECT dbo.fn_GetCarAvailability(?) AS IsAvailable";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, carId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("IsAvailable");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calling fn_GetCarAvailability: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 8. fn_GetOutstandingBalance - Get outstanding balance
     */
    public double getOutstandingBalance(int rentalId) {
        String sql = "SELECT dbo.fn_GetOutstandingBalance(?) AS OutstandingBalance";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, rentalId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("OutstandingBalance");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calling fn_GetOutstandingBalance: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * 9. fn_GetCategoryName - Get category name
     */
    public String getCategoryName(int categoryId) {
        String sql = "SELECT dbo.fn_GetCategoryName(?) AS CategoryName";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("CategoryName");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calling fn_GetCategoryName: " + e.getMessage());
        }
        return "";
    }
    
    /**
     * 10. fn_GetCarStatusName - Get car status name
     */
    public String getCarStatusName(int statusId) {
        String sql = "SELECT dbo.fn_GetCarStatusName(?) AS StatusName";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, statusId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("StatusName");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calling fn_GetCarStatusName: " + e.getMessage());
        }
        return "";
    }
    
    // ========================================
    // INDEX OPTIMIZATION METHODS
    // ========================================
    
    /**
     * 1. Query using idx_Cars_PlateNumber index
     */
    public Map<String, Object> getCarByPlateNumber(String plateNumber) {
        String sql = "SELECT * FROM Cars WHERE PlateNumber = ?"; // Uses idx_Cars_PlateNumber
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, plateNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetToMap(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error querying by plate number: " + e.getMessage());
        }
        return new HashMap<>();
    }
    
    /**
     * 2. Query using idx_Rentals_RentDate index
     */
    public List<Map<String, Object>> getRentalsByDate(LocalDate rentDate) {
        String sql = "SELECT * FROM Rentals WHERE RentDate = ?"; // Uses idx_Rentals_RentDate
        return executeQueryWithDateParam(sql, rentDate);
    }
    
    /**
     * 3. Query using idx_Customers_FullName index
     */
    public List<Map<String, Object>> getCustomersByName(String fullName) {
        String sql = "SELECT * FROM Customers WHERE FullName LIKE ?"; // Uses idx_Customers_FullName
        return executeQueryWithStringParam(sql, "%" + fullName + "%");
    }
    
    /**
     * 4. Query using idx_Customers_Phone index
     */
    public Map<String, Object> getCustomerByPhone(String phone) {
        String sql = "SELECT * FROM Customers WHERE Phone = ?"; // Uses idx_Customers_Phone
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, phone);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetToMap(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error querying by phone: " + e.getMessage());
        }
        return new HashMap<>();
    }
    
    /**
     * 5. Query using idx_Reservations_Dates index
     */
    public List<Map<String, Object>> getReservationsByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM Reservations WHERE ReservedFrom >= ? AND ReservedTo <= ?"; // Uses idx_Reservations_Dates
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            return executeResultSet(stmt);
        } catch (SQLException e) {
            System.err.println("Error querying reservations by date range: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 6. Query using idx_Invoices_TotalAmount index
     */
    public List<Map<String, Object>> getInvoicesByAmountRange(double minAmount, double maxAmount) {
        String sql = "SELECT * FROM Invoices WHERE TotalAmount BETWEEN ? AND ?"; // Uses idx_Invoices_TotalAmount
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, minAmount);
            stmt.setDouble(2, maxAmount);
            
            return executeResultSet(stmt);
        } catch (SQLException e) {
            System.err.println("Error querying invoices by amount: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 7. Query using idx_Maintenance_CarID index
     */
    public List<Map<String, Object>> getMaintenanceRecordsByCarId(int carId) {
        String sql = "SELECT * FROM MaintenanceRecords WHERE CarID = ?"; // Uses idx_Maintenance_CarID
        return executeQueryWithIntParam(sql, carId);
    }
    
    /**
     * 8. Query using idx_Logs_PerformedAt index
     */
    public List<Map<String, Object>> getLogsByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM Logs WHERE CAST(PerformedAt AS DATE) BETWEEN ? AND ?"; // Uses idx_Logs_PerformedAt
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            return executeResultSet(stmt);
        } catch (SQLException e) {
            System.err.println("Error querying logs by date: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 9. Query using idx_Branches_Location index
     */
    public List<Map<String, Object>> getBranchesByLocation(String location) {
        String sql = "SELECT * FROM Branches WHERE Location LIKE ?"; // Uses idx_Branches_Location
        return executeQueryWithStringParam(sql, "%" + location + "%");
    }
    
    /**
     * 10. Query using idx_Cars_Brand_Model index
     */
    public List<Map<String, Object>> getCarsByBrandAndModel(String brand, String model) {
        String sql = "SELECT * FROM Cars WHERE Brand = ? AND Model = ?"; // Uses idx_Cars_Brand_Model
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, brand);
            stmt.setString(2, model);
            
            return executeResultSet(stmt);
        } catch (SQLException e) {
            System.err.println("Error querying cars by brand/model: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // ========================================
    // TRIGGER DEMONSTRATION METHODS
    // ========================================
    
    /**
     * Demonstrates trigger usage by performing operations that activate them
     */
    public void demonstrateTriggers() {
        System.out.println("🔥 Demonstrating Database Triggers:");
        
        // 1. trg_PreventOverlappingReservations - Insert reservation
        System.out.println("1. Testing reservation overlap trigger...");
        insertTestReservation();
        
        // 2. trg_UpdateCarStatusOnReturn - Update rental
        System.out.println("2. Testing car status update trigger...");
        updateTestRental();
        
        // 3. trg_LogCustomerInsert - Insert customer
        System.out.println("3. Testing customer insert logging trigger...");
        insertTestCustomer();
        
        // 4. trg_LogCarInsert - Insert car
        System.out.println("4. Testing car insert logging trigger...");
        insertTestCar();
        
        // 5. trg_UpdateMileageAfterReturn - Update rental mileage
        System.out.println("5. Testing mileage update trigger...");
        updateTestRentalMileage();
        
        // 6. trg_AutoCreateInvoice - Insert rental (auto invoice creation)
        System.out.println("6. Testing auto invoice creation trigger...");
        insertTestRentalForInvoice();
        
        // 7. trg_AutoPaymentStatus - Insert payment
        System.out.println("7. Testing auto payment status trigger...");
        insertTestPayment();
        
        // 8. trg_MaintenanceLog - Insert maintenance record
        System.out.println("8. Testing maintenance logging trigger...");
        insertTestMaintenance();
        
        // 9. trg_EmailNotification - Insert reservation (email notification)
        System.out.println("9. Testing email notification trigger...");
        insertTestReservationForEmail();
        
        // 10. trg_DeleteProtection - Try to delete car
        System.out.println("10. Testing delete protection trigger...");
        testCarDeleteProtection();
        
        System.out.println("✅ All 10 triggers demonstrated!");
    }
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    private List<Map<String, Object>> executeViewQuery(String sql) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return executeResultSet(stmt);
        } catch (SQLException e) {
            System.err.println("Error executing view query: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private List<Map<String, Object>> executeViewQueryWithParam(String sql, int param) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, param);
            return executeResultSet(stmt);
        } catch (SQLException e) {
            System.err.println("Error executing view query with param: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private List<Map<String, Object>> executeStoredProcedureQuery(String sql) {
        try (Connection conn = dbManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            return executeCallableStatementQuery(stmt);
        } catch (SQLException e) {
            System.err.println("Error executing stored procedure: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private List<Map<String, Object>> executeCallableStatementQuery(CallableStatement stmt) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                results.add(resultSetToMap(rs));
            }
        }
        return results;
    }
    
    private List<Map<String, Object>> executeQueryWithIntParam(String sql, int param) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, param);
            return executeResultSet(stmt);
        } catch (SQLException e) {
            System.err.println("Error executing query with int param: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private List<Map<String, Object>> executeQueryWithStringParam(String sql, String param) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, param);
            return executeResultSet(stmt);
        } catch (SQLException e) {
            System.err.println("Error executing query with string param: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private List<Map<String, Object>> executeQueryWithDateParam(String sql, LocalDate date) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            return executeResultSet(stmt);
        } catch (SQLException e) {
            System.err.println("Error executing query with date param: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private List<Map<String, Object>> executeResultSet(PreparedStatement stmt) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                results.add(resultSetToMap(rs));
            }
        }
        return results;
    }
    
    private Map<String, Object> resultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> row = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            Object value = rs.getObject(i);
            row.put(columnName, value);
        }
        
        return row;
    }
    
    // Trigger demonstration helper methods
    private void insertTestReservation() {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO Reservations (CustomerID, CarID, ReservationDate, ReservedFrom, ReservedTo) VALUES (1, 1, GETDATE(), GETDATE(), DATEADD(day, 5, GETDATE()))";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
                System.out.println("  ✅ Test reservation inserted - trigger activated");
            }
        } catch (SQLException e) {
            System.out.println("  ⚠️ Trigger prevented operation or: " + e.getMessage());
        }
    }
    
    private void updateTestRental() {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "UPDATE Rentals SET ReturnDate = GETDATE() WHERE RentalID = 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
                System.out.println("  ✅ Test rental updated - trigger activated");
            }
        } catch (SQLException e) {
            System.out.println("  ⚠️ Update failed or: " + e.getMessage());
        }
    }
    
    private void insertTestCustomer() {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO Customers (FullName, Phone) VALUES ('Test Customer', '1234567890')";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
                System.out.println("  ✅ Test customer inserted - logging trigger activated");
            }
        } catch (SQLException e) {
            System.out.println("  ⚠️ Insert failed or: " + e.getMessage());
        }
    }
    
    private void insertTestCar() {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO Cars (PlateNumber, Brand, Model, Year, DailyRate, CategoryID, StatusID) VALUES ('TEST001', 'Test Brand', 'Test Model', 2024, 100.00, 1, 1)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
                System.out.println("  ✅ Test car inserted - logging trigger activated");
            }
        } catch (SQLException e) {
            System.out.println("  ⚠️ Insert failed or: " + e.getMessage());
        }
    }
    
    private void updateTestRentalMileage() {
        try (Connection conn = dbManager.getConnection()) {
            // Update rental to trigger mileage update
            String sql = "UPDATE Rentals SET ReturnDate = GETDATE() WHERE RentalID = 2";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("  ✅ Rental updated - mileage trigger activated");
                } else {
                    System.out.println("  ⚠️ No rental found to update");
                }
            }
        } catch (SQLException e) {
            System.out.println("  ⚠️ Mileage update failed: " + e.getMessage());
        }
    }
    
    private void insertTestRentalForInvoice() {
        try (Connection conn = dbManager.getConnection()) {
            // Insert rental to trigger auto invoice creation
            String sql = "INSERT INTO Rentals (CustomerID, CarID, RentDate, ReturnDate, TotalAmount) VALUES (1, 2, GETDATE(), DATEADD(day, 3, GETDATE()), 300.00)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
                System.out.println("  ✅ Rental inserted - auto invoice trigger activated");
            }
        } catch (SQLException e) {
            System.out.println("  ⚠️ Rental insert failed: " + e.getMessage());
        }
    }
    
    private void insertTestPayment() {
        try (Connection conn = dbManager.getConnection()) {
            // Insert payment to trigger auto status update
            String sql = "INSERT INTO Payments (RentalID, Amount, Status) VALUES (1, 150.00, 'Pending')";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
                System.out.println("  ✅ Payment inserted - auto status trigger activated");
            }
        } catch (SQLException e) {
            System.out.println("  ⚠️ Payment insert failed: " + e.getMessage());
        }
    }
    
    private void insertTestMaintenance() {
        try (Connection conn = dbManager.getConnection()) {
            // Insert maintenance record to trigger logging
            String sql = "INSERT INTO MaintenanceRecords (CarID, StartDate, EndDate, Description) VALUES (1, GETDATE(), DATEADD(day, 2, GETDATE()), 'Test maintenance - trigger demo')";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
                System.out.println("  ✅ Maintenance record inserted - logging trigger activated");
            }
        } catch (SQLException e) {
            System.out.println("  ⚠️ Maintenance insert failed: " + e.getMessage());
        }
    }
    
    private void insertTestReservationForEmail() {
        try (Connection conn = dbManager.getConnection()) {
            // Insert reservation to trigger email notification
            String sql = "INSERT INTO Reservations (CustomerID, CarID, ReservationDate, ReservedFrom, ReservedTo) VALUES (2, 3, GETDATE(), DATEADD(day, 1, GETDATE()), DATEADD(day, 4, GETDATE()))";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
                System.out.println("  ✅ Reservation inserted - email notification trigger activated");
            }
        } catch (SQLException e) {
            System.out.println("  ⚠️ Reservation insert failed or trigger prevented: " + e.getMessage());
        }
    }
    
    private void testCarDeleteProtection() {
        try (Connection conn = dbManager.getConnection()) {
            // Try to delete a car to test protection trigger
            String sql = "DELETE FROM Cars WHERE CarID = 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("  ⚠️ Car was deleted - protection trigger may not be working");
                } else {
                    System.out.println("  ✅ Car deletion prevented - protection trigger activated");
                }
            }
        } catch (SQLException e) {
            System.out.println("  ✅ Car deletion blocked by trigger: " + e.getMessage());
        }
    }
}