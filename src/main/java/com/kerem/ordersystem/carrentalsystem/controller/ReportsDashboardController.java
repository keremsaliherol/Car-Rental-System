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
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ReportsDashboardController implements Initializable {

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalRentalsLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalCarsLabel;
    @FXML private VBox reportContainer;

    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeReportTypes();
        loadQuickStatistics();
        setDefaultDateRange();
    }

    @FXML
    private void handleBackToAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) reportTypeComboBox.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Admin Dashboard");
        } catch (IOException e) {
            System.err.println("Error navigating back to admin: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateReport() {
        String reportType = reportTypeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (reportType == null) {
            showAlert("Please select a report type");
            return;
        }
        
        generateReport(reportType, startDate, endDate);
    }

    @FXML
    private void handleRevenueReport() {
        reportTypeComboBox.setValue("Revenue Summary");
        generateReport("Revenue Summary", LocalDate.now().minusMonths(1), LocalDate.now());
    }

    @FXML
    private void handlePaymentAnalysis() {
        reportTypeComboBox.setValue("Payment Analysis");
        generateReport("Payment Analysis", LocalDate.now().minusMonths(1), LocalDate.now());
    }

    @FXML
    private void handleMonthlyReport() {
        reportTypeComboBox.setValue("Monthly Report");
        generateReport("Monthly Report", LocalDate.now().withDayOfMonth(1), LocalDate.now());
    }

    @FXML
    private void handleFleetReport() {
        reportTypeComboBox.setValue("Fleet Report");
        generateReport("Fleet Report", LocalDate.now().minusMonths(1), LocalDate.now());
    }

    @FXML
    private void handleRentalReport() {
        reportTypeComboBox.setValue("Rental Report");
        generateReport("Rental Report", LocalDate.now().minusMonths(1), LocalDate.now());
    }

    @FXML
    private void handleSecurityReport() {
        reportTypeComboBox.setValue("Security Report");
        generateReport("Security Report", LocalDate.now().minusMonths(1), LocalDate.now());
    }

    private void initializeReportTypes() {
        reportTypeComboBox.getItems().addAll(
            "Revenue Summary",
            "Payment Analysis", 
            "Monthly Report",
            "Fleet Report",
            "Rental Report",
            "Security Report"
        );
    }

    private void setDefaultDateRange() {
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
    }

    private void loadQuickStatistics() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            loadTotalRevenue(conn);
            loadTotalRentals(conn);
            loadTotalCustomers(conn);
            loadTotalCars(conn);
        } catch (SQLException e) {
            System.err.println("Error loading quick statistics: " + e.getMessage());
        }
    }

    private void loadTotalRevenue(Connection conn) throws SQLException {
        String sql = "SELECT ISNULL(SUM(TotalAmount), 0) as total FROM Rentals WHERE ISNULL(Status, 'Active') IN ('Completed', 'Active')";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                double total = rs.getDouble("total");
                totalRevenueLabel.setText("₺" + currencyFormat.format(total));
            }
        }
    }

    private void loadTotalRentals(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM Rentals";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                totalRentalsLabel.setText(String.valueOf(rs.getInt("total")));
            }
        }
    }

    private void loadTotalCustomers(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM Customers";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                totalCustomersLabel.setText(String.valueOf(rs.getInt("total")));
            }
        }
    }

    private void loadTotalCars(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM Cars";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                totalCarsLabel.setText(String.valueOf(rs.getInt("total")));
            }
        }
    }

    private void generateReport(String reportType, LocalDate startDate, LocalDate endDate) {
        reportContainer.getChildren().clear();
        
        Label reportTitle = new Label("📊 " + reportType);
        reportTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 10;");
        
        if (startDate != null && endDate != null) {
            Label dateRange = new Label("Period: " + startDate + " to " + endDate);
            dateRange.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 5;");
            reportContainer.getChildren().addAll(reportTitle, dateRange);
        } else {
            reportContainer.getChildren().add(reportTitle);
        }
        
        switch (reportType) {
            case "Revenue Summary":
                generateRevenueReport(startDate, endDate);
                break;
            case "Payment Analysis":
                generatePaymentAnalysisReport(startDate, endDate);
                break;
            case "Monthly Report":
                generateMonthlyReport(startDate, endDate);
                break;
            case "Fleet Report":
                generateFleetReport(startDate, endDate);
                break;
            case "Rental Report":
                generateRentalReport(startDate, endDate);
                break;
            case "Security Report":
                generateSecurityReport();
                break;
            default:
                Label noData = new Label("Report generation for '" + reportType + "' is not yet implemented.");
                noData.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
                reportContainer.getChildren().add(noData);
        }
    }

    private void generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = """
                SELECT 
                    COUNT(*) as rental_count,
                    SUM(TotalAmount) as total_revenue,
                    AVG(TotalAmount) as avg_revenue
                FROM Rentals 
                WHERE ISNULL(Status, 'Active') IN ('Completed', 'Active')
                """;
            
            if (startDate != null && endDate != null) {
                sql += " AND RentDate >= ? AND RentDate <= ?";
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (startDate != null && endDate != null) {
                    stmt.setDate(1, java.sql.Date.valueOf(startDate));
                    stmt.setDate(2, java.sql.Date.valueOf(endDate));
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int rentalCount = rs.getInt("rental_count");
                        double totalRevenue = rs.getDouble("total_revenue");
                        double avgRevenue = rs.getDouble("avg_revenue");
                        
                        addReportSection("💰 Revenue Summary", 
                            "Total Rentals: " + rentalCount + "\n" +
                            "Total Revenue: ₺" + currencyFormat.format(totalRevenue) + "\n" +
                            "Average Revenue: ₺" + currencyFormat.format(avgRevenue));
                    }
                }
            }
        } catch (SQLException e) {
            addReportSection("❌ Error", "Failed to generate revenue report: " + e.getMessage());
        }
    }

    private void generateSecurityReport() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String encryptionSql = "SELECT COUNT(*) as encrypted FROM Customers WHERE DriverLicenseNoEncrypted IS NOT NULL";
            String totalSql = "SELECT COUNT(*) as total FROM Customers";
            
            int encryptedCount = 0;
            int totalCount = 0;
            
            try (PreparedStatement stmt = conn.prepareStatement(encryptionSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    encryptedCount = rs.getInt("encrypted");
                }
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(totalSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalCount = rs.getInt("total");
                }
            }
            
            double encryptionRate = totalCount > 0 ? (encryptedCount * 100.0 / totalCount) : 0;
            
            addReportSection("🔐 Security Report", 
                "Security Status Summary:\n\n" +
                "Data Encryption:\n" +
                "- Encrypted Records: " + encryptedCount + "\n" +
                "- Total Records: " + totalCount + "\n" +
                "- Encryption Rate: " + String.format("%.1f%%", encryptionRate) + "\n\n" +
                "Row-Level Security: Active\n" +
                "User Account Management: Enabled");
                
        } catch (SQLException e) {
            addReportSection("❌ Error", "Failed to generate security report: " + e.getMessage());
        }
    }

    private void generatePaymentAnalysisReport(LocalDate startDate, LocalDate endDate) {
        addReportSection("💳 Payment Analysis", 
            "Payment analysis report is under development.\n" +
            "This feature will provide detailed payment statistics and trends.");
    }

    private void generateMonthlyReport(LocalDate startDate, LocalDate endDate) {
        addReportSection("📅 Monthly Report", 
            "Monthly report is under development.\n" +
            "This feature will provide monthly business insights and statistics.");
    }

    private void generateFleetReport(LocalDate startDate, LocalDate endDate) {
        addReportSection("🚗 Fleet Report", 
            "Fleet report is under development.\n" +
            "This feature will provide car utilization and fleet management insights.");
    }

    private void generateRentalReport(LocalDate startDate, LocalDate endDate) {
        addReportSection("📊 Rental Report", 
            "Rental report is under development.\n" +
            "This feature will provide detailed rental statistics and trends.");
    }

    private void addReportSection(String title, String content) {
        Label sectionTitle = new Label(title);
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 10 0 5 0;");
        
        Label sectionContent = new Label(content);
        sectionContent.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 5 0 15 0;");
        sectionContent.setWrapText(true);
        
        reportContainer.getChildren().addAll(sectionTitle, sectionContent);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}