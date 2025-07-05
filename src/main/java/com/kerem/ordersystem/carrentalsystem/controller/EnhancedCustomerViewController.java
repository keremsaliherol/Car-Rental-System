package com.kerem.ordersystem.carrentalsystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import com.kerem.ordersystem.carrentalsystem.util.StageUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class EnhancedCustomerViewController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> securityLevelFilter;
    @FXML private CheckBox showEncryptedDataCheckBox;
    @FXML private TableView<Object> customersTable;
    @FXML private VBox customerDetailsContainer;
    @FXML private Label encryptedCustomersLabel;
    @FXML private Label unencryptedCustomersLabel;
    @FXML private Label encryptionPercentageLabel;
    @FXML private TableView<Object> rentalHistoryTable;
    @FXML private VBox securityLogContainer;
    @FXML private VBox communicationLogContainer;
    @FXML private ComboBox<String> activityFilterComboBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFilters();
        loadEncryptionStatistics();
        loadCustomers();
    }

    @FXML
    private void handleBackToAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) searchField.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Admin Dashboard");
        } catch (IOException e) {
            System.err.println("Error navigating back to admin: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText();
        String status = statusFilter.getValue();
        String securityLevel = securityLevelFilter.getValue();
        
        System.out.println("Searching customers with: " + searchTerm + ", Status: " + status + ", Security: " + securityLevel);
        // TODO: Implement customer search with filters
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        statusFilter.setValue(null);
        securityLevelFilter.setValue(null);
        loadCustomers();
    }

    @FXML
    private void handleExportData() {
        // TODO: Implement data export functionality
        System.out.println("Export customer data clicked");
    }

    @FXML
    private void handleToggleEncryptedData() {
        boolean showEncrypted = showEncryptedDataCheckBox.isSelected();
        System.out.println("Toggle encrypted data: " + showEncrypted);
        // TODO: Implement encrypted data toggle
    }

    @FXML
    private void handleRefreshCustomers() {
        loadCustomers();
        loadEncryptionStatistics();
    }

    @FXML
    private void handleEditCustomer() {
        // TODO: Implement customer editing
        System.out.println("Edit customer clicked");
    }

    @FXML
    private void handleManageCustomerSecurity() {
        // TODO: Implement customer security management
        System.out.println("Manage customer security clicked");
    }

    @FXML
    private void handleEncryptAllData() {
        // TODO: Implement bulk data encryption
        System.out.println("Encrypt all data clicked");
        addSecurityLogEntry("🔑", "Bulk Encryption", "Administrator initiated bulk data encryption");
    }

    @FXML
    private void handleAuditEncryptedData() {
        // TODO: Implement encryption audit
        System.out.println("Audit encrypted data clicked");
        addSecurityLogEntry("🔍", "Encryption Audit", "Administrator performed encryption audit");
    }

    @FXML
    private void handleManageUserAccounts() {
        // TODO: Implement user account management
        System.out.println("Manage user accounts clicked");
    }

    @FXML
    private void handleSendPasswordReset() {
        // TODO: Implement password reset email
        System.out.println("Send password reset clicked");
        addCommunicationLogEntry("📧", "Password Reset", "Password reset email sent to selected customers");
    }

    private void initializeFilters() {
        // Initialize status filter
        statusFilter.getItems().addAll("Active", "Inactive", "Suspended", "All");
        
        // Initialize security level filter
        securityLevelFilter.getItems().addAll("High", "Medium", "Low", "All");
        
        // Initialize activity filter
        activityFilterComboBox.getItems().addAll("All Activities", "Rentals", "Security Events", "Communications");
    }

    private void loadCustomers() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = """
                SELECT 
                    c.CustomerID,
                    c.FullName,
                    c.Phone,
                    c.DriverLicenseNo,
                    c.DriverLicenseNoEncrypted,
                    c.UserID,
                    u.Username
                FROM Customers c
                LEFT JOIN Users u ON c.UserID = u.UserID
                ORDER BY c.CustomerID DESC
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                // TODO: Populate table with customer data
                System.out.println("Loading customers...");
                int count = 0;
                while (rs.next()) {
                    count++;
                    // Process customer data here
                }
                System.out.println("Loaded " + count + " customers");
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
    }

    private void loadEncryptionStatistics() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Count encrypted records
            String encryptedSql = "SELECT COUNT(*) as count FROM Customers WHERE DriverLicenseNoEncrypted IS NOT NULL";
            int encryptedCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement(encryptedSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    encryptedCount = rs.getInt("count");
                }
            }
            
            // Count total records
            String totalSql = "SELECT COUNT(*) as count FROM Customers";
            int totalCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement(totalSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalCount = rs.getInt("count");
                }
            }
            
            int unencryptedCount = totalCount - encryptedCount;
            double encryptionPercentage = totalCount > 0 ? (encryptedCount * 100.0 / totalCount) : 0;
            
            encryptedCustomersLabel.setText("Encrypted Records: " + encryptedCount);
            unencryptedCustomersLabel.setText("Unencrypted Records: " + unencryptedCount);
            encryptionPercentageLabel.setText(String.format("Encryption Rate: %.1f%%", encryptionPercentage));
            
        } catch (SQLException e) {
            System.err.println("Error loading encryption statistics: " + e.getMessage());
            encryptedCustomersLabel.setText("Encrypted Records: Error");
            unencryptedCustomersLabel.setText("Unencrypted Records: Error");
            encryptionPercentageLabel.setText("Encryption Rate: Error");
        }
    }

    private void addSecurityLogEntry(String icon, String title, String description) {
        Label logEntry = new Label(String.format("%s %s - %s [%s]", 
            icon, title, description, 
            java.time.LocalTime.now().toString().substring(0, 8)));
        logEntry.setStyle("-fx-font-size: 12px; -fx-text-fill: #333; -fx-padding: 5;");
        logEntry.setWrapText(true);
        
        securityLogContainer.getChildren().add(0, logEntry);
        
        // Keep only last 10 entries
        if (securityLogContainer.getChildren().size() > 10) {
            securityLogContainer.getChildren().remove(10, securityLogContainer.getChildren().size());
        }
    }

    private void addCommunicationLogEntry(String icon, String title, String description) {
        Label logEntry = new Label(String.format("%s %s - %s [%s]", 
            icon, title, description, 
            java.time.LocalTime.now().toString().substring(0, 8)));
        logEntry.setStyle("-fx-font-size: 12px; -fx-text-fill: #333; -fx-padding: 5;");
        logEntry.setWrapText(true);
        
        communicationLogContainer.getChildren().add(0, logEntry);
        
        // Keep only last 10 entries
        if (communicationLogContainer.getChildren().size() > 10) {
            communicationLogContainer.getChildren().remove(10, communicationLogContainer.getChildren().size());
        }
    }
} 