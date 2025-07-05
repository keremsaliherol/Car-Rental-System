package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.CarDAO;
import com.kerem.ordersystem.carrentalsystem.database.CustomerDAO;
import com.kerem.ordersystem.carrentalsystem.database.RentalDAO;
import com.kerem.ordersystem.carrentalsystem.database.ActivityDAO;
import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import com.kerem.ordersystem.carrentalsystem.model.Car;
import com.kerem.ordersystem.carrentalsystem.model.Customer;
import com.kerem.ordersystem.carrentalsystem.model.Rental;
import com.kerem.ordersystem.carrentalsystem.service.UserAccountService;
import com.kerem.ordersystem.carrentalsystem.service.EmailService;
import com.kerem.ordersystem.carrentalsystem.util.StageUtils;
import javafx.application.Platform;
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
import javafx.scene.control.ButtonBar.ButtonData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.List;

public class CustomerServiceController implements Initializable {

    // Seçilen araç UI elementleri
    @FXML private VBox selectedCarCard;
    @FXML private VBox noCarSelectedWarning;
    @FXML private VBox carImageContainer;
    @FXML private Label selectedCarTitle;
    @FXML private Label selectedCarPlate;
    @FXML private Label selectedCarYear;
    @FXML private Label selectedCarCategory;
    @FXML private Label selectedCarFuel;
    @FXML private Label selectedCarRate;
    @FXML private Label selectedCarStatus;

    @FXML private ComboBox<CustomerComboItem> customerComboBox;
    @FXML private TextField customerNameField;
    @FXML private TextField customerPhoneField;
    @FXML private TextField customerEmailField;
    @FXML private TextArea customerAddressArea;
    @FXML private TextField driverLicenseField;
    @FXML private DatePicker birthDatePicker;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label totalAmountLabel;
    @FXML private Label rentalDaysLabel;

    @FXML private Button createRentalButton;
    @FXML private Label statusLabel;

    // User Registration UI elements
    @FXML private TabPane mainTabPane;
    @FXML private TextField regFullNameField;
    @FXML private TextField regUsernameField;
    @FXML private TextField regEmailField;
    @FXML private TextField regPhoneField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmPasswordField;
    @FXML private TextField regDriverLicenseField;
    @FXML private DatePicker regBirthDatePicker;
    @FXML private TextArea regAddressArea;
    @FXML private Button createUserButton;
    @FXML private Label registrationStatusLabel;

    private RentalDAO rentalDAO = new RentalDAO();
    private Car selectedCar = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔧 CustomerServiceController initialize started...");
        
        try {
            System.out.println("🔧 Setting up date picker listeners...");
            setupDatePickerListeners();
            
            System.out.println("🔧 Setting up phone field validation...");
            setupPhoneFieldValidation();
            
            System.out.println("🔧 Updating selected car display...");
            updateSelectedCarDisplay();
            
            System.out.println("🔧 Ensuring database schema is up to date...");
            DatabaseManager.getInstance().createCustomersTableIfNotExists();
            
            System.out.println("🔧 Setting up customer ComboBox...");
            setupCustomerComboBox();
            
            System.out.println("✅ CustomerServiceController initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error initializing CustomerServiceController: " + e.getMessage());
            e.printStackTrace();
            
            // Set safe defaults
            if (statusLabel != null) {
                statusLabel.setText("❌ Initialization error: " + e.getMessage());
            }
        }
    }

    private void updateSelectedCarDisplay() {
        if (selectedCar == null) {
            // Show warning when no car is selected
            selectedCarCard.setVisible(false);
            noCarSelectedWarning.setVisible(true);
            createRentalButton.setDisable(true);
            statusLabel.setText("⚠️ Please select a car");
        } else {
            // Show selected car information
            selectedCarCard.setVisible(true);
            noCarSelectedWarning.setVisible(false);
            
            selectedCarTitle.setText(selectedCar.getBrand() + " " + selectedCar.getModel());
            selectedCarPlate.setText("Plate: " + selectedCar.getPlateNumber());
            selectedCarYear.setText(String.valueOf(selectedCar.getYear()));
            selectedCarCategory.setText(selectedCar.getCategoryName());
            selectedCarFuel.setText(extractFuelType(selectedCar.getDescription()));
            selectedCarRate.setText("₺" + String.format("%.0f", selectedCar.getDailyRate()));
            
            // Update car image
            updateCarImage();
            
            // Update status badge
            System.out.println("🔍 Car status from model: '" + selectedCar.getStatus() + "'");
            String statusText = getStatusText(selectedCar.getStatus());
            System.out.println("🔍 Converted status text: '" + statusText + "'");
            selectedCarStatus.setText(statusText);
            if ("AVAILABLE".equals(statusText)) {
                selectedCarStatus.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20;");
            } else {
                selectedCarStatus.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20;");
            }
            
            calculateTotal();
            updateRentalButtonState(); // Check if both car and customer are selected
            statusLabel.setText("✅ " + selectedCar.getBrand() + " " + selectedCar.getModel() + " (" + selectedCar.getPlateNumber() + ") selected");
        }
    }

    private void updateCarImage() {
        if (selectedCar == null || carImageContainer == null) {
            System.out.println("⚠️ updateCarImage: selectedCar or carImageContainer is null");
            return;
        }
        
        System.out.println("🖼️ Updating car image for: " + selectedCar.getBrand() + " " + selectedCar.getModel());
        System.out.println("   Image path: " + selectedCar.getImagePath());
        
        // Mevcut içeriği temizle
        carImageContainer.getChildren().clear();
        
        try {
            ImageView carImageView = new ImageView();
            
            // Container boyutlarını al ve padding çıkar
            double containerWidth = carImageContainer.getWidth() > 0 ? carImageContainer.getWidth() - 40 : 220;
            double containerHeight = carImageContainer.getHeight() > 0 ? carImageContainer.getHeight() - 40 : 140;
            
            System.out.println("   Container size: " + containerWidth + "x" + containerHeight);
            
            // Resmi container'a tam sığacak şekilde ayarla
            carImageView.setFitWidth(containerWidth);
            carImageView.setFitHeight(containerHeight);
            carImageView.setPreserveRatio(true);
            carImageView.setSmooth(true);
            carImageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
            
            Image carImg = null;
            if (selectedCar.getImagePath() != null && !selectedCar.getImagePath().isEmpty()) {
                try {
                    // Try loading as resource first
                    String resourcePath = selectedCar.getImagePath();
                    System.out.println("   Trying to load image from: " + resourcePath);
                    
                    // Use getClass().getResourceAsStream() for better resource loading
                    if (getClass().getResourceAsStream(resourcePath) != null) {
                        carImg = new Image(getClass().getResourceAsStream(resourcePath));
                        System.out.println("✅ Image loaded successfully from resource stream");
                    } else {
                        // Fallback to direct path loading
                        carImg = new Image(resourcePath, true);
                        System.out.println("   Trying direct path loading...");
                    }
                    
                    if (carImg != null && carImg.isError()) {
                        System.out.println("⚠️ Image loaded but has error: " + carImg.getException());
                        carImg = null;
                    }
                    
                } catch (Exception e) {
                    System.out.println("⚠️ Could not load car image from path: " + selectedCar.getImagePath());
                    System.out.println("   Error: " + e.getMessage());
                    carImg = null;
                }
            } else {
                System.out.println("⚠️ No image path provided for car");
            }
            
            if (carImg == null) {
                System.out.println("   Using fallback emoji display");
                // Fallback: Araç tipine göre emoji göster
                Label carEmoji = new Label("🚗");
                carEmoji.setStyle("-fx-font-size: 48px;");
                
                Label carLabel = new Label("Car Image");
                carLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
                
                VBox fallbackBox = new VBox(5);
                fallbackBox.setAlignment(Pos.CENTER);
                fallbackBox.getChildren().addAll(carEmoji, carLabel);
                
                carImageContainer.getChildren().add(fallbackBox);
            } else {
                System.out.println("✅ Setting image to ImageView");
                carImageView.setImage(carImg);
                carImageContainer.getChildren().add(carImageView);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error updating car image: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback göster
            Label carEmoji = new Label("🚗");
            carEmoji.setStyle("-fx-font-size: 48px;");
            carImageContainer.getChildren().add(carEmoji);
        }
    }

    private String extractFuelType(String description) {
        if (description == null) return "Unknown";
        
        String lowerDesc = description.toLowerCase();
        if (lowerDesc.contains("benzin") || lowerDesc.contains("gasoline") || lowerDesc.contains("petrol")) {
            return "Gasoline";
        } else if (lowerDesc.contains("dizel") || lowerDesc.contains("diesel")) {
            return "Diesel";
        } else if (lowerDesc.contains("elektrik") || lowerDesc.contains("electric")) {
            return "Electric";
        } else if (lowerDesc.contains("hibrit") || lowerDesc.contains("hybrid")) {
            return "Hybrid";
        }
        return "Unknown";
    }

    private String translateFuelType(String turkishFuelType) {
        if (turkishFuelType == null) return "Unknown";
        
        switch (turkishFuelType.toLowerCase()) {
            case "benzin":
                return "Gasoline";
            case "dizel":
                return "Diesel";
            case "elektrik":
                return "Electric";
            case "hibrit":
                return "Hybrid";
            default:
                return turkishFuelType; // Return as-is if not found
        }
    }

    private String getStatusText(String status) {
        if (status == null) return "UNKNOWN";
        
        // Handle both numeric IDs and status names
        switch (status.toLowerCase()) {
            case "1":
            case "available":
                return "AVAILABLE";
            case "2":
            case "rented":
                return "RENTED";
            case "3":
            case "maintenance":
                return "MAINTENANCE";
            case "4":
            case "deleted":
                return "DELETED";
            default:
                System.out.println("⚠️ Unknown car status: " + status);
                return "UNKNOWN";
        }
    }

    private void setupDatePickerListeners() {
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
    }

    private void setupPhoneFieldValidation() {
        customerPhoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                // Sadece rakamları al
                String digitsOnly = newValue.replaceAll("[^0-9]", "");
                
                // Türk telefon numarası formatına uygun mu kontrol et
                if (digitsOnly.length() <= 11) {
                    String formatted = formatPhoneNumber(digitsOnly);
                    
                    // Eğer format değiştiyse, güncelle
                    if (!formatted.equals(newValue)) {
                        Platform.runLater(() -> {
                            customerPhoneField.setText(formatted);
                            customerPhoneField.positionCaret(formatted.length());
                        });
                    }
                } else {
                    // 11 rakamdan fazla girişi engelle
                    Platform.runLater(() -> {
                        customerPhoneField.setText(oldValue);
                        customerPhoneField.positionCaret(oldValue != null ? oldValue.length() : 0);
                    });
                }
            }
        });
    }

    private String formatPhoneNumber(String digitsOnly) {
        if (digitsOnly.length() == 0) return "";
        if (digitsOnly.length() <= 4) return digitsOnly;
        if (digitsOnly.length() <= 7) return digitsOnly.substring(0, 4) + " " + digitsOnly.substring(4);
        if (digitsOnly.length() <= 9) return digitsOnly.substring(0, 4) + " " + digitsOnly.substring(4, 7) + " " + digitsOnly.substring(7);
        return digitsOnly.substring(0, 4) + " " + digitsOnly.substring(4, 7) + " " + digitsOnly.substring(7, 9) + " " + digitsOnly.substring(9);
    }

    private void calculateTotal() {
        if (selectedCar == null || startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            totalAmountLabel.setText("₺0.00");
            rentalDaysLabel.setText("0 days");
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (endDate.isBefore(startDate)) {
            totalAmountLabel.setText("₺0.00");
            rentalDaysLabel.setText("Invalid dates");
            return;
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double total = days * selectedCar.getDailyRate();

        rentalDaysLabel.setText(days + " day" + (days > 1 ? "s" : ""));
        totalAmountLabel.setText("₺" + String.format("%.2f", total));
    }

    @FXML
    private void handleCreateRental() {
        System.out.println("🚗 Creating rental...");
        
        if (!validateInputs()) {
            return;
        }

        try {
            CustomerComboItem selectedCustomerItem = customerComboBox.getValue();
            if (selectedCustomerItem == null) {
                showAlert("Error", "Please select a customer.");
                return;
            }

            // Get customer from database
            CustomerDAO customerDAO = new CustomerDAO();
            Customer customer = customerDAO.getCustomerById(selectedCustomerItem.getCustomerId());
            
            if (customer == null) {
                showAlert("Error", "Selected customer not found in database.");
                return;
            }

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
            double totalAmount = days * selectedCar.getDailyRate();

            // Create rental object
            Rental rental = new Rental();
            rental.setCustomerId(customer.getCustomerId());
            rental.setCarId(selectedCar.getCarId());
            rental.setStartDate(startDate);
            rental.setEndDate(endDate);
            rental.setTotalAmount(totalAmount);
            rental.setStatus("Active");

            // Save rental to database
            boolean success = rentalDAO.createRental(rental);
            
            if (success) {
                // Update car status to rented
                CarDAO carDAO = new CarDAO();
                carDAO.updateCarStatus(selectedCar.getCarId(), 2); // 2 = Rented
                
                // Log activity
                ActivityDAO.logCarRented(
                    customer.getFullName(),
                    selectedCar.getPlateNumber(),
                    selectedCar.getBrand(),
                    selectedCar.getModel()
                );

                System.out.println("✅ Rental created successfully!");
                
                // Show success dialog and redirect
                showSuccessDialogAndRedirectToDashboard(rental, customer, selectedCar);
                
            } else {
                showAlert("Error", "Failed to create rental. Please try again.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error creating rental: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "An error occurred while creating the rental: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (selectedCar == null) {
            showAlert("Validation Error", "Please select a car.");
            return false;
        }

        if (customerComboBox.getValue() == null) {
            showAlert("Validation Error", "Please select a customer.");
            return false;
        }

        if (startDatePicker.getValue() == null) {
            showAlert("Validation Error", "Please select a start date.");
            return false;
        }

        if (endDatePicker.getValue() == null) {
            showAlert("Validation Error", "Please select an end date.");
            return false;
        }

        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            showAlert("Validation Error", "End date cannot be before start date.");
            return false;
        }

        return true;
    }

    private void clearForm() {
        customerComboBox.setValue(null);
        customerNameField.clear();
        customerPhoneField.clear();
        customerEmailField.clear();
        customerAddressArea.clear();
        driverLicenseField.clear();
        birthDatePicker.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        totalAmountLabel.setText("₺0.00");
        rentalDaysLabel.setText("0 days");
    }

    @FXML
    private void handleSelectDifferentCar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view-cars.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) selectedCarCard.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Select Car - Car Rental System");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open car selection window.");
        }
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard - Car Rental System");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not return to dashboard.");
        }
    }

    public void setPreSelectedCar(Car car) {
        this.selectedCar = car;
        updateSelectedCarDisplay();
    }

    private void showSuccessDialogAndRedirectToDashboard(Rental rental, Customer customer, Car car) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("🎉 Rental Created Successfully");
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
            mainContainer.setAlignment(Pos.CENTER);

            // Success header with icon
            HBox headerBox = new HBox(20);
            headerBox.setAlignment(Pos.CENTER);
            headerBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #4CAF50, #2E7D32); " +
                "-fx-padding: 25; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12;"
            );

            Label successIcon = new Label("🎉");
            successIcon.setStyle("-fx-font-size: 48px;");

            VBox headerTextBox = new VBox(8);
            headerTextBox.setAlignment(Pos.CENTER);

            Label titleLabel = new Label("RENTAL CREATED!");
            titleLabel.setStyle(
                "-fx-font-size: 24px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: white;"
            );

            Label subtitleLabel = new Label("Your car rental has been successfully processed");
            subtitleLabel.setStyle(
                "-fx-font-size: 16px; " +
                "-fx-text-fill: rgba(255,255,255,0.9);"
            );

            headerTextBox.getChildren().addAll(titleLabel, subtitleLabel);
            headerBox.getChildren().addAll(successIcon, headerTextBox);

            // Details cards container
            HBox cardsContainer = new HBox(20);
            cardsContainer.setAlignment(Pos.CENTER);

            // Car details card
            VBox carCard = createRentalDetailCard(
                "🚗", "VEHICLE DETAILS", 
                new String[]{"Car", "Plate", "Daily Rate"}, 
                new String[]{
                    car.getBrand() + " " + car.getModel(),
                    car.getPlateNumber(),
                    "₺" + String.format("%.0f", car.getDailyRate())
                },
                "#E3F2FD", "#1976D2"
            );

            // Customer details card
            VBox customerCard = createRentalDetailCard(
                "👤", "CUSTOMER INFO",
                new String[]{"Name", "Phone", "Email"},
                new String[]{
                    customer.getFullName(),
                    customer.getPhone() != null ? customer.getPhone() : "N/A",
                    customer.getEmail() != null ? customer.getEmail() : "N/A"
                },
                "#E8F5E8", "#2E7D32"
            );

            cardsContainer.getChildren().addAll(carCard, customerCard);

            // Rental period and payment card
            VBox rentalCard = createRentalDetailCard(
                "📅", "RENTAL PERIOD",
                new String[]{"Start Date", "End Date", "Duration"},
                new String[]{
                    rental.getStartDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    rental.getEndDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    java.time.temporal.ChronoUnit.DAYS.between(rental.getStartDate(), rental.getEndDate()) + " days"
                },
                "#FFF3E0", "#F57C00"
            );

            VBox paymentCard = createRentalDetailCard(
                "💰", "PAYMENT INFO",
                new String[]{"Total Amount", "Status", "Rental ID"},
                new String[]{
                    "₺" + String.format("%.2f", rental.getTotalAmount()),
                    "Confirmed",
                    "#" + rental.getRentalId()
                },
                "#F3E5F5", "#7B1FA2"
            );

            // Second row of cards
            HBox cardsContainer2 = new HBox(20);
            cardsContainer2.setAlignment(Pos.CENTER);
            cardsContainer2.getChildren().addAll(rentalCard, paymentCard);

            // Instructions card
            VBox instructionsCard = new VBox(12);
            instructionsCard.setStyle(
                "-fx-background-color: #E8F5E8; " +
                "-fx-border-color: #4CAF50; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 20;"
            );

            Label instructionsTitle = new Label("📋 Next Steps");
            instructionsTitle.setStyle(
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #2E7D32;"
            );

            Label instruction1 = new Label("• Your rental is now active and ready");
            instruction1.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
            
            Label instruction2 = new Label("• You can view this rental in 'My Rentals' section");
            instruction2.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
            
            Label instruction3 = new Label("• Contact support if you need any assistance");
            instruction3.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

            instructionsCard.getChildren().addAll(instructionsTitle, instruction1, instruction2, instruction3);

            // Add all components to main container
            mainContainer.getChildren().addAll(headerBox, cardsContainer, cardsContainer2, instructionsCard);

            dialogPane.setContent(mainContainer);

            // Custom buttons
            ButtonType dashboardButton = new ButtonType("🏠 Go to Dashboard", ButtonBar.ButtonData.OK_DONE);
            ButtonType myRentalsButton = new ButtonType("📋 My Rentals", ButtonBar.ButtonData.OTHER);
            dialogPane.getButtonTypes().addAll(dashboardButton, myRentalsButton);

            // Style the buttons
            Button dashboardBtn = (Button) dialogPane.lookupButton(dashboardButton);
            dashboardBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #4CAF50, #2E7D32); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 12 24; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.4), 10, 0, 0, 3);"
            );

            Button myRentalsBtn = (Button) dialogPane.lookupButton(myRentalsButton);
            myRentalsBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #2196F3, #1976D2); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 12 24; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(33,150,243,0.4), 10, 0, 0, 3);"
            );

            // Set dialog size
            dialog.setResizable(false);
            dialogPane.setPrefWidth(650);
            dialogPane.setPrefHeight(700);

            // Show dialog and handle result
            dialog.showAndWait().ifPresent(result -> {
                if (result == dashboardButton) {
                    redirectToDashboard();
                } else if (result == myRentalsButton) {
                    redirectToMyRentals();
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error showing success dialog: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: Simple alert
            showAlert("Success", "Rental created successfully!");
            redirectToDashboard();
        }
    }

    private HBox createDetailRow(String icon, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");
        iconLabel.setPrefWidth(25);
        
        Label labelText = new Label(label + ":");
        labelText.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        labelText.setPrefWidth(100);
        
        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: #212529;");
        
        row.getChildren().addAll(iconLabel, labelText, valueText);
        return row;
    }

    private void redirectToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Admin Dashboard - Car Rental System");
            
        } catch (IOException e) {
            System.err.println("❌ Error redirecting to dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void redirectToMyRentals() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/my-rentals.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "My Rentals - Car Rental System");
            
        } catch (IOException e) {
            System.err.println("❌ Error redirecting to my rentals: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a detail card for rental information
     */
    private VBox createRentalDetailCard(String icon, String title, String[] labels, String[] values, String bgColor, String accentColor) {
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
        card.setPrefWidth(280);
        card.setMaxWidth(280);

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

    // CustomerComboItem class for displaying customers in ComboBox
    public static class CustomerComboItem {
        private final int customerId;
        private final String displayName;
        private final String fullName;
        private final String phone;
        private final String email;
        private final String driverLicense;
        private final String birthDate;
        private final String address;

        public CustomerComboItem(int customerId, String displayName, String fullName, 
                               String phone, String email, String driverLicense, 
                               String birthDate, String address) {
            this.customerId = customerId;
            this.displayName = displayName;
            this.fullName = fullName;
            this.phone = phone;
            this.email = email;
            this.driverLicense = driverLicense;
            this.birthDate = birthDate;
            this.address = address;
        }

        public int getCustomerId() { return customerId; }
        public String getDisplayName() { return displayName; }
        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public String getDriverLicense() { return driverLicense; }
        public String getBirthDate() { return birthDate; }
        public String getAddress() { return address; }

        @Override
        public String toString() { return displayName; }
    }

    private void setupCustomerComboBox() {
        try {
            CustomerDAO customerDAO = new CustomerDAO();
            // 🔄 Only get customers with user accounts (Customer role - RoleID = 2)
            List<Customer> customers = customerDAO.getCustomersWithUserAccounts();
            
            ObservableList<CustomerComboItem> customerItems = FXCollections.observableArrayList();
            
            for (Customer customer : customers) {
                String displayName = customer.getFullName() + " (" + customer.getPhone() + ")";
                CustomerComboItem item = new CustomerComboItem(
                    customer.getCustomerId(),
                    displayName,
                    customer.getFullName(),
                    customer.getPhone(),
                    customer.getEmail(),
                    customer.getDriverLicenseNo(),
                    customer.getDateOfBirth() != null ? customer.getDateOfBirth().toString() : "",
                    customer.getAddress()
                );
                customerItems.add(item);
            }
            
            customerComboBox.setItems(customerItems);
            
            // Add listener for customer selection
            customerComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    // Fill customer fields with selected customer data
                    customerNameField.setText(newVal.getFullName());
                    customerPhoneField.setText(newVal.getPhone());
                    customerEmailField.setText(newVal.getEmail());
                    driverLicenseField.setText(newVal.getDriverLicense());
                    customerAddressArea.setText(newVal.getAddress());
                    
                    // Parse and set birth date
                    if (!newVal.getBirthDate().isEmpty()) {
                        try {
                            birthDatePicker.setValue(LocalDate.parse(newVal.getBirthDate()));
                        } catch (Exception e) {
                            System.out.println("⚠️ Could not parse birth date: " + newVal.getBirthDate());
                        }
                    }
                    
                    updateRentalButtonState();
                }
            });
            
            System.out.println("✅ Customer ComboBox setup completed with " + customers.size() + " customers (Customer role only)");
            
        } catch (Exception e) {
            System.err.println("❌ Error setting up customer ComboBox: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateRentalButtonState() {
        boolean carSelected = selectedCar != null;
        boolean customerSelected = customerComboBox.getValue() != null;
        
        createRentalButton.setDisable(!(carSelected && customerSelected));
        
        if (carSelected && customerSelected) {
            statusLabel.setText("✅ Ready to create rental");
        } else if (!carSelected) {
            statusLabel.setText("⚠️ Please select a car");
        } else if (!customerSelected) {
            statusLabel.setText("⚠️ Please select a customer");
        }
    }

    // User Registration Methods
    /**
     * ✅ Create Account Button Handler
     * Creates new customer with Customer role (RoleID = 2) and updates ComboBox
     */
    @FXML
    private void handleCreateUser() {
        try {
            // Validate registration form
            if (!validateRegistrationForm()) {
                return;
            }
            
            // Get form data
            String fullName = regFullNameField.getText().trim();
            String username = regUsernameField.getText().trim();
            String email = regEmailField.getText().trim();
            String phone = regPhoneField.getText().trim();
            String password = regPasswordField.getText();
            String driverLicense = regDriverLicenseField.getText().trim();
            String address = regAddressArea.getText().trim();
            LocalDate birthDate = regBirthDatePicker.getValue();
            
            // Insert customer directly using SQL (not stored procedure)
            Customer createdCustomer = CustomerDAO.insertCustomerWithReturn(
                fullName, 
                phone, 
                address, 
                driverLicense, 
                birthDate != null ? Date.valueOf(birthDate) : null
            );
            
            if (createdCustomer != null) {
                // Set email for the customer
                createdCustomer.setEmail(email);
                
                // ✅ Create user account with Customer role (RoleID = 2)
                int userId = createUserAccountManually(username, password, email);
                
                if (userId > 0) {
                    // Link customer to user
                    boolean linkSuccess = UserAccountService.linkCustomerToUser(createdCustomer.getCustomerId(), userId);
                    
                    if (linkSuccess) {
                        System.out.println("✅ Customer " + createdCustomer.getCustomerId() + " successfully linked to User " + userId);
                        
                        // Send welcome email with the actual credentials
                        EmailService.sendWelcomeEmail(email, fullName, username, password, "", "");
                        
                        registrationStatusLabel.setText("✅ User account created successfully!");
                        registrationStatusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                        
                        // Log activity
                        ActivityDAO.logCustomerAdded(fullName);
                        
                        // Clear form
                        handleClearRegistrationForm();
                        
                        // ✅ Refresh customer ComboBox to include new customer (Customer role only)
                        setupCustomerComboBox();
                        
                        // ✅ Show modern success dialog
                        showAccountCreatedSuccessDialog(fullName, username, email);
                        
                        System.out.println("✅ User account created: " + fullName + " (Customer ID: " + createdCustomer.getCustomerId() + ", User ID: " + userId + ")");
                        System.out.println("   Username: " + username);
                        System.out.println("   Password: " + password);
                        System.out.println("   Role: Customer (RoleID = 2)");
                        
                        // Debug: Verify the link was created
                        verifyCustomerUserLink(createdCustomer.getCustomerId(), userId);
                        
                    } else {
                        System.err.println("❌ Failed to link Customer " + createdCustomer.getCustomerId() + " to User " + userId);
                        registrationStatusLabel.setText("❌ Failed to link customer to user account");
                        registrationStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    }
                    
                } else {
                    registrationStatusLabel.setText("❌ Failed to create user account");
                    registrationStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                }
            } else {
                registrationStatusLabel.setText("❌ Failed to create customer record");
                registrationStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            }
            
        } catch (Exception e) {
            registrationStatusLabel.setText("❌ Error: " + e.getMessage());
            registrationStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create user account manually with provided credentials (for registration form)
     * ✅ Always creates users with Customer role (RoleID = 2)
     */
    private int createUserAccountManually(String username, String password, String email) {
        String sql = """
            INSERT INTO Users (Username, PasswordHash, Email, RoleID, CreatedAt) 
            VALUES (?, ?, ?, 2, GETDATE())
            """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Hash password with BCrypt for security
            String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
            
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);
            // RoleID = 2 (Customer role) is hardcoded in SQL above
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    System.out.println("✅ User created in database with ID: " + userId + " (Customer role)");
                    return userId;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error creating user in database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    @FXML
    private void handleClearRegistrationForm() {
        regFullNameField.clear();
        regUsernameField.clear();
        regEmailField.clear();
        regPhoneField.clear();
        regPasswordField.clear();
        regConfirmPasswordField.clear();
        regDriverLicenseField.clear();
        regBirthDatePicker.setValue(null);
        regAddressArea.clear();
        
        registrationStatusLabel.setText("✅ Fill the form to create a new user account");
        registrationStatusLabel.setStyle("-fx-text-fill: #666; -fx-font-weight: bold;");
    }
    
    private boolean validateRegistrationForm() {
        // Validate required fields
        if (regFullNameField.getText().trim().isEmpty()) {
            registrationStatusLabel.setText("❌ Full name is required");
            registrationStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            return false;
        }
        
        if (regUsernameField.getText().trim().length() < 4) {
            registrationStatusLabel.setText("❌ Username must be at least 4 characters");
            registrationStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            return false;
        }
        
        if (regEmailField.getText().trim().isEmpty() || !regEmailField.getText().contains("@")) {
            registrationStatusLabel.setText("❌ Valid email is required");
            registrationStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            return false;
        }
        
        if (regPasswordField.getText().length() < 6) {
            registrationStatusLabel.setText("❌ Password must be at least 6 characters");
            registrationStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            return false;
        }
        
        if (!regPasswordField.getText().equals(regConfirmPasswordField.getText())) {
            registrationStatusLabel.setText("❌ Passwords do not match");
            registrationStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            return false;
        }
        
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show modern success dialog when account is created successfully
     */
    private void showAccountCreatedSuccessDialog(String fullName, String username, String email) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("✅ Account Created Successfully");
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
            mainContainer.setAlignment(Pos.CENTER);

            // Success header with icon
            HBox headerBox = new HBox(20);
            headerBox.setAlignment(Pos.CENTER);
            headerBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #4CAF50, #2E7D32); " +
                "-fx-padding: 25; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12;"
            );

            Label successIcon = new Label("✅");
            successIcon.setStyle("-fx-font-size: 48px;");

            VBox headerTextBox = new VBox(8);
            headerTextBox.setAlignment(Pos.CENTER);

            Label titleLabel = new Label("ACCOUNT CREATED!");
            titleLabel.setStyle(
                "-fx-font-size: 24px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: white;"
            );

            Label subtitleLabel = new Label("Welcome to Car Rental System");
            subtitleLabel.setStyle(
                "-fx-font-size: 16px; " +
                "-fx-text-fill: rgba(255,255,255,0.9);"
            );

            headerTextBox.getChildren().addAll(titleLabel, subtitleLabel);
            headerBox.getChildren().addAll(successIcon, headerTextBox);

            // Account details card
            VBox detailsCard = new VBox(15);
            detailsCard.setStyle(
                "-fx-background-color: #F8F9FA; " +
                "-fx-border-color: #4CAF50; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 25;"
            );

            Label detailsTitle = new Label("👤 Account Details");
            detailsTitle.setStyle(
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #2E7D32;"
            );

            // Account info rows
            VBox accountInfoBox = new VBox(12);
            
            HBox nameRow = createAccountDetailRow("👤", "Full Name", fullName);
            HBox usernameRow = createAccountDetailRow("🔑", "Username", username);
            HBox emailRow = createAccountDetailRow("📧", "Email", email);
            HBox roleRow = createAccountDetailRow("🏷️", "Role", "Customer");

            accountInfoBox.getChildren().addAll(nameRow, usernameRow, emailRow, roleRow);
            detailsCard.getChildren().addAll(detailsTitle, accountInfoBox);

            // Instructions card
            VBox instructionsCard = new VBox(12);
            instructionsCard.setStyle(
                "-fx-background-color: #E3F2FD; " +
                "-fx-border-color: #2196F3; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 20;"
            );

            Label instructionsTitle = new Label("📋 Next Steps");
            instructionsTitle.setStyle(
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #1976D2;"
            );

            Label instruction1 = new Label("• You can now login with your username and password");
            instruction1.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
            
            Label instruction2 = new Label("• Your account has been added to the customer list");
            instruction2.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
            
            Label instruction3 = new Label("• A welcome email has been sent to your email address");
            instruction3.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

            instructionsCard.getChildren().addAll(instructionsTitle, instruction1, instruction2, instruction3);

            // Add all components to main container
            mainContainer.getChildren().addAll(headerBox, detailsCard, instructionsCard);

            dialogPane.setContent(mainContainer);

            // Custom buttons
            ButtonType okButton = new ButtonType("✅ OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType loginButton = new ButtonType("🚪 Go to Login", ButtonBar.ButtonData.OTHER);
            dialogPane.getButtonTypes().addAll(okButton, loginButton);

            // Style the buttons
            Button okBtn = (Button) dialogPane.lookupButton(okButton);
            okBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #4CAF50, #2E7D32); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 12 24; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.4), 10, 0, 0, 3);"
            );

            Button loginBtn = (Button) dialogPane.lookupButton(loginButton);
            loginBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #2196F3, #1976D2); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 12 24; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(33,150,243,0.4), 10, 0, 0, 3);"
            );

            // Set dialog size
            dialog.setResizable(false);
            dialogPane.setPrefWidth(550);
            dialogPane.setPrefHeight(600);

            // Show dialog and handle result
            dialog.showAndWait().ifPresent(result -> {
                if (result == loginButton) {
                    // Redirect to login page
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) statusLabel.getScene().getWindow();
                        StageUtils.initializeDimensions(stage);
                        StageUtils.setFullscreenScene(stage, root, "Car Rental System - Login");
                    } catch (IOException e) {
                        System.err.println("❌ Error redirecting to login: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error showing success dialog: " + e.getMessage());
            e.printStackTrace();
            // Fallback to simple alert
            showAlert("Account Created", "Account created successfully for " + fullName);
        }
    }

    /**
     * Create a detail row for account information
     */
    private HBox createAccountDetailRow(String icon, String label, String value) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        iconLabel.setPrefWidth(25);

        Label labelText = new Label(label + ":");
        labelText.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #666;"
        );
        labelText.setPrefWidth(100);

        Label valueText = new Label(value);
        valueText.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #333; " +
            "-fx-font-weight: bold;"
        );

        row.getChildren().addAll(iconLabel, labelText, valueText);
        return row;
    }

    /**
     * Verify that customer-user link was created successfully
     */
    private void verifyCustomerUserLink(int customerId, int userId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT UserID FROM Customers WHERE CustomerID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, customerId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int linkedUserId = rs.getInt("UserID");
                    if (linkedUserId == userId) {
                        System.out.println("✅ Verification successful: Customer " + customerId + " is linked to User " + userId);
                    } else {
                        System.err.println("❌ Verification failed: Customer " + customerId + " is linked to User " + linkedUserId + " instead of " + userId);
                    }
                } else {
                    System.err.println("❌ Verification failed: Customer " + customerId + " not found");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error verifying customer-user link: " + e.getMessage());
        }
    }
}