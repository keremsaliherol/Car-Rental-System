package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import com.kerem.ordersystem.carrentalsystem.util.StageUtils;
import com.kerem.ordersystem.carrentalsystem.util.UserCustomerLinkFixer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Parent;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String enteredPassword = passwordField.getText();

        String sql = "SELECT UserID, PasswordHash, RoleID FROM Users WHERE Username = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("PasswordHash");
                int roleId = rs.getInt("RoleID");
                int userId = rs.getInt("UserID");

                // 🧪 Debug: Giriş kontrolü için log
                System.out.println("Kullanıcıdan gelen: " + enteredPassword);
                System.out.println("Veritabanından gelen: " + storedHash);
                System.out.println("Role ID: " + roleId);

                // Null/empty check
                if (storedHash == null || storedHash.isEmpty()) {
                    System.out.println("❌ HATA: Veritabanından hash boş geldi!");
                    errorLabel.setText("❌ Database error: Password hash not found");
                    return;
                }

                boolean isAuthenticated = false;

                if (roleId == 1) {
                    // 👑 Admin → düz şifre kontrolü
                    isAuthenticated = enteredPassword.equals(storedHash);
                    System.out.println("Admin düz şifre kontrolü: " + isAuthenticated);
                } else {
                    // 👤 Müşteriler → BCrypt kontrolü
                    try {
                        isAuthenticated = BCrypt.checkpw(enteredPassword, storedHash);
                        System.out.println("Customer BCrypt kontrolü: " + isAuthenticated);
                    } catch (Exception bcryptError) {
                        System.out.println("❌ BCrypt hatası: " + bcryptError.getMessage());
                        errorLabel.setText("❌ Authentication error");
                        return;
                    }
                }

                if (isAuthenticated) {
                    // ✅ Başarılı giriş: role'a göre yönlendir
                    System.out.println("✅ Giriş başarılı! Role: " + roleId);
                    
                    ChangePasswordController.setCurrentUserId(userId);
                    
                    // Set customer ID for profile editing (if user has a customer profile)
                    if (roleId == 2) {
                        setCustomerIdFromUserId(userId);
                    }
                    
                    // Role'a göre yönlendirme
                    if (roleId == 1) {
                        // Admin ekranı
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
                        Stage stage = (Stage) usernameField.getScene().getWindow();
                        StageUtils.initializeDimensions(stage);
                        StageUtils.setFullscreenScene(stage, loader.load(), "Admin Panel");
                    } else if (roleId == 2) {
                        // Customer ekranı - Customer Dashboard'a yönlendir
                        setCustomerDataFromUserId(userId);
                        
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/customer-dashboard.fxml"));
                        Stage stage = (Stage) usernameField.getScene().getWindow();
                        StageUtils.initializeDimensions(stage);
                        StageUtils.setFullscreenScene(stage, loader.load(), "Customer Dashboard");
                    } else {
                        // Diğer roller
                        errorLabel.setText("🚫 This role has no permission.");
                    }
                } else {
                    System.out.println("❌ Şifre eşleşmedi!");
                    errorLabel.setText("❌ Invalid credentials");
                }

            } else {
                System.out.println("❌ Kullanıcı bulunamadı: " + username);
                errorLabel.setText("❌ User not found");
            }

        } catch (Exception e) {
            System.out.println("❌ Login hatası: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("❌ Login error: " + e.getMessage());
        }
    }

    private void setCustomerIdFromUserId(int userId) {
        String sql = "SELECT CustomerID FROM Customers WHERE UserID = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int customerId = rs.getInt("CustomerID");
                EditProfileController.setCurrentCustomerId(customerId);
            }

        } catch (Exception e) {
            System.err.println("Error setting customer ID: " + e.getMessage());
        }
    }
    
    private void setCustomerDataFromUserId(int userId) {
        String sql = "SELECT c.CustomerID, c.FullName FROM Customers c WHERE c.UserID = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int customerId = rs.getInt("CustomerID");
                String customerName = rs.getString("FullName");
                
                // Set data for both controllers
                EditProfileController.setCurrentCustomerId(customerId);
                CustomerDashboardController.setCurrentCustomer(customerId, customerName);
                
                System.out.println("✅ Customer data set: ID=" + customerId + ", Name=" + customerName);
            } else {
                System.err.println("❌ No customer found for UserID: " + userId);
                System.out.println("🔧 Attempting to fix user-customer link...");
                
                // Try to fix the user-customer link automatically
                UserCustomerLinkFixer.LinkResult result = UserCustomerLinkFixer.fixAllUserCustomerLinks();
                
                if (result.success && result.linksFixed > 0) {
                    System.out.println("✅ Fixed " + result.linksFixed + " user-customer links, retrying...");
                    
                    // Retry after fixing links
                    try (PreparedStatement retryStmt = conn.prepareStatement(sql)) {
                        retryStmt.setInt(1, userId);
                        ResultSet retryRs = retryStmt.executeQuery();
                        
                        if (retryRs.next()) {
                            int customerId = retryRs.getInt("CustomerID");
                            String customerName = retryRs.getString("FullName");
                            
                            EditProfileController.setCurrentCustomerId(customerId);
                            CustomerDashboardController.setCurrentCustomer(customerId, customerName);
                            
                            System.out.println("✅ Customer data set after fix: ID=" + customerId + ", Name=" + customerName);
                        } else {
                            System.err.println("❌ Still no customer found after link fix for UserID: " + userId);
                        }
                    }
                } else {
                    System.err.println("❌ Failed to fix user-customer links: " + result.message);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error setting customer data: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 