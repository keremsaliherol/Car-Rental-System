package com.kerem.ordersystem.carrentalsystem.database;

import com.kerem.ordersystem.carrentalsystem.model.Customer;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced CustomerDAO with integrated database objects:
 * Uses Views, Stored Procedures, Functions, and Indexes
 */
public class CustomerDAO {
    
    private static final DatabaseService databaseService = new DatabaseService();

    // ========================================
    // VIEWS INTEGRATION - Customer Related Views
    // ========================================
    
    /**
     * Get customer rental history using CustomerRentalHistory view
     */
    public static List<Map<String, Object>> getCustomerRentalHistory(int customerId) {
        return databaseService.getCustomerRentalHistory(customerId);
    }
    
    /**
     * Get customer invoice summary using CustomerInvoiceSummary view
     */
    public static List<Map<String, Object>> getCustomerInvoiceSummary() {
        return databaseService.getCustomerInvoiceSummary();
    }

    // ========================================
    // STORED PROCEDURES INTEGRATION
    // ========================================
    
    /**
     * Add customer using stored procedure with validation
     */
    public static boolean addCustomerWithSP(int userId, String fullName, String phone, 
                                           String address, String driverLicenseNo, LocalDate dateOfBirth) {
        
        // Validate age using function
        int age = databaseService.getCustomerAge(dateOfBirth);
        if (age < 18) {
            System.out.println("❌ Customer must be at least 18 years old. Current age: " + age);
            return false;
        }
        
        // Check if customer already exists by phone (uses index)
        Customer existingCustomer = findCustomerByPhone(phone);
        if (existingCustomer != null) {
            System.out.println("❌ Customer with phone " + phone + " already exists");
            return false;
        }
        
        // Add customer using stored procedure
        boolean result = databaseService.addCustomer(userId, fullName, phone, address, driverLicenseNo, dateOfBirth);
        
        if (result) {
            System.out.println("✅ Customer added successfully");
            System.out.println("   Name: " + fullName);
            System.out.println("   Age: " + age + " years");
            System.out.println("   Phone: " + phone);
        }
        
        return result;
    }

    // ========================================
    // FUNCTIONS INTEGRATION
    // ========================================
    
    /**
     * Get customer age using function
     */
    public static int getCustomerAge(LocalDate dateOfBirth) {
        return databaseService.getCustomerAge(dateOfBirth);
    }
    
    /**
     * Get customer rental count using function
     */
    public static int getCustomerRentalCount(int customerId) {
        return databaseService.getCustomerRentalCount(customerId);
    }

    // ========================================
    // INDEX-OPTIMIZED QUERIES
    // ========================================
    
    /**
     * Find customer by phone (uses idx_Customers_Phone index)
     */
    public static Customer findCustomerByPhone(String phone) {
        Map<String, Object> result = databaseService.getCustomerByPhone(phone);
        
        if (result.isEmpty()) {
            return null;
        }
        
        return mapToCustomer(result);
    }
    
    /**
     * Search customers by name (uses idx_Customers_FullName index)
     */
    public static List<Customer> searchCustomersByName(String searchTerm) {
        List<Map<String, Object>> results = databaseService.getCustomersByName(searchTerm);
        List<Customer> customers = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            customers.add(mapToCustomer(row));
        }
        
        return customers;
    }
    
    // ========================================
    // COMPREHENSIVE CUSTOMER OPERATIONS
    // ========================================
    
    /**
     * Get comprehensive customer profile with statistics
     */
    public static Map<String, Object> getCustomerProfile(int customerId) {
        Map<String, Object> profile = new HashMap<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Get basic customer info
            String sql = "SELECT * FROM Customers WHERE CustomerID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, customerId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        profile.put("CustomerID", rs.getInt("CustomerID"));
                        profile.put("FullName", rs.getString("FullName"));
                        profile.put("Phone", rs.getString("Phone"));
                        profile.put("Email", rs.getString("Email"));
                        profile.put("Address", rs.getString("Address"));
                        profile.put("DriverLicenseNo", rs.getString("DriverLicenseNo"));
                        
                        Date dob = rs.getDate("DateOfBirth");
                        if (dob != null) {
                            LocalDate dateOfBirth = dob.toLocalDate();
                            profile.put("DateOfBirth", dateOfBirth);
                            
                            // Enrich with function results
                            profile.put("Age", databaseService.getCustomerAge(dateOfBirth));
                        }
                        
                        // Get rental statistics
                        profile.put("TotalRentals", databaseService.getCustomerRentalCount(customerId));
                        
                        // Get rental history
                        profile.put("RentalHistory", getCustomerRentalHistory(customerId));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting customer profile: " + e.getMessage());
        }
        
        return profile;
    }
    
    /**
     * Get customer loyalty status based on rental count
     */
    public static String getCustomerLoyaltyStatus(int customerId) {
        int rentalCount = getCustomerRentalCount(customerId);
        
        if (rentalCount >= 20) {
            return "Platinum";
        } else if (rentalCount >= 10) {
            return "Gold";
        } else if (rentalCount >= 5) {
            return "Silver";
        } else {
            return "Bronze";
        }
    }
    
    /**
     * Validate customer for rental eligibility
     */
    public static Map<String, Object> validateCustomerForRental(int customerId) {
        Map<String, Object> validation = new HashMap<>();
        Map<String, Object> profile = getCustomerProfile(customerId);
        
        if (profile.isEmpty()) {
            validation.put("isEligible", false);
            validation.put("reason", "Customer not found");
            return validation;
        }
        
        // Check age
        Integer age = (Integer) profile.get("Age");
        if (age == null || age < 18) {
            validation.put("isEligible", false);
            validation.put("reason", "Customer must be at least 18 years old");
            return validation;
        }
        
        // Check driver license
        String driverLicense = (String) profile.get("DriverLicenseNo");
        if (driverLicense == null || driverLicense.trim().isEmpty()) {
            validation.put("isEligible", false);
            validation.put("reason", "Valid driver license required");
            return validation;
        }
        
        validation.put("isEligible", true);
        validation.put("loyaltyStatus", getCustomerLoyaltyStatus(customerId));
        validation.put("totalRentals", profile.get("TotalRentals"));
        
        return validation;
    }

    // ========================================
    // LEGACY METHODS (Updated to use new system)
    // ========================================
    
    public static List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT * FROM Customers ORDER BY FullName";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(rs.getInt("CustomerID"));
                    customer.setFullName(rs.getString("FullName"));
                    customer.setPhone(rs.getString("Phone"));
                    customer.setEmail(rs.getString("Email"));
                    customer.setAddress(rs.getString("Address"));
                    customer.setDriverLicenseNo(rs.getString("DriverLicenseNo"));
                    
                    Date dob = rs.getDate("DateOfBirth");
                    if (dob != null) {
                        customer.setDateOfBirth(dob.toLocalDate());
                    }
                    
                    customers.add(customer);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting all customers: " + e.getMessage());
        }
        
        return customers;
    }
    
    /**
     * Get only customers who have user accounts with Customer role (RoleID = 2)
     */
    public static List<Customer> getCustomersWithUserAccounts() {
        List<Customer> customers = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = """
                SELECT c.CustomerID, c.FullName, c.Phone, c.Email, c.Address, 
                       c.DriverLicenseNo, c.DateOfBirth, u.Username
                FROM Customers c
                INNER JOIN Users u ON c.UserID = u.UserID
                WHERE u.RoleID = 2
                ORDER BY c.FullName
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(rs.getInt("CustomerID"));
                    customer.setFullName(rs.getString("FullName"));
                    customer.setPhone(rs.getString("Phone"));
                    customer.setEmail(rs.getString("Email"));
                    customer.setAddress(rs.getString("Address"));
                    customer.setDriverLicenseNo(rs.getString("DriverLicenseNo"));
                    
                    Date dob = rs.getDate("DateOfBirth");
                    if (dob != null) {
                        customer.setDateOfBirth(dob.toLocalDate());
                    }
                    
                    customers.add(customer);
                }
                
                System.out.println("✅ Found " + customers.size() + " customers with user accounts (Customer role)");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting customers with user accounts: " + e.getMessage());
            e.printStackTrace();
        }
        
        return customers;
    }
    
    public static Customer getCustomerById(int customerId) {
        Map<String, Object> profile = getCustomerProfile(customerId);
        
        if (profile.isEmpty()) {
            return null;
        }
        
        Customer customer = new Customer();
        customer.setCustomerId((Integer) profile.get("CustomerID"));
        customer.setFullName((String) profile.get("FullName"));
        customer.setPhone((String) profile.get("Phone"));
        customer.setEmail((String) profile.get("Email"));
        customer.setAddress((String) profile.get("Address"));
        customer.setDriverLicenseNo((String) profile.get("DriverLicenseNo"));
        customer.setDateOfBirth((LocalDate) profile.get("DateOfBirth"));
        
        return customer;
    }
    
    public static boolean addCustomer(Customer customer) {
        return addCustomerWithSP(
            customer.getUserId(),
            customer.getFullName(),
            customer.getPhone(),
            customer.getAddress(),
            customer.getDriverLicenseNo(),
            customer.getDateOfBirth()
        );
    }
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    private static Customer mapToCustomer(Map<String, Object> row) {
        Customer customer = new Customer();
        customer.setCustomerId((Integer) row.get("CustomerID"));
        customer.setFullName((String) row.get("FullName"));
        customer.setPhone((String) row.get("Phone"));
        customer.setAddress((String) row.get("Address"));
        customer.setDriverLicenseNo((String) row.get("DriverLicenseNo"));
        
        Date dob = (Date) row.get("DateOfBirth");
        if (dob != null) {
            customer.setDateOfBirth(dob.toLocalDate());
        }
        
        return customer;
    }

    /**
     * Insert customer and return the created Customer object with ID
     */
    public static Customer insertCustomerWithReturn(String fullName, String phone, String address, 
                                                   String driverLicenseNo, Date dateOfBirth) {
        String sql = "INSERT INTO Customers (FullName, Phone, Address, DriverLicenseNo, DateOfBirth) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, fullName);
            stmt.setString(2, phone);
            stmt.setString(3, address);
            stmt.setString(4, driverLicenseNo);
            stmt.setDate(5, dateOfBirth);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int customerId = generatedKeys.getInt(1);
                        
                        // Create and return Customer object
                        Customer customer = new Customer();
                        customer.setCustomerId(customerId);
                        customer.setFullName(fullName);
                        customer.setPhone(phone);
                        customer.setAddress(address);
                        customer.setDriverLicenseNo(driverLicenseNo);
                        customer.setDateOfBirth(dateOfBirth.toLocalDate());
                        
                        System.out.println("✅ Customer inserted successfully with ID: " + customerId);
                        return customer;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error inserting customer: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}