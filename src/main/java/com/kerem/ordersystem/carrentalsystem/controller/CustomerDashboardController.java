package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.CustomerDAO;
import com.kerem.ordersystem.carrentalsystem.database.RentalDAO;
import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import com.kerem.ordersystem.carrentalsystem.model.Customer;
import com.kerem.ordersystem.carrentalsystem.util.StageUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class CustomerDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label profileFullName;
    @FXML private Label profilePhone;
    @FXML private Label profileEmail;
    @FXML private Label profileDriverLicense;
    @FXML private Label profileBirthDate;
    @FXML private Label profileAddress;
    @FXML private Label totalRentalsLabel;
    @FXML private Label activeRentalsLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label statusLabel;

    private static int currentCustomerId = -1;
    private static String currentCustomerName = "";
    
    public static void setCurrentCustomer(int customerId, String customerName) {
        currentCustomerId = customerId;
        currentCustomerName = customerName;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("🔧 CustomerDashboardController initialize started...");
            System.out.println("🔧 Current Customer ID: " + currentCustomerId);
            System.out.println("🔧 Current Customer Name: " + currentCustomerName);
            
            // Check if customer ID is valid
            if (currentCustomerId == -1) {
                System.err.println("❌ No customer ID set! Dashboard will show empty data.");
                statusLabel.setText("❌ No customer data available");
                return;
            }
            
            // Set welcome message
            if (!currentCustomerName.isEmpty()) {
                welcomeLabel.setText("Welcome, " + currentCustomerName + "!");
            } else {
                welcomeLabel.setText("Welcome!");
            }
            
            // Load customer profile
            loadCustomerProfile();
            
            // Load rental statistics
            loadRentalStatistics();
            
            statusLabel.setText("✅ Dashboard loaded successfully");
            System.out.println("✅ CustomerDashboardController initialized successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing CustomerDashboardController: " + e.getMessage());
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("❌ Error loading dashboard: " + e.getMessage());
            }
        }
    }

    private void loadCustomerProfile() {
        if (currentCustomerId == -1) {
            statusLabel.setText("❌ No customer selected");
            return;
        }

        try {
            Customer customer = CustomerDAO.getCustomerById(currentCustomerId);
            if (customer != null) {
                profileFullName.setText(customer.getFullName() != null ? customer.getFullName() : "N/A");
                profilePhone.setText(customer.getPhone() != null ? customer.getPhone() : "N/A");
                profileEmail.setText(customer.getEmail() != null ? customer.getEmail() : "N/A");
                profileDriverLicense.setText(customer.getDriverLicenseNo() != null ? customer.getDriverLicenseNo() : "N/A");
                profileBirthDate.setText(customer.getDateOfBirth() != null ? customer.getDateOfBirth().toString() : "N/A");
                profileAddress.setText(customer.getAddress() != null ? customer.getAddress() : "N/A");
            } else {
                statusLabel.setText("❌ Customer profile not found");
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading customer profile: " + e.getMessage());
            statusLabel.setText("❌ Error loading profile: " + e.getMessage());
        }
    }

    private void loadRentalStatistics() {
        if (currentCustomerId == -1) {
            System.err.println("❌ Cannot load rental statistics: Customer ID is -1");
            totalRentalsLabel.setText("0");
            activeRentalsLabel.setText("0");
            totalSpentLabel.setText("₺0");
            return;
        }

        System.out.println("🔧 Loading rental statistics for Customer ID: " + currentCustomerId);

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Total rentals
            String totalSql = "SELECT COUNT(*) as total FROM Rentals WHERE CustomerID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(totalSql)) {
                stmt.setInt(1, currentCustomerId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int totalRentals = rs.getInt("total");
                    totalRentalsLabel.setText(String.valueOf(totalRentals));
                    System.out.println("📊 Total rentals: " + totalRentals);
                }
            }

            // Active rentals
            String activeSql = "SELECT COUNT(*) as active FROM Rentals WHERE CustomerID = ? AND ISNULL(Status, 'Active') = 'Active'";
            try (PreparedStatement stmt = conn.prepareStatement(activeSql)) {
                stmt.setInt(1, currentCustomerId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int activeRentals = rs.getInt("active");
                    activeRentalsLabel.setText(String.valueOf(activeRentals));
                    System.out.println("📊 Active rentals: " + activeRentals);
                }
            }

            // Total spent
            String spentSql = "SELECT ISNULL(SUM(TotalAmount), 0) as total_spent FROM Rentals WHERE CustomerID = ? AND ISNULL(Status, 'Active') IN ('Completed', 'Active')";
            try (PreparedStatement stmt = conn.prepareStatement(spentSql)) {
                stmt.setInt(1, currentCustomerId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double totalSpent = rs.getDouble("total_spent");
                    String formattedAmount = "₺" + String.format("%.0f", totalSpent);
                    totalSpentLabel.setText(formattedAmount);
                    System.out.println("📊 Total spent: " + formattedAmount);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading rental statistics: " + e.getMessage());
            e.printStackTrace();
            // Set default values on error
            totalRentalsLabel.setText("0");
            activeRentalsLabel.setText("0");
            totalSpentLabel.setText("₺0");
        }
    }

    // handleViewAvailableCars method removed - button no longer exists in UI

    @FXML
    private void handleMyRentals() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/my-rentals.fxml"));
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.setFullscreenScene(stage, loader.load(), "My Rentals");
        } catch (IOException e) {
            System.err.println("❌ Error loading my rentals: " + e.getMessage());
            statusLabel.setText("❌ Error loading my rentals");
        }
    }

    // handleEditProfile removed - profile is now view-only

    @FXML
    private void handleChangePassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/change-password.fxml"));
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.setFullscreenScene(stage, loader.load(), "Change Password");
        } catch (IOException e) {
            System.err.println("❌ Error loading change password: " + e.getMessage());
            statusLabel.setText("❌ Error loading change password");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear current customer data
            currentCustomerId = -1;
            currentCustomerName = "";
            
            // Redirect to login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.setFullscreenScene(stage, loader.load(), "Car Rental System - Login");
            
        } catch (IOException e) {
            System.err.println("❌ Error during logout: " + e.getMessage());
            statusLabel.setText("❌ Error during logout");
        }
    }
} 