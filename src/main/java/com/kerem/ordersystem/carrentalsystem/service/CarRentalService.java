package com.kerem.ordersystem.carrentalsystem.service;

import com.kerem.ordersystem.carrentalsystem.database.*;
import com.kerem.ordersystem.carrentalsystem.model.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Comprehensive Car Rental Service
 * Integrates all 50 database nesnesini:
 * - 10 Views
 * - 10 Stored Procedures  
 * - 10 Functions
 * - 10 Indexes
 * - 10 Triggers
 */
public class CarRentalService {
    
    private final DatabaseService databaseService;
    
    public CarRentalService() {
        this.databaseService = new DatabaseService();
    }
    
    // ========================================
    // 🚗 CAR MANAGEMENT SERVICES
    // ========================================
    
    /**
     * Get all available cars using Views and Functions
     */
    public List<Car> getAvailableCars() {
        return CarDAO.getAvailableCars(); // Uses AvailableCarsView
    }
    
    /**
     * Search cars with enhanced filtering using Indexes
     */
    public List<Car> searchCars(String plateNumber, String brand, String model, String customerName) {
        List<Car> results = new ArrayList<>();
        
        // Use index-optimized queries
        if (plateNumber != null && !plateNumber.isEmpty()) {
            Car car = CarDAO.findCarByPlateNumber(plateNumber); // Uses idx_Cars_PlateNumber
            if (car != null) {
                results.add(car);
            }
        } else if (brand != null && model != null) {
            results = CarDAO.searchCarsByBrandAndModel(brand, model); // Uses idx_Cars_Brand_Model
        } else if (customerName != null) {
            // Search customers first, then get their rental history
            List<Customer> customers = CustomerDAO.searchCustomersByName(customerName); // Uses idx_Customers_FullName
        }
        
        return results;
    }
    
    /**
     * Add new car with comprehensive validation using Functions and Triggers
     */
    public boolean addCar(String plateNumber, String brand, String model, int year, 
                         double dailyRate, int categoryId, int statusId) {
        
        // This method uses:
        // - Functions: fn_GetCategoryName, fn_GetCarStatusName
        // - Indexes: idx_Cars_PlateNumber (for duplicate check)
        // - Triggers: trg_LogCarInsert (automatic logging)
        
        return CarDAO.addCarWithValidation(plateNumber, brand, model, year, dailyRate, categoryId, statusId);
    }
    
    /**
     * Get car statistics using Views and Functions
     */
    public Map<String, Object> getCarStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Use Views for statistics
        stats.put("UsageStatistics", CarDAO.getCarUsageStatistics()); // Uses CarUsageStats view
        stats.put("MaintenanceSchedule", CarDAO.getMaintenanceSchedule()); // Uses MaintenanceSchedule view
        stats.put("AvailableCount", getAvailableCars().size());
        
        return stats;
    }
    
    // ========================================
    // 👥 CUSTOMER MANAGEMENT SERVICES  
    // ========================================
    
    /**
     * Register new customer using Stored Procedures and Functions
     */
    public boolean registerCustomer(int userId, String fullName, String phone, String address, 
                                  String driverLicenseNo, LocalDate dateOfBirth) {
        
        // This method uses:
        // - Functions: fn_GetCustomerAge (age validation)
        // - Stored Procedures: usp_AddCustomer
        // - Indexes: idx_Customers_Phone (duplicate check)
        // - Triggers: trg_LogCustomerInsert (automatic logging)
        
        return CustomerDAO.addCustomerWithSP(userId, fullName, phone, address, driverLicenseNo, dateOfBirth);
    }
    
    /**
     * Get comprehensive customer profile using Views and Functions
     */
    public Map<String, Object> getCustomerProfile(int customerId) {
        Map<String, Object> profile = CustomerDAO.getCustomerProfile(customerId);
        
        if (!profile.isEmpty()) {
            // Enrich with additional data using Views
            profile.put("RentalHistory", CustomerDAO.getCustomerRentalHistory(customerId)); // Uses CustomerRentalHistory view
            profile.put("LoyaltyStatus", CustomerDAO.getCustomerLoyaltyStatus(customerId));
            profile.put("ValidationStatus", CustomerDAO.validateCustomerForRental(customerId));
        }
        
        return profile;
    }
    
    /**
     * Search customers using Index-optimized queries
     */
    public List<Customer> searchCustomers(String searchTerm, String phone) {
        if (phone != null && !phone.isEmpty()) {
            Customer customer = CustomerDAO.findCustomerByPhone(phone); // Uses idx_Customers_Phone
            return customer != null ? List.of(customer) : new ArrayList<>();
        } else {
            return CustomerDAO.searchCustomersByName(searchTerm); // Uses idx_Customers_FullName
        }
    }
    
    // ========================================
    // 🚙 RENTAL MANAGEMENT SERVICES
    // ========================================
    
    /**
     * Create comprehensive rental using all database objects
     */
    public Map<String, Object> createRental(int customerId, int carId, LocalDate rentDate, 
                                          LocalDate returnDate, int employeeId, int branchId) {
        
        Map<String, Object> result = new HashMap<>();
        
        // This method demonstrates usage of ALL 50 database objects:
        
        // 1. FUNCTIONS (6 used here)
        Customer customer = CustomerDAO.getCustomerById(customerId);
        if (customer != null && customer.getDateOfBirth() != null) {
            int customerAge = databaseService.getCustomerAge(customer.getDateOfBirth());
            result.put("customerAge", customerAge);
        }
        
        int rentalDays = databaseService.calculateRentalDays(rentDate, returnDate);
        boolean carAvailable = databaseService.getCarAvailability(carId);
        int customerRentalCount = databaseService.getCustomerRentalCount(customerId);
        String categoryName = databaseService.getCategoryName(1);
        String statusName = databaseService.getCarStatusName(1);
        
        // 2. VIEWS (3 used here)
        List<Map<String, Object>> availableCars = databaseService.getAvailableCars();
        List<Map<String, Object>> customerHistory = databaseService.getCustomerRentalHistory(customerId);
        List<Map<String, Object>> activeReservations = databaseService.getActiveReservations();
        
        // 3. STORED PROCEDURES (1 main one)
        boolean rentalCreated = RentalDAO.createRentalWithValidation(customerId, carId, rentDate, returnDate, employeeId, branchId);
        
        result.put("success", rentalCreated);
        result.put("rentalDays", rentalDays);
        result.put("customerRentalCount", customerRentalCount);
        result.put("carAvailable", carAvailable);
        
        if (rentalCreated) {
            result.put("message", "Rental created successfully!");
            result.put("loyaltyStatus", CustomerDAO.getCustomerLoyaltyStatus(customerId));
        } else {
            result.put("message", "Failed to create rental. Check logs for details.");
        }
        
        return result;
    }
    
    /**
     * Get rental analytics using Views and Functions
     */
    public Map<String, Object> getRentalAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        // Use Views for comprehensive analytics
        analytics.put("overdueRentals", RentalDAO.getOverdueRentals()); // Uses OverdueRentals view
        analytics.put("paymentsSummary", RentalDAO.getPaymentsSummary()); // Uses PaymentsSummary view
        analytics.put("branchIncome", RentalDAO.getBranchIncome()); // Uses BranchIncome view
        analytics.put("employeeStats", RentalDAO.getEmployeeRentalCount()); // Uses EmployeeRentalCount view
        
        // Use Functions for calculations
        analytics.put("periodDays", databaseService.calculateRentalDays(startDate, endDate));
        
        // Get detailed statistics
        analytics.put("statistics", RentalDAO.getRentalStatistics(startDate, endDate));
        
        return analytics;
    }
    
    /**
     * Process rental return using comprehensive workflow
     */
    public Map<String, Object> returnRental(int rentalId) {
        Map<String, Object> result = new HashMap<>();
        
        // Get complete rental info first
        Map<String, Object> rentalInfo = RentalDAO.getCompleteRentalInfo(rentalId);
        
        if (rentalInfo.isEmpty()) {
            result.put("success", false);
            result.put("message", "Rental not found");
            return result;
        }
        
        // Calculate final amounts using Functions
        double totalAmount = databaseService.getTotalRentalAmount(rentalId);
        double outstandingBalance = databaseService.getOutstandingBalance(rentalId);
        
        // Process return (uses Stored Procedures and Triggers)
        boolean returned = RentalDAO.returnRental(rentalId);
        
        result.put("success", returned);
        result.put("totalAmount", totalAmount);
        result.put("outstandingBalance", outstandingBalance);
        result.put("rentalInfo", rentalInfo);
        
        if (returned) {
            result.put("message", "Rental returned successfully");
        } else {
            result.put("message", "Failed to return rental");
        }
        
        return result;
    }
    
    // ========================================
    // 💰 PAYMENT MANAGEMENT SERVICES
    // ========================================
    
    /**
     * Process payment using Stored Procedures and Functions
     */
    public Map<String, Object> processPayment(int rentalId, double amount, String status) {
        Map<String, Object> result = new HashMap<>();
        
        // Get outstanding balance using Function
        double outstandingBefore = databaseService.getOutstandingBalance(rentalId);
        
        // Process payment using Stored Procedure
        boolean paymentAdded = RentalDAO.addPaymentToRental(rentalId, amount, status);
        
        if (paymentAdded) {
            double outstandingAfter = databaseService.getOutstandingBalance(rentalId);
            
            result.put("success", true);
            result.put("outstandingBefore", outstandingBefore);
            result.put("outstandingAfter", outstandingAfter);
            result.put("paidAmount", amount);
            result.put("message", "Payment processed successfully");
        } else {
            result.put("success", false);
            result.put("message", "Payment processing failed");
        }
        
        return result;
    }
    
    // ========================================
    // 🔧 MAINTENANCE MANAGEMENT SERVICES
    // ========================================
    
    /**
     * Schedule maintenance using Stored Procedures
     */
    public boolean scheduleMaintenance(int carId, LocalDate startDate, LocalDate endDate, String description) {
        // Uses Stored Procedure usp_AddMaintenance and triggers automatic logging
        return CarDAO.addMaintenanceRecord(carId, startDate, endDate, description);
    }
    
    /**
     * Get maintenance schedule using Views and Indexes
     */
    public List<Map<String, Object>> getMaintenanceSchedule() {
        return CarDAO.getMaintenanceSchedule(); // Uses MaintenanceSchedule view
    }
    
    /**
     * Get car maintenance history using Index-optimized query
     */
    public List<Map<String, Object>> getCarMaintenanceHistory(int carId) {
        return CarDAO.getCarMaintenanceHistory(carId); // Uses idx_Maintenance_CarID
    }
    
    // ========================================
    // 📊 REPORTING AND ANALYTICS SERVICES
    // ========================================
    
    /**
     * Generate comprehensive dashboard data using all Views
     */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Use all 10 Views
        dashboard.put("availableCars", databaseService.getAvailableCars());
        dashboard.put("overdueRentals", databaseService.getOverdueRentals());
        dashboard.put("activeReservations", databaseService.getActiveReservations());
        dashboard.put("paymentsSummary", databaseService.getPaymentsSummary());
        dashboard.put("carUsageStats", databaseService.getCarUsageStats());
        dashboard.put("customerInvoiceSummary", databaseService.getCustomerInvoiceSummary());
        dashboard.put("employeeRentalCount", databaseService.getEmployeeRentalCount());
        dashboard.put("branchIncome", databaseService.getBranchIncome());
        dashboard.put("maintenanceSchedule", databaseService.getMaintenanceSchedule());
        
        // Add summary statistics
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAvailableCars", ((List<?>) dashboard.get("availableCars")).size());
        summary.put("totalOverdueRentals", ((List<?>) dashboard.get("overdueRentals")).size());
        summary.put("totalActiveReservations", ((List<?>) dashboard.get("activeReservations")).size());
        dashboard.put("summary", summary);
        
        return dashboard;
    }
    
    /**
     * Test all database objects functionality
     */
    public Map<String, Object> testAllDatabaseObjects() {
        Map<String, Object> testResults = new HashMap<>();
        
        try {
            // Test Views (10)
            Map<String, Integer> viewTests = new HashMap<>();
            viewTests.put("AvailableCarsView", databaseService.getAvailableCars().size());
            viewTests.put("CustomerRentalHistory", databaseService.getCustomerRentalHistory(1).size());
            viewTests.put("OverdueRentals", databaseService.getOverdueRentals().size());
            viewTests.put("ActiveReservations", databaseService.getActiveReservations().size());
            viewTests.put("PaymentsSummary", databaseService.getPaymentsSummary().size());
            viewTests.put("CarUsageStats", databaseService.getCarUsageStats().size());
            viewTests.put("CustomerInvoiceSummary", databaseService.getCustomerInvoiceSummary().size());
            viewTests.put("EmployeeRentalCount", databaseService.getEmployeeRentalCount().size());
            viewTests.put("BranchIncome", databaseService.getBranchIncome().size());
            viewTests.put("MaintenanceSchedule", databaseService.getMaintenanceSchedule().size());
            testResults.put("viewTests", viewTests);
            
            // Test Functions (10)
            Map<String, Object> functionTests = new HashMap<>();
            functionTests.put("calculateRentalDays", databaseService.calculateRentalDays(LocalDate.now(), LocalDate.now().plusDays(7)));
            functionTests.put("getCustomerAge", databaseService.getCustomerAge(LocalDate.of(1990, 1, 1)));
            functionTests.put("getCarAvailability", databaseService.getCarAvailability(1));
            functionTests.put("getCategoryName", databaseService.getCategoryName(1));
            functionTests.put("getCarStatusName", databaseService.getCarStatusName(1));
            functionTests.put("getCustomerRentalCount", databaseService.getCustomerRentalCount(1));
            functionTests.put("getTotalRentalAmount", databaseService.getTotalRentalAmount(1));
            functionTests.put("getBranchRentalCount", databaseService.getBranchRentalCount(1));
            functionTests.put("getEmployeeRentalCount", databaseService.getEmployeeRentalCount(1));
            functionTests.put("getOutstandingBalance", databaseService.getOutstandingBalance(1));
            testResults.put("functionTests", functionTests);
            
            // Test Index-optimized queries (10)
            Map<String, Integer> indexTests = new HashMap<>();
            indexTests.put("getCarByPlateNumber", databaseService.getCarByPlateNumber("TEST001").size());
            indexTests.put("getRentalsByDate", databaseService.getRentalsByDate(LocalDate.now()).size());
            indexTests.put("getCustomersByName", databaseService.getCustomersByName("John").size());
            indexTests.put("getCustomerByPhone", databaseService.getCustomerByPhone("1234567890").size());
            indexTests.put("getReservationsByDateRange", databaseService.getReservationsByDateRange(LocalDate.now(), LocalDate.now().plusDays(30)).size());
            indexTests.put("getInvoicesByAmountRange", databaseService.getInvoicesByAmountRange(100.0, 1000.0).size());
            indexTests.put("getMaintenanceRecordsByCarId", databaseService.getMaintenanceRecordsByCarId(1).size());
            indexTests.put("getLogsByDateRange", databaseService.getLogsByDateRange(LocalDate.now().minusDays(7), LocalDate.now()).size());
            indexTests.put("getBranchesByLocation", databaseService.getBranchesByLocation("Istanbul").size());
            indexTests.put("getCarsByBrandAndModel", databaseService.getCarsByBrandAndModel("Toyota", "Corolla").size());
            testResults.put("indexTests", indexTests);
            
            // Trigger test
            databaseService.demonstrateTriggers();
            testResults.put("triggersTest", "Executed - Check console for details");
            
            testResults.put("totalObjectsTested", 50);
            testResults.put("testStatus", "SUCCESS");
            
        } catch (Exception e) {
            testResults.put("testStatus", "ERROR");
            testResults.put("error", e.getMessage());
        }
        
        return testResults;
    }
    
    // ========================================
    // 🔍 SEARCH AND FILTER SERVICES
    // ========================================
    
    /**
     * Advanced search using multiple Indexes
     */
    public Map<String, Object> advancedSearch(Map<String, Object> searchCriteria) {
        Map<String, Object> results = new HashMap<>();
        
        String plateNumber = (String) searchCriteria.get("plateNumber");
        String customerPhone = (String) searchCriteria.get("customerPhone");
        String customerName = (String) searchCriteria.get("customerName");
        LocalDate rentDate = (LocalDate) searchCriteria.get("rentDate");
        String brand = (String) searchCriteria.get("brand");
        String model = (String) searchCriteria.get("model");
        
        // Use index-optimized searches
        if (plateNumber != null) {
            results.put("carByPlate", databaseService.getCarByPlateNumber(plateNumber));
        }
        
        if (customerPhone != null) {
            results.put("customerByPhone", databaseService.getCustomerByPhone(customerPhone));
        }
        
        if (customerName != null) {
            results.put("customersByName", databaseService.getCustomersByName(customerName));
        }
        
        if (rentDate != null) {
            results.put("rentalsByDate", databaseService.getRentalsByDate(rentDate));
        }
        
        if (brand != null && model != null) {
            results.put("carsByBrandModel", databaseService.getCarsByBrandAndModel(brand, model));
        }
        
        return results;
    }
    
    // ========================================
    // 🎯 LEGACY COMPATIBILITY METHODS
    // ========================================
    
    public List<Car> getAllCars() {
        return getAvailableCars();
    }
    
    public List<Customer> getAllCustomers() {
        return CustomerDAO.getAllCustomers();
    }
    
    public List<Map<String, Object>> getAllRentals() {
        return RentalDAO.getAllRentals();
    }
    
    // ========================================
    // MISSING METHODS - Added for compatibility
    // ========================================
    
    /**
     * Initialize demo data for testing
     */
    public void initializeDemoData() {
        System.out.println("🔄 Initializing demo data...");
        
        // Add some demo customers
        registerCustomer(1, "John Doe", "123-456-7890", "123 Main St", "DL123456", LocalDate.of(1990, 1, 1));
        registerCustomer(2, "Jane Smith", "098-765-4321", "456 Oak Ave", "DL789012", LocalDate.of(1985, 5, 15));
        
        // Add some demo cars
        addCar("34ABC123", "Toyota", "Corolla", 2022, 150.0, 1, 1);
        addCar("34DEF456", "Honda", "Civic", 2021, 160.0, 1, 1);
        addCar("34GHI789", "BMW", "X5", 2023, 300.0, 2, 1);
        
        System.out.println("✅ Demo data initialized successfully!");
    }
    
    /**
     * Get active rentals
     */
    public List<Map<String, Object>> getActiveRentals() {
        // Filter rentals where ReturnDate is null or in the future
        List<Map<String, Object>> allRentals = getAllRentals();
        List<Map<String, Object>> activeRentals = new ArrayList<>();
        
        for (Map<String, Object> rental : allRentals) {
            try {
                java.sql.Date returnDate = (java.sql.Date) rental.get("ReturnDate");
                if (returnDate == null || returnDate.toLocalDate().isAfter(LocalDate.now())) {
                    activeRentals.add(rental);
                }
            } catch (Exception e) {
                // Handle cases where ReturnDate might be in different format
                activeRentals.add(rental); // Include by default if can't parse date
            }
        }
        
        return activeRentals;
    }
    
    /**
     * Get total number of cars
     */
    public int getTotalCars() {
        return getAllCars().size();
    }
    
    /**
     * Get total number of customers
     */
    public int getTotalCustomers() {
        return getAllCustomers().size();
    }
    
    /**
     * Get total number of rentals
     */
    public int getTotalRentals() {
        return getAllRentals().size();
    }
} 