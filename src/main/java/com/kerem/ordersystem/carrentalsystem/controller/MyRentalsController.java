package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MyRentalsController {

    @FXML private ComboBox<String> statusFilterComboBox, dateRangeComboBox;
    @FXML private VBox rentalsContainer;
    @FXML private Label statusLabel, rentalCountLabel;

    private int currentCustomerId;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    public void initialize() {
        System.out.println("🔧 MyRentals: Initializing...");
        
        // Get customer ID from session
        fetchCustomerId();
        
        // If customer ID not found, try to fix the link
        if (currentCustomerId <= 0) {
            System.out.println("⚠️ MyRentals: Customer ID not found, attempting to fix user-customer link...");
            attemptToFixUserCustomerLink();
            fetchCustomerId(); // Try again after fix
        }
        
        // Initialize combo boxes
        initializeComboBoxes();
        
        // Load rentals
        loadRentals();
    }

    private void initializeComboBoxes() {
        // Status Filter ComboBox
        statusFilterComboBox.getItems().addAll("All Statuses", "Active", "Completed", "Cancelled");
        statusFilterComboBox.setValue("All Statuses");
        
        // Date Range ComboBox
        dateRangeComboBox.getItems().addAll("All Time", "Last 30 Days", "Last 3 Months", "Last 6 Months", "This Year");
        dateRangeComboBox.setValue("All Time");
    }

    private void fetchCustomerId() {
        int currentUserId = ChangePasswordController.getCurrentUserId();
        System.out.println("🔧 MyRentals: Current User ID = " + currentUserId);
        
        String sql = "SELECT CustomerID, FullName FROM Customers WHERE UserID = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentCustomerId = rs.getInt("CustomerID");
                String customerName = rs.getString("FullName");
                System.out.println("✅ MyRentals: Found Customer ID = " + currentCustomerId + " (" + customerName + ")");
            } else {
                System.err.println("❌ MyRentals: No customer found for User ID = " + currentUserId);
                
                // Debug: Check if user exists at all
                String debugSql = "SELECT Username FROM Users WHERE UserID = ?";
                try (PreparedStatement debugStmt = conn.prepareStatement(debugSql)) {
                    debugStmt.setInt(1, currentUserId);
                    ResultSet debugRs = debugStmt.executeQuery();
                    if (debugRs.next()) {
                        String username = debugRs.getString("Username");
                        System.err.println("❌ MyRentals: User exists (" + username + ") but no linked customer found");
                        
                        // Set error message but don't return - let the auto-fix attempt run
                        statusLabel.setText("⚠️ Linking customer account...");
                        statusLabel.setStyle("-fx-text-fill: #FF9800;");
                    } else {
                        System.err.println("❌ MyRentals: User ID " + currentUserId + " does not exist in Users table");
                        statusLabel.setText("❌ User account not found. Please contact support.");
                        statusLabel.setStyle("-fx-text-fill: #f44336;");
                        return;
                    }
                } catch (SQLException debugE) {
                    System.err.println("❌ MyRentals: Debug query failed: " + debugE.getMessage());
                    statusLabel.setText("❌ Database error during debug: " + debugE.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #f44336;");
                    return;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ MyRentals: Error fetching customer ID: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("❌ Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    private void loadRentals() {
        rentalsContainer.getChildren().clear();
        
        if (currentCustomerId <= 0) {
            System.err.println("❌ MyRentals: Invalid customer ID = " + currentCustomerId);
            showNoRentalsMessage();
            return;
        }
        
        System.out.println("🔧 MyRentals: Loading rentals for Customer ID = " + currentCustomerId);

        String sql = "SELECT r.RentalID, r.CustomerID, r.CarID, r.RentDate, r.ReturnDate, r.TotalAmount, " +
                    "ISNULL(r.Status, 'Active') as Status, " +
                    "c.Brand, c.Model, c.PlateNumber, c.DailyRate " +
                    "FROM Rentals r " +
                    "JOIN Cars c ON r.CarID = c.CarID " +
                    "WHERE r.CustomerID = ? " +
                    "ORDER BY r.RentDate DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentCustomerId);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                int rentalId = rs.getInt("RentalID");
                String brand = rs.getString("Brand");
                String model = rs.getString("Model");
                String plateNumber = rs.getString("PlateNumber");
                LocalDate rentDate = rs.getDate("RentDate").toLocalDate();
                LocalDate returnDate = rs.getDate("ReturnDate").toLocalDate();
                double totalAmount = rs.getDouble("TotalAmount");
                String status = rs.getString("Status");
                
                System.out.println("✅ MyRentals: Found rental " + rentalId + " - " + brand + " " + model + " (" + status + ")");
                
                VBox rentalCard = createRentalCard(
                    rentalId, brand, model, plateNumber,
                    rentDate, returnDate, totalAmount, status
                );
                rentalsContainer.getChildren().add(rentalCard);
                count++;
            }

            System.out.println("✅ MyRentals: Loaded " + count + " rentals");
            updateRentalCount(count);

            if (count == 0) {
                System.out.println("⚠️ MyRentals: No rentals found for customer " + currentCustomerId);
                showNoRentalsMessage();
            }

        } catch (SQLException e) {
            System.err.println("❌ MyRentals: Error loading rentals: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("❌ Error loading rental history: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    private VBox createRentalCard(int rentalId, String brand, String model, String plateNumber,
                                 LocalDate rentDate, LocalDate returnDate, double totalAmount, String status) {
        VBox card = new VBox();
        card.setSpacing(15);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: white; " +
                     "-fx-border-radius: 12; " +
                     "-fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Header with car info and status
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        VBox carInfo = new VBox(5);
        Label carLabel = new Label("🚗 " + brand + " " + model);
        carLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label plateLabel = new Label("Plaka: " + plateNumber);
        plateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        carInfo.getChildren().addAll(carLabel, plateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(getStatusText(status));
        statusBadge.setStyle(getStatusStyle(status));
        statusBadge.setPadding(new Insets(5, 15, 5, 15));

        header.getChildren().addAll(carInfo, spacer, statusBadge);

        // Rental details
        HBox details = new HBox(30);
        details.setAlignment(Pos.CENTER_LEFT);

        VBox dateInfo = new VBox(5);
        Label dateLabel = new Label("📅 Rental Date");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #666;");
        
        Label dateValue = new Label(rentDate.format(dateFormat) + " - " + returnDate.format(dateFormat));
        dateValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        
        dateInfo.getChildren().addAll(dateLabel, dateValue);

        VBox priceInfo = new VBox(5);
        Label priceLabel = new Label("💰 Total Amount");
        priceLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #666;");
        
        Label priceValue = new Label("₺" + currencyFormat.format(totalAmount));
        priceValue.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");
        
        priceInfo.getChildren().addAll(priceLabel, priceValue);

        VBox durationInfo = new VBox(5);
        Label durationLabel = new Label("⏱️ Duration");
        durationLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #666;");
        
        long days = java.time.temporal.ChronoUnit.DAYS.between(rentDate, returnDate);
        Label durationValue = new Label(days + " days");
        durationValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        
        durationInfo.getChildren().addAll(durationLabel, durationValue);

        details.getChildren().addAll(dateInfo, priceInfo, durationInfo);

        // Action buttons (if needed)
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if ("Active".equals(status)) {
            Button cancelButton = new Button("❌ Cancel");
            cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                                "-fx-font-size: 12px; -fx-padding: 8 16; " +
                                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
            cancelButton.setOnAction(e -> cancelRental(rentalId));
            actions.getChildren().add(cancelButton);
        }

        Button detailButton = new Button("📋 Details");
        detailButton.setStyle("-fx-background-color: #42A5F5; -fx-text-fill: white; " +
                            "-fx-font-size: 12px; -fx-padding: 8 16; " +
                            "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        detailButton.setOnAction(e -> showRentalDetails(rentalId));
        actions.getChildren().add(detailButton);

        card.getChildren().addAll(header, details, actions);
        return card;
    }

    private String getStatusText(String status) {
        switch (status) {
            case "Active": return "🟢 Active";
            case "Completed": return "✅ Completed";
            case "Cancelled": return "❌ Cancelled";
            default: return "❓ Unknown";
        }
    }

    private String getStatusStyle(String status) {
        String baseStyle = "-fx-font-size: 12px; -fx-font-weight: bold; " +
                          "-fx-border-radius: 15; -fx-background-radius: 15;";
        
        switch (status) {
            case "Active": 
                return baseStyle + " -fx-background-color: #E8F5E8; -fx-text-fill: #2E7D32;";
            case "Completed": 
                return baseStyle + " -fx-background-color: #E3F2FD; -fx-text-fill: #1976D2;";
            case "Cancelled": 
                return baseStyle + " -fx-background-color: #FFEBEE; -fx-text-fill: #D32F2F;";
            default: 
                return baseStyle + " -fx-background-color: #F5F5F5; -fx-text-fill: #666;";
        }
    }

    private void cancelRental(int rentalId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Rental Cancellation");
        alert.setHeaderText("Are you sure you want to cancel this rental?");
        alert.setContentText("This action cannot be undone.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            // Use RentalDAO.cancelRental method which properly updates both rental and car status
            boolean success = com.kerem.ordersystem.carrentalsystem.database.RentalDAO.cancelRental(rentalId);
            
            if (success) {
                statusLabel.setText("✅ Rental successfully cancelled and car returned to available status.");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                loadRentals(); // Refresh the list
            } else {
                statusLabel.setText("❌ Rental could not be cancelled. Please try again.");
                statusLabel.setStyle("-fx-text-fill: #f44336;");
            }
        }
    }

    private void showRentalDetails(int rentalId) {
        // Implementation for showing rental details
        statusLabel.setText("ℹ️ Rental details feature will be added soon.");
        statusLabel.setStyle("-fx-text-fill: #42A5F5;");
    }

    private void showNoRentalsMessage() {
        VBox noRentalsBox = new VBox(20);
        noRentalsBox.setAlignment(Pos.CENTER);
        noRentalsBox.setPadding(new Insets(50));
        noRentalsBox.setStyle("-fx-background-color: white; " +
                             "-fx-border-radius: 12; " +
                             "-fx-background-radius: 12; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label icon = new Label("📋");
        icon.setStyle("-fx-font-size: 64px;");

        Label message = new Label("You don't have any rental history yet");
        message.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #666;");

        Label subMessage = new Label("Return to the main page to make your first car rental");
        subMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");

        Button goToRentalsButton = new Button("🚗 Rent a Car");
        goToRentalsButton.setStyle("-fx-background-color: linear-gradient(to right, #42A5F5, #1976D2); " +
                                  "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                                  "-fx-padding: 15 30; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;");
        goToRentalsButton.setOnAction(e -> handleBack());

        noRentalsBox.getChildren().addAll(icon, message, subMessage, goToRentalsButton);
        rentalsContainer.getChildren().add(noRentalsBox);
    }

    private void updateRentalCount(int count) {
        rentalCountLabel.setText(count + " rentals found");
    }

    @FXML
    private void handleFilter() {
        // Implementation for filtering rentals
        loadRentals();
        statusLabel.setText("🔍 Filter applied.");
        statusLabel.setStyle("-fx-text-fill: #42A5F5;");
    }

    @FXML
    private void handleRefresh() {
        loadRentals();
        statusLabel.setText("🔄 Rental list refreshed.");
        statusLabel.setStyle("-fx-text-fill: #4CAF50;");
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/customer-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rentalsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Customer Dashboard");
        } catch (IOException e) {
            statusLabel.setText("❌ Dashboard opening error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    /**
     * Attempt to fix missing user-customer links by matching usernames with customer names
     */
    private void attemptToFixUserCustomerLink() {
        int currentUserId = ChangePasswordController.getCurrentUserId();
        System.out.println("🔧 MyRentals: Attempting to fix link for User ID = " + currentUserId);
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // First, get the username of current user
            String getUserSql = "SELECT Username FROM Users WHERE UserID = ?";
            String username = null;
            
            try (PreparedStatement getUserStmt = conn.prepareStatement(getUserSql)) {
                getUserStmt.setInt(1, currentUserId);
                ResultSet userRs = getUserStmt.executeQuery();
                if (userRs.next()) {
                    username = userRs.getString("Username");
                    System.out.println("🔧 MyRentals: Current username = " + username);
                }
            }
            
            if (username != null) {
                // Try multiple matching strategies to find the customer
                String[] searchPatterns = {
                    "%" + username.toLowerCase() + "%",           // Contains username
                    username.toLowerCase() + "%",                 // Starts with username  
                    "%" + username.toLowerCase(),                 // Ends with username
                    username.toLowerCase()                        // Exact match
                };
                
                boolean customerFound = false;
                
                for (String pattern : searchPatterns) {
                    if (customerFound) break;
                    
                    String findCustomerSql = "SELECT CustomerID, FullName FROM Customers WHERE LOWER(FullName) LIKE ? AND UserID IS NULL";
                    
                    try (PreparedStatement findStmt = conn.prepareStatement(findCustomerSql)) {
                        findStmt.setString(1, pattern);
                        ResultSet customerRs = findStmt.executeQuery();
                        
                        if (customerRs.next()) {
                            int customerId = customerRs.getInt("CustomerID");
                            String customerName = customerRs.getString("FullName");
                            
                            System.out.println("🔧 MyRentals: Found unlinked customer: " + customerName + " (ID: " + customerId + ") using pattern: " + pattern);
                            
                                                    // Link the customer to the user
                        String linkSql = "UPDATE Customers SET UserID = ? WHERE CustomerID = ?";
                        try (PreparedStatement linkStmt = conn.prepareStatement(linkSql)) {
                            linkStmt.setInt(1, currentUserId);
                            linkStmt.setInt(2, customerId);
                            
                            int rowsUpdated = linkStmt.executeUpdate();
                            if (rowsUpdated > 0) {
                                System.out.println("✅ MyRentals: Successfully linked Customer " + customerId + " (" + customerName + ") to User " + currentUserId + " (" + username + ")");
                                
                                // Update status
                                if (statusLabel != null) {
                                    statusLabel.setText("✅ Account linked successfully! Refreshing...");
                                    statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                                }
                                customerFound = true;
                            } else {
                                System.err.println("❌ MyRentals: Failed to link customer to user");
                            }
                        } catch (SQLException linkError) {
                            if (linkError.getMessage().contains("fn_securitypredicate") || 
                                linkError.getMessage().contains("security policy")) {
                                System.err.println("⚠️ MyRentals: RLS policy blocking update. Please use Admin panel to fix user links.");
                                if (statusLabel != null) {
                                    statusLabel.setText("⚠️ Security policy active. Please contact admin to link account.");
                                    statusLabel.setStyle("-fx-text-fill: #FF9800;");
                                }
                            } else {
                                System.err.println("❌ MyRentals: Error linking customer: " + linkError.getMessage());
                                throw linkError; // Re-throw if it's not an RLS issue
                            }
                        }
                        }
                    }
                }
                
                if (!customerFound) {
                    System.out.println("⚠️ MyRentals: No unlinked customer found matching username: " + username);
                    
                    // Show all unlinked customers for debugging
                    String showAllUnlinkedSql = "SELECT CustomerID, FullName, Phone, Email FROM Customers WHERE UserID IS NULL ORDER BY CustomerID";
                    try (PreparedStatement showStmt = conn.prepareStatement(showAllUnlinkedSql);
                         ResultSet allRs = showStmt.executeQuery()) {
                        
                        System.out.println("🔍 MyRentals: All unlinked customers:");
                        while (allRs.next()) {
                            System.out.println("  • ID: " + allRs.getInt("CustomerID") + 
                                             ", Name: " + allRs.getString("FullName") + 
                                             ", Phone: " + allRs.getString("Phone") + 
                                             ", Email: " + allRs.getString("Email"));
                        }
                    }
                    
                    // Check if there are any customers with this name already linked
                    String checkLinkedSql = "SELECT CustomerID, FullName, UserID FROM Customers WHERE LOWER(FullName) LIKE ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkLinkedSql)) {
                        checkStmt.setString(1, "%" + username.toLowerCase() + "%");
                        ResultSet linkedRs = checkStmt.executeQuery();
                        
                        while (linkedRs.next()) {
                            int custId = linkedRs.getInt("CustomerID");
                            String custName = linkedRs.getString("FullName");
                            int linkedUserId = linkedRs.getInt("UserID");
                            System.out.println("ℹ️ MyRentals: Found customer " + custName + " (ID: " + custId + ") already linked to User " + linkedUserId);
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ MyRentals: Error attempting to fix user-customer link: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
