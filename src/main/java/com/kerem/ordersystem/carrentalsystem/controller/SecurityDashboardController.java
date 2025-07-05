package com.kerem.ordersystem.carrentalsystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

public class SecurityDashboardController implements Initializable {

    @FXML private Label rlsStatusLabel;
    @FXML private Label encryptionStatusLabel;
    @FXML private Label userCountLabel;
    @FXML private ScrollPane securityLogsScrollPane;
    @FXML private VBox securityLogsContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔧 SecurityDashboardController initialize started...");
        
        try {
            // Sadece temel UI elementlerini kontrol et
            if (rlsStatusLabel != null) {
                rlsStatusLabel.setText("Status: Controller Loaded");
                rlsStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4CAF50;");
            }
            
            if (securityLogsContainer != null) {
                securityLogsContainer.getChildren().clear();
                Label testLabel = new Label("✅ Security Dashboard Controller loaded successfully!");
                testLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-padding: 10;");
                securityLogsContainer.getChildren().add(testLabel);
            }
            
            System.out.println("✅ SecurityDashboardController initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error initializing Security Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rlsStatusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Admin Dashboard");
        } catch (IOException e) {
            System.err.println("Error navigating back to admin: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewRLSPolicies() {
        addSecurityLogEntry("🔐", "RLS Policies Viewed", "Administrator viewed Row-Level Security policies");
        // TODO: Implement RLS policies viewer
        System.out.println("View RLS Policies clicked");
    }

    @FXML
    private void handleTestRLSAccess() {
        addSecurityLogEntry("🔍", "RLS Access Test", "Administrator tested RLS access controls");
        // TODO: Implement RLS access testing
        System.out.println("Test RLS Access clicked");
    }

    @FXML
    private void handleManageRoles() {
        addSecurityLogEntry("⚙️", "Role Management", "Administrator accessed role management");
        // TODO: Implement role management
        System.out.println("Manage Roles clicked");
    }

    @FXML
    private void handleEncryptionStats() {
        addSecurityLogEntry("📈", "Encryption Statistics", "Administrator viewed encryption statistics");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/encryption-statistics.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rlsStatusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Encryption Statistics");
        } catch (IOException e) {
            System.err.println("❌ Encryption Statistics could not be loaded: " + e.getMessage());
        }
    }

    @FXML
    private void handleMigrateData() {
        addSecurityLogEntry("🔄", "Data Migration", "Administrator initiated data migration process");
        // TODO: Implement data migration
        System.out.println("Migrate Data clicked");
    }

    @FXML
    private void handleAuditTrail() {
        addSecurityLogEntry("📋", "Audit Trail", "Administrator accessed audit trail");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/audit-trail.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rlsStatusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Audit Trail");
        } catch (IOException e) {
            System.err.println("❌ Audit Trail could not be loaded: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewAllUsers() {
        addSecurityLogEntry("👤", "User Management", "Administrator viewed all users");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user-management.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rlsStatusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "User Management");
        } catch (IOException e) {
            System.err.println("❌ User Management could not be loaded: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetPasswords() {
        addSecurityLogEntry("🔐", "Password Reset", "Administrator initiated password reset");
        // TODO: Implement password reset functionality
        System.out.println("Reset Passwords clicked");
    }

    @FXML
    private void handleEmailSettings() {
        addSecurityLogEntry("📧", "Email Settings", "Administrator accessed email settings");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/email-settings.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rlsStatusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Email Settings");
        } catch (IOException e) {
            System.err.println("❌ Email Settings could not be loaded: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshLogs() {
        loadSecurityLogs();
    }

    private void loadSecurityStatus() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Check RLS status
            checkRLSStatus(conn);
            
            // Check encryption status
            checkEncryptionStatus(conn);
            
            // Count users
            countUsers(conn);
            
        } catch (SQLException e) {
            System.err.println("Error loading security status: " + e.getMessage());
            rlsStatusLabel.setText("Status: Error");
            encryptionStatusLabel.setText("Status: Error");
            userCountLabel.setText("Total Users: Error");
        }
    }

    private void checkRLSStatus(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as policy_count FROM sys.security_policies WHERE is_enabled = 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int policyCount = rs.getInt("policy_count");
                if (policyCount > 0) {
                    rlsStatusLabel.setText("Status: Active (" + policyCount + " policies)");
                    rlsStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4CAF50;");
                } else {
                    rlsStatusLabel.setText("Status: No active policies");
                    rlsStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF9800;");
                }
            }
        } catch (SQLException e) {
            rlsStatusLabel.setText("Status: Unable to check");
            rlsStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #F44336;");
        }
    }

    private void checkEncryptionStatus(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as encrypted_count FROM Customers WHERE DriverLicenseNoEncrypted IS NOT NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int encryptedCount = rs.getInt("encrypted_count");
                encryptionStatusLabel.setText("Status: " + encryptedCount + " records encrypted");
                if (encryptedCount > 0) {
                    encryptionStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196F3;");
                } else {
                    encryptionStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF9800;");
                }
            }
        } catch (SQLException e) {
            encryptionStatusLabel.setText("Status: Unable to check");
            encryptionStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #F44336;");
        }
    }

    private void countUsers(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as user_count FROM Users";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int userCount = rs.getInt("user_count");
                userCountLabel.setText("Total Users: " + userCount);
                userCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF9800;");
            }
        } catch (SQLException e) {
            userCountLabel.setText("Total Users: Unable to count");
            userCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #F44336;");
        }
    }

    private void loadSecurityLogs() {
        securityLogsContainer.getChildren().clear();
        
        // Add some sample security log entries
        addSecurityLogEntry("🔐", "System Startup", "Security dashboard initialized");
        addSecurityLogEntry("🛡️", "RLS Check", "Row-Level Security policies verified");
        addSecurityLogEntry("🔑", "Encryption Check", "Data encryption status verified");
        addSecurityLogEntry("👥", "User Count", "System user count updated");
    }

    private void addSecurityLogEntry(String icon, String title, String description) {
        HBox logEntry = new HBox();
        logEntry.setSpacing(15);
        logEntry.setAlignment(Pos.CENTER_LEFT);
        logEntry.setStyle("-fx-padding: 10; -fx-border-radius: 6; -fx-background-color: #f8f9fa; -fx-background-radius: 6;");
        
        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        
        // Content
        VBox contentBox = new VBox();
        contentBox.setSpacing(2);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label descriptionLabel = new Label(description);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        descriptionLabel.setWrapText(true);
        
        contentBox.getChildren().addAll(titleLabel, descriptionLabel);
        
        // Timestamp
        Label timestampLabel = new Label(java.time.LocalTime.now().toString().substring(0, 8));
        timestampLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
        
        logEntry.getChildren().addAll(iconLabel, contentBox, timestampLabel);
        
        securityLogsContainer.getChildren().add(0, logEntry); // Add to top
        
        // Keep only last 10 entries
        if (securityLogsContainer.getChildren().size() > 10) {
            securityLogsContainer.getChildren().remove(10, securityLogsContainer.getChildren().size());
        }
    }
}