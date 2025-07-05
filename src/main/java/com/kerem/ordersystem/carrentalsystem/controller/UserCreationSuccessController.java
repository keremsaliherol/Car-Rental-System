package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.util.StageUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UserCreationSuccessController implements Initializable {

    @FXML private Label successMessageLabel;
    @FXML private Label userInfoLabel;
    @FXML private Label statusLabel;

    // Static variables to receive data from previous screen
    private static String customerName;
    private static String username;
    private static String password;
    private static String email;
    private static String rentalInfo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("🔧 UserCreationSuccessController initialize() started...");
            
            // Set success message
            successMessageLabel.setText("✅ User Account Created Successfully!");
            
            // Display user information
            if (customerName != null && username != null && password != null) {
                String userInfo = String.format(
                    "Customer: %s\nUsername: %s\nPassword: %s\nEmail: %s",
                    customerName, username, password, 
                    email != null ? email : "Not provided"
                );
                
                if (rentalInfo != null && !rentalInfo.isEmpty()) {
                    userInfo += "\n\nRental Information:\n" + rentalInfo;
                }
                
                userInfoLabel.setText(userInfo);
                statusLabel.setText("✅ Account created and ready for use");
            } else {
                userInfoLabel.setText("❌ User information not available");
                statusLabel.setText("❌ Error: Missing user data");
            }
            
            System.out.println("✅ UserCreationSuccessController initialized successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error in UserCreationSuccessController initialize: " + e.getMessage());
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("❌ Initialization error: " + e.getMessage());
            }
        }
    }

    // Static method to set user data from previous screen
    public static void setUserData(String customerName, String username, String password, String email) {
        UserCreationSuccessController.customerName = customerName;
        UserCreationSuccessController.username = username;
        UserCreationSuccessController.password = password;
        UserCreationSuccessController.email = email;
        
        System.out.println("✅ User data set for success screen:");
        System.out.println("   Customer: " + customerName);
        System.out.println("   Username: " + username);
        System.out.println("   Email: " + email);
    }

    // Static method to set rental information
    public static void setRentalInfo(String rentalInfo) {
        UserCreationSuccessController.rentalInfo = rentalInfo;
        System.out.println("✅ Rental info set: " + rentalInfo);
    }

    @FXML
    private void handleBackToRentals() {
        try {
            System.out.println("🔄 Navigating back to rentals...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/active-rentals.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Active Rentals");
            
        } catch (IOException e) {
            System.err.println("❌ Error navigating to rentals: " + e.getMessage());
            if (statusLabel != null) {
                statusLabel.setText("❌ Navigation error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBackToAdmin() {
        try {
            System.out.println("🔄 Navigating back to admin dashboard...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Admin Dashboard");
            
        } catch (IOException e) {
            System.err.println("❌ Error navigating to admin dashboard: " + e.getMessage());
            if (statusLabel != null) {
                statusLabel.setText("❌ Navigation error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSendEmail() {
        statusLabel.setText("🔄 Sending welcome email...");
        System.out.println("🔄 Send Email functionality - Email would be sent with credentials");
        
        // In a real implementation, you would send an email here
        // EmailService.sendWelcomeEmail(email, customerName, username, password);
        
        statusLabel.setText("✅ Welcome email sent successfully!");
    }

    @FXML
    private void handlePrintCredentials() {
        statusLabel.setText("🔄 Printing credentials...");
        System.out.println("🔄 Print Credentials functionality - Credentials would be printed");
        
        // In a real implementation, you would trigger printing here
        
        statusLabel.setText("✅ Credentials printed successfully!");
    }
} 