package com.kerem.ordersystem.carrentalsystem.service;

import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import com.kerem.ordersystem.carrentalsystem.model.Customer;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Service for managing user account creation when admin creates rentals
 */
public class UserAccountService {
    
    /**
     * Create a user account for a customer during rental creation
     * Returns the created User ID
     */
    public static int createUserAccountForCustomer(Customer customer, String email) {
        UserCredentials credentials = generateCredentialsForCustomer(customer);
        
        System.out.println("🔄 Creating user account for: " + customer.getFullName());
        System.out.println("   Generated username: " + credentials.getUsername());
        System.out.println("   Generated password: " + credentials.getPassword());
        
        int userId = createUserInDatabase(credentials.getUsername(), credentials.getPassword(), email);
        
        if (userId > 0) {
            System.out.println("✅ User account created successfully with ID: " + userId);
        } else {
            System.err.println("❌ Failed to create user account for: " + customer.getFullName());
        }
        
        return userId;
    }
    
    /**
     * Get the generated username and password for a customer
     * This method is used for getting credentials after account creation
     */
    public static UserCredentials generateCredentialsForCustomer(Customer customer) {
        String username = generateUsername(customer.getFullName(), customer.getPhone());
        String password = EmailService.generateRandomPassword();
        return new UserCredentials(username, password);
    }
    
    /**
     * Inner class to hold user credentials
     */
    public static class UserCredentials {
        private String username;
        private String password;
        
        public UserCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
    
    /**
     * Create user in Users table
     */
    private static int createUserInDatabase(String username, String password, String email) {
        String sql = """
            INSERT INTO Users (Username, PasswordHash, Email, RoleID, CreatedAt) 
            VALUES (?, ?, ?, 2, GETDATE())
            """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Hash password with BCrypt
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error creating user in database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Generate a unique username based on customer name and phone
     */
    private static String generateUsername(String fullName, String phone) {
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "User";
        }
        
        // Clean name: remove spaces, special characters, convert to lowercase
        String cleanName = fullName.trim()
            .replaceAll("[^a-zA-ZğĞıİöÖüÜşŞçÇ]", "")
            .toLowerCase()
            .replace('ğ', 'g').replace('ç', 'c').replace('ş', 's')
            .replace('ı', 'i').replace('ü', 'u').replace('ö', 'o');
        
        // Take first 6 characters
        if (cleanName.length() > 6) {
            cleanName = cleanName.substring(0, 6);
        }
        
        // Add last 4 digits of phone (if available)
        String phoneSuffix = "";
        if (phone != null && phone.length() >= 4) {
            phoneSuffix = phone.replaceAll("[^0-9]", "");
            if (phoneSuffix.length() >= 4) {
                phoneSuffix = phoneSuffix.substring(phoneSuffix.length() - 4);
            }
        } else {
            // If no phone, add random 4 digits
            phoneSuffix = String.format("%04d", (int)(Math.random() * 10000));
        }
        
        String baseUsername = cleanName + phoneSuffix;
        
        // Check if username already exists and make it unique
        return ensureUniqueUsername(baseUsername);
    }
    
    /**
     * Ensure username is unique by adding number suffix if needed
     */
    private static String ensureUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        
        while (isUsernameExists(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
    
    /**
     * Check if username already exists in database
     */
    private static boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error checking username existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update customer record with the created user ID
     */
    public static boolean linkCustomerToUser(int customerId, int userId) {
        String sql = "UPDATE Customers SET UserID = ? WHERE CustomerID = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, customerId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Customer " + customerId + " linked to User " + userId);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error linking customer to user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get email address for customer notifications
     * This is a placeholder - you might want to add an email field to Customer table
     */
    public static String getCustomerEmail(Customer customer) {
        // For now, generate a placeholder email based on name and phone
        // In a real system, you would ask for email during customer creation
        String cleanName = customer.getFullName() != null ? 
            customer.getFullName().toLowerCase().replaceAll("[^a-z]", "") : "customer";
        
        String phone = customer.getPhone() != null ? 
            customer.getPhone().replaceAll("[^0-9]", "") : "0000";
        
        if (phone.length() >= 4) {
            phone = phone.substring(phone.length() - 4);
        }
        
        return cleanName + phone + "@email.com";
    }
    
    /**
     * Send welcome email with complete rental details
     */
    public static void sendWelcomeEmailWithRentalDetails(String email, String customerName, 
                                                       String username, String password,
                                                       String carBrand, String carModel, String plateNumber,
                                                       String startDate, String endDate, String totalAmount) {
        
        String carDetails = String.format("%s %s (%s)", carBrand, carModel, plateNumber);
        String rentalDetails = String.format("%s to %s - Total: ₺%s", startDate, endDate, totalAmount);
        
        EmailService.sendWelcomeEmail(email, customerName, username, password, carDetails, rentalDetails)
            .thenAccept(success -> {
                if (success) {
                    System.out.println("✅ Complete welcome email sent to: " + email);
                } else {
                    System.err.println("❌ Failed to send complete welcome email to: " + email);
                }
            });
    }
} 