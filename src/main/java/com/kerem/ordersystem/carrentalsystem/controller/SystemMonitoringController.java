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
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class SystemMonitoringController implements Initializable {

    @FXML private Label dbStatusLabel;
    @FXML private Label dbSizeLabel;
    @FXML private Label agentStatusLabel;
    @FXML private Label lastBackupLabel;
    @FXML private Label backupSizeLabel;
    @FXML private Label connectionCountLabel;
    @FXML private Label uptimeLabel;
    @FXML private TableView<Object> jobsTable;
    @FXML private VBox backupHistoryContainer;
    @FXML private VBox systemLogsContainer;
    @FXML private Label totalCarsLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private Label activeRentalsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label systemUsersLabel;

    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔧 SystemMonitoringController initialize started...");
        
        try {
            // Sadece temel UI elementlerini kontrol et
            if (dbStatusLabel != null) {
                dbStatusLabel.setText("Status: Controller Loaded");
                dbStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4CAF50;");
            }
            
            System.out.println("🔧 Loading system logs...");
            loadSystemLogs();
            
            System.out.println("✅ SystemMonitoringController initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error initializing System Monitoring: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) dbStatusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Admin Dashboard");
        } catch (IOException e) {
            System.err.println("Error navigating back to admin: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshAll() {
        loadSystemStatus();
        loadDatabaseStatistics();
        loadSystemLogs();
    }

    @FXML
    private void handleManualBackup() {
        // TODO: Implement manual backup functionality
        System.out.println("Manual backup initiated");
        addSystemLogEntry("💾", "Manual Backup", "Administrator initiated manual database backup");
    }

    @FXML
    private void handleBrowseBackups() {
        // TODO: Implement backup browser
        System.out.println("Browse backups clicked");
    }

    @FXML
    private void handleRestoreBackup() {
        // TODO: Implement backup restore
        System.out.println("Restore backup clicked");
    }

    @FXML
    private void handleViewAllLogs() {
        // TODO: Implement log viewer
        System.out.println("View all logs clicked");
    }

    @FXML
    private void handleClearLogs() {
        if (systemLogsContainer != null) {
            systemLogsContainer.getChildren().clear();
            addSystemLogEntry("🗑️", "Logs Cleared", "System logs cleared by administrator");
        }
    }

    private void loadSystemStatus() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Database status
            if (dbStatusLabel != null) {
                dbStatusLabel.setText("Status: Connected");
                dbStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4CAF50;");
            }
            
            // Database size
            checkDatabaseSize(conn);
            
            // SQL Agent status (Express edition limitation)
            if (agentStatusLabel != null) {
                agentStatusLabel.setText("Status: Not Available (Express)");
                agentStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF9800;");
            }
            
            // Backup status
            checkLastBackup(conn);
            
            // Connection count
            checkConnectionCount(conn);
            
        } catch (SQLException e) {
            System.err.println("Error loading system status: " + e.getMessage());
            if (dbStatusLabel != null) {
                dbStatusLabel.setText("Status: Error");
                dbStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #F44336;");
            }
        }
    }

    private void checkDatabaseSize(Connection conn) throws SQLException {
        String sql = """
            SELECT 
                SUM(CAST(FILEPROPERTY(name, 'SpaceUsed') AS bigint) * 8192.) / 1024 / 1024 AS SizeMB
            FROM sys.database_files
            WHERE type IN (0,1)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && dbSizeLabel != null) {
                double sizeMB = rs.getDouble("SizeMB");
                dbSizeLabel.setText(String.format("Size: %.2f MB", sizeMB));
            }
        } catch (SQLException e) {
            if (dbSizeLabel != null) {
                dbSizeLabel.setText("Size: Unable to determine");
            }
        }
    }

    private void checkLastBackup(Connection conn) throws SQLException {
        String sql = """
            SELECT TOP 1 
                backup_start_date,
                backup_size
            FROM msdb.dbo.backupset 
            WHERE database_name = DB_NAME()
            ORDER BY backup_start_date DESC
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String backupDate = rs.getTimestamp("backup_start_date").toString();
                long backupSize = rs.getLong("backup_size");
                if (lastBackupLabel != null) {
                    lastBackupLabel.setText("Last: " + backupDate.substring(0, 16));
                }
                if (backupSizeLabel != null) {
                    backupSizeLabel.setText(String.format("Size: %.2f MB", backupSize / 1024.0 / 1024.0));
                }
            } else {
                if (lastBackupLabel != null) {
                    lastBackupLabel.setText("Last: No backups found");
                }
                if (backupSizeLabel != null) {
                    backupSizeLabel.setText("Size: N/A");
                }
            }
        } catch (SQLException e) {
            if (lastBackupLabel != null) {
                lastBackupLabel.setText("Last: Unable to check");
            }
            if (backupSizeLabel != null) {
                backupSizeLabel.setText("Size: Unable to check");
            }
        }
    }

    private void checkConnectionCount(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as connection_count FROM sys.dm_exec_sessions WHERE is_user_process = 1";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && connectionCountLabel != null) {
                int connectionCount = rs.getInt("connection_count");
                connectionCountLabel.setText("Connections: " + connectionCount);
            }
        } catch (SQLException e) {
            if (connectionCountLabel != null) {
                connectionCountLabel.setText("Connections: Unable to check");
            }
        }
        
        // Mock uptime
        if (uptimeLabel != null) {
            uptimeLabel.setText("Uptime: 2d 14h 32m");
        }
    }

    private void loadDatabaseStatistics() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Total cars
            loadTotalCars(conn);
            
            // Total customers
            loadTotalCustomers(conn);
            
            // Active rentals
            loadActiveRentals(conn);
            
            // Total revenue
            loadTotalRevenue(conn);
            
            // System users
            loadSystemUsers(conn);
            
        } catch (SQLException e) {
            System.err.println("Error loading database statistics: " + e.getMessage());
        }
    }

    private void loadTotalCars(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM Cars";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && totalCarsLabel != null) {
                totalCarsLabel.setText(String.valueOf(rs.getInt("total")));
            }
        }
    }

    private void loadTotalCustomers(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM Customers";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && totalCustomersLabel != null) {
                totalCustomersLabel.setText(String.valueOf(rs.getInt("total")));
            }
        }
    }

    private void loadActiveRentals(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM Rentals WHERE StatusID = 1"; // Active status
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && activeRentalsLabel != null) {
                activeRentalsLabel.setText(String.valueOf(rs.getInt("total")));
            }
        }
    }

    private void loadTotalRevenue(Connection conn) throws SQLException {
        String sql = "SELECT ISNULL(SUM(TotalAmount), 0) as total FROM Rentals WHERE StatusID IN (2, 3)"; // Completed/Cancelled
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && totalRevenueLabel != null) {
                double total = rs.getDouble("total");
                totalRevenueLabel.setText("₺" + currencyFormat.format(total));
            }
        }
    }

    private void loadSystemUsers(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM Users";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && systemUsersLabel != null) {
                systemUsersLabel.setText(String.valueOf(rs.getInt("total")));
            }
        } catch (SQLException e) {
            if (systemUsersLabel != null) {
                systemUsersLabel.setText("0"); // Table might not exist yet
            }
        }
    }

    private void loadSystemLogs() {
        if (systemLogsContainer != null) {
            systemLogsContainer.getChildren().clear();
            
            // Add sample system log entries
            addSystemLogEntry("🖥️", "System Startup", "System monitoring dashboard initialized");
            addSystemLogEntry("🗄️", "Database Check", "Database connection verified");
            addSystemLogEntry("⚙️", "Agent Status", "SQL Server Agent status checked");
            addSystemLogEntry("💾", "Backup Check", "Backup history reviewed");
            addSystemLogEntry("📊", "Statistics", "Database statistics loaded");
        }
    }

    private void addSystemLogEntry(String icon, String title, String description) {
        if (systemLogsContainer != null) {
            try {
                String timeStr = java.time.LocalTime.now().toString();
                if (timeStr.length() > 8) {
                    timeStr = timeStr.substring(0, 8);
                }
                
                Label logEntry = new Label(String.format("%s %s - %s [%s]", 
                    icon, title, description, timeStr));
                logEntry.setStyle("-fx-font-size: 12px; -fx-text-fill: #333; -fx-padding: 5;");
                logEntry.setWrapText(true);
                
                systemLogsContainer.getChildren().add(0, logEntry); // Add to top
                
                // Keep only last 15 entries
                if (systemLogsContainer.getChildren().size() > 15) {
                    systemLogsContainer.getChildren().remove(15, systemLogsContainer.getChildren().size());
                }
            } catch (Exception e) {
                System.err.println("Error adding system log entry: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 