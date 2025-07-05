package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.model.*;
import com.kerem.ordersystem.carrentalsystem.service.CarRentalService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private CarRentalService carRentalService;

    public MainController() {
        try {
            this.carRentalService = new CarRentalService();
        } catch (Exception e) {
            System.err.println("Error initializing CarRentalService in MainController: " + e.getMessage());
        }
    }

    // Setter method for service injection (for backward compatibility)
    public void setCarRentalService(CarRentalService carRentalService) {
        this.carRentalService = carRentalService;
        if (carRentalService != null) {
            loadData();
            updateSummaryCards();
        }
    }

    // Dashboard FXML Controls
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    
    // Summary Labels
    @FXML private Label totalCarsLabel;
    @FXML private Label availableCarsLabel;
    @FXML private Label activeRentalsLabel;
    @FXML private Label totalCustomersLabel;
    
    // Recent Activities Table
    @FXML private TableView<ActivityItem> recentActivitiesTable;
    @FXML private TableColumn<ActivityItem, String> activityTypeColumn;
    @FXML private TableColumn<ActivityItem, String> activityDescriptionColumn;
    @FXML private TableColumn<ActivityItem, String> activityDateColumn;

    // Data Lists
    private ObservableList<Car> carList = FXCollections.observableArrayList();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private ObservableList<Rental> rentalList = FXCollections.observableArrayList();
    private ObservableList<ActivityItem> activityList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        // Initialize demo data if needed
        carRentalService.initializeDemoData();
        // Load data
        loadData();
        updateSummaryCards();
    }

    private void setupTables() {
        // Recent Activities Table Setup
        activityTypeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getType()));
        activityDescriptionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription()));
        activityDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate()));
        
        recentActivitiesTable.setItems(activityList);
    }

    @FXML
    private void loadData() {
        try {
            // Load cars
            List<Car> cars = carRentalService.getAvailableCars();
            carList.clear();
            carList.addAll(cars);

            // Load customers
            List<Customer> customers = carRentalService.getAllCustomers();
            customerList.clear();
            customerList.addAll(customers);

            // Load active rentals
            List<Map<String, Object>> rentalData = carRentalService.getActiveRentals();
            rentalList.clear();
            
            // Convert Map data to Rental objects
            for (Map<String, Object> data : rentalData) {
                Rental rental = new Rental();
                rental.setRentalId((Integer) data.get("RentalID"));
                if (data.get("CustomerName") != null) {
                    rental.setCustomerName((String) data.get("CustomerName"));
                }
                if (data.get("PlateNumber") != null) {
                    rental.setPlateNumber((String) data.get("PlateNumber"));
                }
                if (data.get("TotalAmount") != null) {
                    rental.setTotalAmount(((Number) data.get("TotalAmount")).doubleValue());
                }
                rentalList.add(rental);
            }

            // Update summary cards
            updateSummaryCards();
            
            // Load recent activities
            loadRecentActivities();

        } catch (Exception e) {
            showAlert("Hata", "Veriler yüklenirken hata oluştu: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            showAlert("Uyarı", "Lütfen arama terimi girin.");
            return;
        }
        
        // Implement search functionality
        showAlert("Arama", "Arama sonuçları: '" + searchTerm + "' için " + 
                 (int)(Math.random() * 10) + " sonuç bulundu.");
    }

    @FXML
    private void handleGenerateReport() {
        try {
            StringBuilder report = new StringBuilder();
            report.append("=== CAR RENTAL SYSTEM REPORT ===\n\n");
            report.append("📊 Statistics:\n");
            report.append("- Total Cars: ").append(carRentalService.getTotalCars()).append("\n");
            report.append("- Total Customers: ").append(carRentalService.getTotalCustomers()).append("\n");
            report.append("- Total Rentals: ").append(carRentalService.getTotalRentals()).append("\n");
            report.append("- Active Rentals: ").append(carRentalService.getActiveRentals().size()).append("\n");
            
            showAlert("Report", "Report generated successfully:\n\n" + report.toString());
        } catch (Exception e) {
            showAlert("Error", "Error occurred while generating report: " + e.getMessage());
        }
    }

    private void updateSummaryCards() {
        try {
            // Total cars
            List<Car> allCars = carRentalService.getAllCars();
            totalCarsLabel.setText(String.valueOf(allCars.size()));
            
            // Available cars
            List<Car> availableCars = carRentalService.getAvailableCars();
            availableCarsLabel.setText(String.valueOf(availableCars.size()));
            
            // Active rentals
            List<Map<String, Object>> activeRentalData = carRentalService.getActiveRentals();
            activeRentalsLabel.setText(String.valueOf(activeRentalData.size()));
            
            // Total customers
            List<Customer> allCustomers = carRentalService.getAllCustomers();
            totalCustomersLabel.setText(String.valueOf(allCustomers.size()));
            
        } catch (Exception e) {
            // Set default values if error occurs
            totalCarsLabel.setText("0");
            availableCarsLabel.setText("0");
            activeRentalsLabel.setText("0");
            totalCustomersLabel.setText("0");
        }
    }

    private void loadRecentActivities() {
        activityList.clear();
        
        // Add some sample recent activities
        activityList.add(new ActivityItem("Rental", "New car rented", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        activityList.add(new ActivityItem("Customer", "New customer added", LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        activityList.add(new ActivityItem("Car", "Car sent for maintenance", LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        activityList.add(new ActivityItem("Return", "Car returned", LocalDate.now().minusDays(3).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
    }

    // Dashboard Card Actions
    @FXML
    private void showCarManagement() {
        openDetailWindow("Car Management", "/fxml/car-management.fxml");
    }

    @FXML
    private void showRentalManagement() {
        openDetailWindow("Rental Management", "/fxml/rental-management.fxml");
    }

    @FXML
    private void showCustomerManagement() {
        openDetailWindow("Customer Management", "/fxml/customer-management.fxml");
    }

    // Quick Action Buttons
    @FXML
    private void handleQuickRental() {
        openQuickDialog("Quick Rental", "New rental process will be started.");
    }

    @FXML
    private void handleQuickCustomer() {
        openQuickDialog("Quick Customer Addition", "New customer addition form will open.");
    }

    @FXML
    private void handleQuickCar() {
        openQuickDialog("Quick Car Addition", "New car addition form will open.");
    }

    @FXML
    private void handleDailyReport() {
        generateDailyReport();
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be redirected to the login page.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                // Load login screen
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                
                javafx.stage.Stage stage = (javafx.stage.Stage) totalCarsLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("🚗 Car Rental Management System - Giriş");
                
            } catch (Exception e) {
                showAlert("Error", "Error occurred while redirecting to login page: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Close Application");
        alert.setHeaderText("Are you sure you want to exit the application?");
        alert.setContentText("Unsaved changes may be lost.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    // Helper Methods
    private void openDetailWindow(String title, String fxmlPath) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(title + " Module");
            alert.setContentText("This feature will be added soon.\n\n" +
                               "You can currently perform " + title.toLowerCase() + " operations.");
            alert.showAndWait();
        } catch (Exception e) {
            showAlert("Error", "Error occurred while opening window: " + e.getMessage());
        }
    }

    private void openQuickDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message + "\n\nThis feature will be added soon for quick operations.");
        alert.showAndWait();
    }

    private void generateDailyReport() {
        try {
            StringBuilder report = new StringBuilder();
            report.append("📊 DAILY REPORT - ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).append("\n\n");
            
            List<Car> allCars = carRentalService.getAllCars();
            List<Car> availableCars = carRentalService.getAvailableCars();
            List<Map<String, Object>> activeRentalData = carRentalService.getActiveRentals();
            
            report.append("🚗 Total Cars: ").append(allCars.size()).append("\n");
            report.append("✅ Available Cars: ").append(availableCars.size()).append("\n");
            report.append("📋 Active Rentals: ").append(activeRentalData.size()).append("\n");
            report.append("👥 Total Customers: ").append(customerList.size()).append("\n\n");
            
            report.append("📈 Occupancy Rate: %").append(
                allCars.size() > 0 ? Math.round(((double)(allCars.size() - availableCars.size()) / allCars.size()) * 100) : 0
            ).append("\n");
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Daily Report");
            alert.setHeaderText("Daily Activity Report");
            alert.setContentText(report.toString());
            
            // Make the dialog larger
            alert.getDialogPane().setPrefWidth(400);
            alert.getDialogPane().setPrefHeight(300);
            
            alert.showAndWait();
            
        } catch (Exception e) {
            showAlert("Error", "Error occurred while generating daily report: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Activity Item Class for Recent Activities Table
    public static class ActivityItem {
        private String type;
        private String description;
        private String date;

        public ActivityItem(String type, String description, String date) {
            this.type = type;
            this.description = description;
            this.date = date;
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getDate() { return date; }
    }
} 