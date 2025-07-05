package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.RentalDAO;
import com.kerem.ordersystem.carrentalsystem.database.CustomerDAO;
import com.kerem.ordersystem.carrentalsystem.util.StageUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RentalHistoryController implements Initializable {

    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> customerFilterComboBox;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;

    @FXML private Label completedCountLabel;
    @FXML private Label cancelledCountLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalRecordsLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<RentalHistoryRow> rentalHistoryTable;
    @FXML private TableColumn<RentalHistoryRow, String> rentalIdColumn;
    @FXML private TableColumn<RentalHistoryRow, String> customerNameColumn;
    @FXML private TableColumn<RentalHistoryRow, String> carInfoColumn;
    @FXML private TableColumn<RentalHistoryRow, String> plateNumberColumn;
    @FXML private TableColumn<RentalHistoryRow, String> rentDateColumn;
    @FXML private TableColumn<RentalHistoryRow, String> returnDateColumn;
    @FXML private TableColumn<RentalHistoryRow, String> durationColumn;
    @FXML private TableColumn<RentalHistoryRow, String> totalAmountColumn;
    @FXML private TableColumn<RentalHistoryRow, String> statusColumn;
    @FXML private TableColumn<RentalHistoryRow, String> actionsColumn;

    private List<Map<String, Object>> allRentals;
    private List<Map<String, Object>> filteredRentals;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("🔧 RentalHistoryController initialize() started...");
            
            // Initialize with safe defaults
            allRentals = new ArrayList<>();
            filteredRentals = new ArrayList<>();
            
            if (statusLabel != null) {
                statusLabel.setText("🔄 Initializing rental history...");
            }
            
            System.out.println("🔧 Setting up table...");
            setupTable();
            
            System.out.println("🔧 Setting up filters...");
            setupFilters();
            
            System.out.println("🔧 Loading rental history...");
            loadRentalHistory();
            
            System.out.println("🔧 Updating statistics...");
            updateStatistics();
            
            System.out.println("✅ RentalHistoryController initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error in RentalHistoryController initialize: " + e.getMessage());
            e.printStackTrace();
            
            // Initialize with empty data to prevent crashes
            if (allRentals == null) allRentals = new ArrayList<>();
            if (filteredRentals == null) filteredRentals = new ArrayList<>();
            
            if (statusLabel != null) {
                statusLabel.setText("❌ Initialization error: " + e.getMessage());
            }
            
            // Show empty table instead of crashing
            if (rentalHistoryTable != null) {
                rentalHistoryTable.setItems(FXCollections.observableArrayList());
            }
        }
    }

    private void setupTable() {
        // Set up table columns
        rentalIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRentalId()));
        customerNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
        carInfoColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCarInfo()));
        plateNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlateNumber()));
        rentDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRentDate()));
        returnDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReturnDate()));
        durationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDuration()));
        totalAmountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTotalAmount()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        
        // Actions column with styled buttons
        actionsColumn.setCellFactory(param -> new TableCell<RentalHistoryRow, String>() {
            private final Button viewButton = new Button("👁️ View");
            private final HBox buttonBox = new HBox(5);

            {
                viewButton.setStyle(
                    "-fx-background-color: #2196F3; " +
                    "-fx-text-fill: white; " +
                    "-fx-border-radius: 4; " +
                    "-fx-background-radius: 4; " +
                    "-fx-padding: 4 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-font-size: 11px;"
                );
                
                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.getChildren().add(viewButton);
                
                viewButton.setOnAction(e -> {
                    RentalHistoryRow rental = getTableView().getItems().get(getIndex());
                    handleViewRentalDetails(rental);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
    }

    private void setupFilters() {
        // Status filter
        ObservableList<String> statuses = FXCollections.observableArrayList();
        statuses.add("All Statuses");
        statuses.add("Completed");
        statuses.add("Cancelled");
        statusFilterComboBox.setItems(statuses);
        statusFilterComboBox.setValue("All Statuses");

        // Customer filter - will be populated when rentals are loaded
        customerFilterComboBox.setValue("All Customers");
    }

    private void loadRentalHistory() {
        try {
            System.out.println("🔧 loadRentalHistory() started...");
            
            if (statusLabel != null) {
                statusLabel.setText("🔄 Loading rental history...");
            }
            
            System.out.println("🔧 Calling RentalDAO.getAllRentals()...");
            // Get all rentals first - with fallback to empty list
            List<Map<String, Object>> allRentalData = new ArrayList<>();
            try {
                allRentalData = RentalDAO.getAllRentals();
                if (allRentalData == null) {
                    System.err.println("❌ RentalDAO.getAllRentals() returned null!");
                    allRentalData = new ArrayList<>();
                }
                System.out.println("🔧 Retrieved " + allRentalData.size() + " total rentals");
            } catch (Exception e) {
                System.err.println("❌ Error calling RentalDAO.getAllRentals(): " + e.getMessage());
                e.printStackTrace();
                System.out.println("🔧 Using empty list as fallback");
                allRentalData = new ArrayList<>();
            }
            
            // Initialize lists if null
            if (allRentals == null) allRentals = new ArrayList<>();
            if (filteredRentals == null) filteredRentals = new ArrayList<>();
            
            // Filter for completed and cancelled rentals only
            allRentals = allRentalData.stream()
                .filter(rental -> {
                    try {
                        String status = (String) rental.get("Status");
                        return "Completed".equals(status) || "Cancelled".equals(status);
                    } catch (Exception e) {
                        System.err.println("❌ Error filtering rental: " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
            
            System.out.println("🔧 Filtered to " + allRentals.size() + " completed/cancelled rentals");
            filteredRentals = new ArrayList<>(allRentals);
            
            System.out.println("🔧 Setting up customer filter...");
            // Populate customer filter
            try {
                setupCustomerFilter();
            } catch (Exception e) {
                System.err.println("❌ Error setting up customer filter: " + e.getMessage());
            }
            
            System.out.println("🔧 Displaying rentals in table...");
            // Display rentals in table
            try {
                displayRentals();
            } catch (Exception e) {
                System.err.println("❌ Error displaying rentals: " + e.getMessage());
                // Show empty table
                if (rentalHistoryTable != null) {
                    rentalHistoryTable.setItems(FXCollections.observableArrayList());
                }
            }
            
            if (statusLabel != null) {
                statusLabel.setText("✅ Rental history loaded successfully - " + allRentals.size() + " records");
            }
            System.out.println("✅ loadRentalHistory() completed successfully!");
            
        } catch (Exception e) {
            if (statusLabel != null) {
                statusLabel.setText("❌ Error loading rental history: " + e.getMessage());
            }
            System.err.println("❌ Error loading rental history: " + e.getMessage());
            e.printStackTrace();
            
            // Ensure lists are initialized
            if (allRentals == null) allRentals = new ArrayList<>();
            if (filteredRentals == null) filteredRentals = new ArrayList<>();
            
            // Show empty table
            if (rentalHistoryTable != null) {
                rentalHistoryTable.setItems(FXCollections.observableArrayList());
            }
        }
    }

    private void setupCustomerFilter() {
        ObservableList<String> customers = FXCollections.observableArrayList();
        customers.add("All Customers");
        
        // Get unique customers from rentals
        allRentals.stream()
            .map(rental -> (String) rental.get("CustomerName"))
            .distinct()
            .sorted()
            .forEach(customers::add);
            
        customerFilterComboBox.setItems(customers);
    }

    private void displayRentals() {
        ObservableList<RentalHistoryRow> tableData = FXCollections.observableArrayList();
        
        for (Map<String, Object> rental : filteredRentals) {
            try {
                String rentalId = String.valueOf(rental.get("RentalID"));
                String customerName = (String) rental.get("CustomerName");
                String carBrand = (String) rental.get("CarBrand");
                String carModel = (String) rental.get("CarModel");
                String carInfo = carBrand + " " + carModel;
                String plateNumber = (String) rental.get("PlateNumber");
                String status = (String) rental.get("Status");
                
                // Format dates
                Object rentDateObj = rental.get("RentDate");
                Object returnDateObj = rental.get("ReturnDate");
                String rentDate = formatDate(rentDateObj);
                String returnDate = formatDate(returnDateObj);
                
                // Calculate duration
                String duration = calculateDuration(rentDateObj, returnDateObj);
                
                // Format amount
                Object amountObj = rental.get("TotalAmount");
                String totalAmount = formatCurrency(amountObj);
                
                RentalHistoryRow row = new RentalHistoryRow(
                    rentalId, customerName, carInfo, plateNumber,
                    rentDate, returnDate, duration, totalAmount, status
                );
                
                tableData.add(row);
                
            } catch (Exception e) {
                System.err.println("Error processing rental record: " + e.getMessage());
            }
        }
        
        rentalHistoryTable.setItems(tableData);
        totalRecordsLabel.setText("Total: " + tableData.size() + " records");
    }

    private String formatDate(Object dateObj) {
        if (dateObj == null) return "N/A";
        try {
            if (dateObj instanceof java.sql.Date) {
                return ((java.sql.Date) dateObj).toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } else if (dateObj instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } else {
                return dateObj.toString();
            }
        } catch (Exception e) {
            return "Invalid Date";
        }
    }

    private String calculateDuration(Object startObj, Object endObj) {
        try {
            if (startObj == null || endObj == null) return "N/A";
            
            LocalDate startDate = null;
            LocalDate endDate = null;
            
            if (startObj instanceof java.sql.Date) {
                startDate = ((java.sql.Date) startObj).toLocalDate();
            }
            if (endObj instanceof java.sql.Date) {
                endDate = ((java.sql.Date) endObj).toLocalDate();
            } else if (endObj instanceof java.sql.Timestamp) {
                endDate = ((java.sql.Timestamp) endObj).toLocalDateTime().toLocalDate();
            }
            
            if (startDate != null && endDate != null) {
                long days = ChronoUnit.DAYS.between(startDate, endDate);
                return String.valueOf(Math.max(1, days)); // Minimum 1 day
            }
            
            return "N/A";
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String formatCurrency(Object amountObj) {
        if (amountObj == null) return "0 TL";
        try {
            double amount = 0;
            if (amountObj instanceof Number) {
                amount = ((Number) amountObj).doubleValue();
            } else {
                amount = Double.parseDouble(amountObj.toString());
            }
            return currencyFormat.format(amount) + " TL";
        } catch (Exception e) {
            return "0 TL";
        }
    }

    private void updateStatistics() {
        try {
            System.out.println("🔧 updateStatistics() started...");
            if (filteredRentals == null) {
                System.out.println("🔧 filteredRentals is null, using default values");
                if (completedCountLabel != null) completedCountLabel.setText("0");
                if (cancelledCountLabel != null) cancelledCountLabel.setText("0");
                if (totalRevenueLabel != null) totalRevenueLabel.setText("0 TL");
                return;
            }
            
            int completedCount = 0;
            int cancelledCount = 0;
            double totalRevenue = 0;
            
            for (Map<String, Object> rental : filteredRentals) {
                try {
                    String status = (String) rental.get("Status");
                    
                    if ("Completed".equals(status)) {
                        completedCount++;
                        // Add to revenue only for completed rentals
                        Object amountObj = rental.get("TotalAmount");
                        if (amountObj instanceof Number) {
                            totalRevenue += ((Number) amountObj).doubleValue();
                        }
                    } else if ("Cancelled".equals(status)) {
                        cancelledCount++;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error processing rental for statistics: " + e.getMessage());
                }
            }
            
            // Update statistics labels with null checks
            if (completedCountLabel != null) {
                completedCountLabel.setText(String.valueOf(completedCount));
            }
            if (cancelledCountLabel != null) {
                cancelledCountLabel.setText(String.valueOf(cancelledCount));
            }
            if (totalRevenueLabel != null) {
                totalRevenueLabel.setText(currencyFormat.format(totalRevenue) + " TL");
            }
            System.out.println("✅ updateStatistics() completed successfully");
        } catch (Exception e) {
            System.err.println("❌ Error in updateStatistics(): " + e.getMessage());
            e.printStackTrace();
            // Set default values on error with null checks
            if (completedCountLabel != null) completedCountLabel.setText("0");
            if (cancelledCountLabel != null) cancelledCountLabel.setText("0");
            if (totalRevenueLabel != null) totalRevenueLabel.setText("0 TL");
        }
    }

    @FXML
    private void handleApplyFilters() {
        try {
            statusLabel.setText("🔄 Applying filters...");
            
            filteredRentals = allRentals.stream()
                .filter(rental -> {
                    // Status filter
                    String statusFilter = statusFilterComboBox.getValue();
                    if (!"All Statuses".equals(statusFilter)) {
                        String rentalStatus = (String) rental.get("Status");
                        if (!statusFilter.equals(rentalStatus)) {
                            return false;
                        }
                    }
                    
                    // Customer filter
                    String customerFilter = customerFilterComboBox.getValue();
                    if (!"All Customers".equals(customerFilter)) {
                        String customerName = (String) rental.get("CustomerName");
                        if (!customerFilter.equals(customerName)) {
                            return false;
                        }
                    }
                    
                    // Date filters
                    LocalDate fromDate = dateFromPicker.getValue();
                    LocalDate toDate = dateToPicker.getValue();
                    
                    if (fromDate != null || toDate != null) {
                        Object rentDateObj = rental.get("RentDate");
                        if (rentDateObj instanceof java.sql.Date) {
                            LocalDate rentDate = ((java.sql.Date) rentDateObj).toLocalDate();
                            
                            if (fromDate != null && rentDate.isBefore(fromDate)) {
                                return false;
                            }
                            if (toDate != null && rentDate.isAfter(toDate)) {
                                return false;
                            }
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
            displayRentals();
            updateStatistics();
            
            statusLabel.setText("✅ Filters applied - " + filteredRentals.size() + " records found");
            
        } catch (Exception e) {
            statusLabel.setText("❌ Error applying filters: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearFilters() {
        statusFilterComboBox.setValue("All Statuses");
        customerFilterComboBox.setValue("All Customers");
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        
        filteredRentals = allRentals;
        displayRentals();
        updateStatistics();
        
        statusLabel.setText("🔄 Filters cleared - Showing all records");
    }

    @FXML
    private void handleRefreshData() {
        statusLabel.setText("🔄 Refreshing rental data...");
        
        try {
            // Clear current data
            if (allRentals != null) {
                allRentals.clear();
            }
            if (filteredRentals != null) {
                filteredRentals.clear();
            }
            rentalHistoryTable.getItems().clear();
            
            // Reload data from database
            loadRentalHistory();
            
            // Clear filters and show all data
            statusFilterComboBox.setValue("All Statuses");
            customerFilterComboBox.setValue("All Customers");
            dateFromPicker.setValue(null);
            dateToPicker.setValue(null);
            
            statusLabel.setText("✅ Data refreshed successfully - " + (allRentals != null ? allRentals.size() : 0) + " records loaded");
            
        } catch (Exception e) {
            statusLabel.setText("❌ Error refreshing data: " + e.getMessage());
            System.err.println("❌ Error refreshing rental data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Admin Dashboard");
        } catch (IOException e) {
            statusLabel.setText("❌ Dashboard could not be loaded: " + e.getMessage());
        }
    }

    private void handleViewRentalDetails(RentalHistoryRow rental) {
        try {
            createModernRentalDetailsDialog(rental);
        } catch (Exception e) {
            statusLabel.setText("❌ Error viewing rental details: " + e.getMessage());
        }
    }

    private void createModernRentalDetailsDialog(RentalHistoryRow rental) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("📋 Rental Details");
        dialog.setHeaderText(null);

        // Get dialog pane and apply modern styling
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E0E0E0; " +
            "-fx-border-radius: 15; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);"
        );

        // Main container
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // Header with rental ID and status
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setStyle(
            "-fx-background-color: linear-gradient(to right, #42A5F5, #1976D2); " +
            "-fx-padding: 20; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12;"
        );

        Label rentalIcon = new Label("📋");
        rentalIcon.setStyle("-fx-font-size: 32px;");

        VBox headerTextBox = new VBox(5);
        headerTextBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("RENTAL DETAILS");
        titleLabel.setStyle(
            "-fx-font-size: 20px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: white;"
        );

        Label rentalIdLabel = new Label("ID: " + rental.getRentalId());
        rentalIdLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: rgba(255,255,255,0.9);"
        );

        headerTextBox.getChildren().addAll(titleLabel, rentalIdLabel);

        // Status badge
        Label statusBadge = new Label(rental.getStatus().toUpperCase());
        statusBadge.setStyle(getStatusBadgeStyle(rental.getStatus()));
        statusBadge.setPadding(new Insets(8, 16, 8, 16));

        headerBox.getChildren().addAll(rentalIcon, headerTextBox, new Region(), statusBadge);
        HBox.setHgrow(headerBox.getChildren().get(2), Priority.ALWAYS);

        // Details cards container
        HBox cardsContainer = new HBox(20);
        cardsContainer.setAlignment(Pos.CENTER);

        // Customer card
        VBox customerCard = createDetailCard(
            "👤", "CUSTOMER INFO", 
            new String[]{"Name", "Phone"}, 
            new String[]{rental.getCustomerName(), "N/A"}, // Phone not available in this context
            "#E3F2FD", "#1976D2"
        );

        // Car card
        VBox carCard = createDetailCard(
            "🚗", "VEHICLE INFO",
            new String[]{"Car", "Plate"},
            new String[]{rental.getCarInfo(), rental.getPlateNumber()},
            "#E8F5E8", "#2E7D32"
        );

        cardsContainer.getChildren().addAll(customerCard, carCard);

        // Rental period card
        VBox periodCard = createDetailCard(
            "📅", "RENTAL PERIOD",
            new String[]{"Start Date", "End Date", "Duration"},
            new String[]{rental.getRentDate(), rental.getReturnDate(), rental.getDuration() + " days"},
            "#FFF3E0", "#F57C00"
        );

        // Financial card
        VBox financialCard = createDetailCard(
            "💰", "FINANCIAL INFO",
            new String[]{"Total Amount", "Status"},
            new String[]{rental.getTotalAmount(), rental.getStatus()},
            "#F3E5F5", "#7B1FA2"
        );

        // Second row of cards
        HBox cardsContainer2 = new HBox(20);
        cardsContainer2.setAlignment(Pos.CENTER);
        cardsContainer2.getChildren().addAll(periodCard, financialCard);

        // Add all components to main container
        mainContainer.getChildren().addAll(headerBox, cardsContainer, cardsContainer2);

        dialogPane.setContent(mainContainer);

        // Custom OK button
        ButtonType okButton = new ButtonType("✅ OK", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(okButton);

        // Style the OK button
        dialogPane.lookupButton(okButton).setStyle(
            "-fx-background-color: linear-gradient(to right, #42A5F5, #1976D2); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12 24; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(66,165,245,0.4), 10, 0, 0, 3);"
        );

        // Set dialog size
        dialog.setResizable(false);
        dialogPane.setPrefWidth(600);
        dialogPane.setPrefHeight(450);

        dialog.showAndWait();
    }

    private VBox createDetailCard(String icon, String title, String[] labels, String[] values, String bgColor, String accentColor) {
        VBox card = new VBox(15);
        card.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-border-color: " + accentColor + "; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );
        card.setPrefWidth(250);
        card.setMaxWidth(250);

        // Card header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: " + accentColor + ";"
        );

        headerBox.getChildren().addAll(iconLabel, titleLabel);

        // Card content
        VBox contentBox = new VBox(8);
        for (int i = 0; i < labels.length && i < values.length; i++) {
            VBox fieldBox = new VBox(3);
            
            Label fieldLabel = new Label(labels[i] + ":");
            fieldLabel.setStyle(
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #666;"
            );

            Label fieldValue = new Label(values[i]);
            fieldValue.setStyle(
                "-fx-font-size: 13px; " +
                "-fx-text-fill: #333; " +
                "-fx-font-weight: bold;"
            );
            fieldValue.setWrapText(true);

            fieldBox.getChildren().addAll(fieldLabel, fieldValue);
            contentBox.getChildren().add(fieldBox);
        }

        card.getChildren().addAll(headerBox, contentBox);
        return card;
    }

    private String getStatusBadgeStyle(String status) {
        String baseStyle = "-fx-font-size: 12px; -fx-font-weight: bold; " +
                          "-fx-border-radius: 15; -fx-background-radius: 15;";
        
        switch (status.toLowerCase()) {
            case "completed":
                return baseStyle + " -fx-background-color: #4CAF50; -fx-text-fill: white;";
            case "cancelled":
                return baseStyle + " -fx-background-color: #F44336; -fx-text-fill: white;";
            case "active":
                return baseStyle + " -fx-background-color: #FF9800; -fx-text-fill: white;";
            default:
                return baseStyle + " -fx-background-color: #9E9E9E; -fx-text-fill: white;";
        }
    }

    // Data class for table rows
    public static class RentalHistoryRow {
        private final String rentalId;
        private final String customerName;
        private final String carInfo;
        private final String plateNumber;
        private final String rentDate;
        private final String returnDate;
        private final String duration;
        private final String totalAmount;
        private final String status;

        public RentalHistoryRow(String rentalId, String customerName, String carInfo, String plateNumber,
                               String rentDate, String returnDate, String duration, String totalAmount, String status) {
            this.rentalId = rentalId;
            this.customerName = customerName;
            this.carInfo = carInfo;
            this.plateNumber = plateNumber;
            this.rentDate = rentDate;
            this.returnDate = returnDate;
            this.duration = duration;
            this.totalAmount = totalAmount;
            this.status = status;
        }

        // Getters
        public String getRentalId() { return rentalId; }
        public String getCustomerName() { return customerName; }
        public String getCarInfo() { return carInfo; }
        public String getPlateNumber() { return plateNumber; }
        public String getRentDate() { return rentDate; }
        public String getReturnDate() { return returnDate; }
        public String getDuration() { return duration; }
        public String getTotalAmount() { return totalAmount; }
        public String getStatus() { return status; }
    }
}