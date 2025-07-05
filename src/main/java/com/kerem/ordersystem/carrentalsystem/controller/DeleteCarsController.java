package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.CarDAO;
import com.kerem.ordersystem.carrentalsystem.database.CarCategoryDAO;
import com.kerem.ordersystem.carrentalsystem.model.Car;
import com.kerem.ordersystem.carrentalsystem.model.CarCategory;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;

public class DeleteCarsController implements Initializable {

    @FXML private ScrollPane carsScrollPane;
    @FXML private Label statusLabel;
    @FXML private Label totalCarsLabel;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> fuelFilterComboBox;

    private List<Car> carsList;
    private List<Car> filteredCarsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCars();
        setupFilters();
        displayCars();
    }

    private void setupFilters() {
        // Category filter
        ObservableList<String> categories = FXCollections.observableArrayList();
        categories.add("All Categories");
        List<CarCategory> categoryList = CarCategoryDAO.getAllCategoriesAsObjects();
        for (CarCategory category : categoryList) {
            categories.add(category.getCategoryName());
        }
        categoryFilterComboBox.setItems(categories);
        categoryFilterComboBox.setValue("All Categories");

        // Status filter
        ObservableList<String> statuses = FXCollections.observableArrayList();
        statuses.add("All Statuses");
        statuses.add("AVAILABLE");
        statuses.add("RENTED");
        statuses.add("MAINTENANCE");
        statusFilterComboBox.setItems(statuses);
        statusFilterComboBox.setValue("All Statuses");

        // Fuel type filter
        ObservableList<String> fuelTypes = FXCollections.observableArrayList();
        fuelTypes.add("All Fuel Types");
        fuelTypes.add("Gasoline");
        fuelTypes.add("Diesel");
        fuelTypes.add("Electric");
        fuelFilterComboBox.setItems(fuelTypes);
        fuelFilterComboBox.setValue("All Fuel Types");
    }

    private void loadCars() {
        carsList = CarDAO.getAllCars();
        filteredCarsList = carsList;
        updateStatusLabels();
    }

    private void updateStatusLabels() {
        statusLabel.setText("✅ " + filteredCarsList.size() + " cars showing");
        totalCarsLabel.setText("Total " + filteredCarsList.size() + " cars");
    }

    @FXML
    private void handleApplyFilters() {
        String selectedCategory = categoryFilterComboBox.getValue();
        String selectedStatus = statusFilterComboBox.getValue();
        String selectedFuel = fuelFilterComboBox.getValue();

        filteredCarsList = carsList.stream()
            .filter(car -> {
                // Category filter
                if (!"All Categories".equals(selectedCategory)) {
                    if (!selectedCategory.equals(car.getCategoryName())) {
                        return false;
                    }
                }

                // Status filter
                if (!"All Statuses".equals(selectedStatus)) {
                    String carStatus = getStatusText(car.getStatus());
                    if (!selectedStatus.equals(carStatus)) {
                        return false;
                    }
                }

                // Fuel type filter
                if (!"All Fuel Types".equals(selectedFuel)) {
                    String carFuelType = extractFuelType(car.getDescription());
                    if (!selectedFuel.equals(carFuelType)) {
                        return false;
                    }
                }

                return true;
            })
            .collect(Collectors.toList());

        updateStatusLabels();
        displayCars();
        
        statusLabel.setText("🔍 Filter applied - " + filteredCarsList.size() + " cars found");
    }

    @FXML
    private void handleClearFilters() {
        categoryFilterComboBox.setValue("All Categories");
        statusFilterComboBox.setValue("All Statuses");
        fuelFilterComboBox.setValue("All Fuel Types");
        
        filteredCarsList = carsList;
        updateStatusLabels();
        displayCars();
        
        statusLabel.setText("🔄 Filters cleared - Showing all cars");
    }

    private void displayCars() {
        // Create grid layout
        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setPadding(new Insets(20));
        gridPane.setAlignment(Pos.TOP_CENTER);

        int column = 0;
        int row = 0;
        int carsPerRow = 4; // 4 cars per row

        for (Car car : filteredCarsList) {
            VBox carCard = createCarCard(car);
            gridPane.add(carCard, column, row);

            column++;
            if (column == carsPerRow) {
                column = 0;
                row++;
            }
        }

        carsScrollPane.setContent(gridPane);
    }

    private VBox createCarCard(Car car) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPrefWidth(280);
        card.setPrefHeight(320);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E0E0E0; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
            "-fx-padding: 15;"
        );

        // Economic label (only for electric cars)
        Label economicLabel = null;
        String carFuelType = extractFuelType(car.getDescription());
        if ("Electric".equals(carFuelType)) {
            economicLabel = new Label("Economic");
            economicLabel.setStyle(
                "-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 12px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 5 10; " +
                "-fx-background-radius: 15;"
            );
            economicLabel.setAlignment(Pos.TOP_LEFT);
        }

        // Car image
        ImageView carImage = new ImageView();
        carImage.setFitWidth(250);
        carImage.setFitHeight(140);
        carImage.setPreserveRatio(true);
        carImage.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");
        
        // Car image loading
        try {
            Image carImg = null;
            if (car.getImagePath() != null && !car.getImagePath().isEmpty()) {
                // Use specific car image if available
                carImg = new Image(getClass().getResourceAsStream(car.getImagePath()));
            } else {
                // Use default image
                carImg = new Image(getClass().getResourceAsStream("/applogo.png"));
            }
            carImage.setImage(carImg);
        } catch (Exception e) {
            // Use placeholder if image cannot be loaded
            carImage.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");
            System.err.println("Car image could not be loaded: " + car.getBrand() + " " + car.getModel() + " - " + e.getMessage());
        }

        // Car title
        Label carTitle = new Label(car.getBrand() + " " + car.getModel());
        carTitle.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #333;"
        );

        // Year
        Label yearLabel = new Label(String.valueOf(car.getYear()));
        yearLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #666;"
        );

        // Features (fuel, gear, plate)
        HBox featuresBox = new HBox();
        featuresBox.setSpacing(15);
        featuresBox.setAlignment(Pos.CENTER_LEFT);

        // Fuel type (from description)
        String fuelType = extractFuelType(car.getDescription());
        Label fuelLabel = createFeatureLabel("⛽", fuelType);
        
        // Category
        Label categoryLabel = createFeatureLabel("📋", car.getCategoryName());

        featuresBox.getChildren().addAll(fuelLabel, categoryLabel);

        // Daily rate
        HBox priceBox = new HBox();
        priceBox.setSpacing(5);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Label priceTitle = new Label("DAILY");
        priceTitle.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-text-fill: #666; " +
            "-fx-font-weight: bold;"
        );

        Label priceValue = new Label(String.format("%.0f TL", car.getDailyRate()));
        priceValue.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #333;"
        );

        priceBox.getChildren().addAll(priceTitle, priceValue);

        // Status label
        Label statusLabel = new Label(getStatusText(car.getStatus()));
        if ("Available".equals(car.getStatus())) {
            statusLabel.setStyle(
                "-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 5 10; " +
                "-fx-background-radius: 12;"
            );
        } else {
            statusLabel.setStyle(
                "-fx-background-color: #F44336; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 5 10; " +
                "-fx-background-radius: 12;"
            );
        }

        // Combine card
        VBox topBox = new VBox(5);
        if (economicLabel != null) {
            topBox.getChildren().addAll(economicLabel, carImage);
        } else {
            topBox.getChildren().add(carImage);
        }
        topBox.setAlignment(Pos.TOP_LEFT);

        VBox infoBox = new VBox(8);
        infoBox.getChildren().addAll(carTitle, yearLabel, featuresBox, priceBox, statusLabel);

        card.getChildren().addAll(topBox, infoBox);
        card.setAlignment(Pos.TOP_CENTER);

        // Add clickable feature to card (for deletion)
        card.setOnMouseClicked(e -> handleDeleteCar(car));
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-border-color: #F44336; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(244,67,54,0.3), 15, 0, 0, 3); " +
                "-fx-padding: 15; " +
                "-fx-cursor: hand;"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-border-color: #E0E0E0; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                "-fx-padding: 15;"
            );
        });

        return card;
    }

    private Label createFeatureLabel(String icon, String text) {
        Label label = new Label(icon + " " + text);
        label.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-text-fill: #666; " +
            "-fx-background-color: #F5F5F5; " +
            "-fx-padding: 3 8; " +
            "-fx-background-radius: 10;"
        );
        return label;
    }

    private String extractFuelType(String description) {
        if (description != null) {
            // Check for English format first
            if (description.contains("Fuel: ")) {
                String fuelType = description.substring(description.indexOf("Fuel: ") + 6).trim();
                return translateFuelType(fuelType);
            }
            // Check for Turkish format (legacy data)
            if (description.contains("Yakıt: ")) {
                String fuelType = description.substring(description.indexOf("Yakıt: ") + 7).trim();
                return translateFuelType(fuelType);
            }
        }
        return "Gasoline"; // default
    }
    
    private String translateFuelType(String turkishFuelType) {
        if (turkishFuelType == null) return "Gasoline";
        
        switch (turkishFuelType.toLowerCase()) {
            case "benzin": return "Gasoline";
            case "dizel": return "Diesel";
            case "elektrik": return "Electric";
            case "hybrid": return "Hybrid";
            case "lpg": return "LPG";
            case "benzin+lpg": return "Gasoline+LPG";
            // If already in English, return as is
            case "gasoline": return "Gasoline";
            case "diesel": return "Diesel";
            case "electric": return "Electric";
            default: return turkishFuelType; // Return original if not found
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "Available": return "AVAILABLE";
            case "Rented": return "RENTED";
            case "Maintenance": return "MAINTENANCE";
            default: return status.toUpperCase();
        }
    }

    private void handleDeleteCar(Car car) {
        // Create custom confirmation dialog
        createCustomDeleteConfirmationDialog(car);
    }

    private void createCustomDeleteConfirmationDialog(Car car) {
        // Create main dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🗑️ Car Deletion Confirmation");
        dialog.setHeaderText(null);

        // Get dialog pane and apply style
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E0E0E0; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12;"
        );

        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setAlignment(Pos.CENTER);

        // Warning icon and title
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER);
        
        Label warningIcon = new Label("⚠️");
        warningIcon.setStyle("-fx-font-size: 48px;");
        
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("ATTENTION!");
        titleLabel.setStyle(
            "-fx-font-size: 24px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #D32F2F;"
        );
        
        Label subtitleLabel = new Label("This operation cannot be undone!");
        subtitleLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #666; " +
            "-fx-font-style: italic;"
        );
        
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        headerBox.getChildren().addAll(warningIcon, titleBox);

        // Car information card
        VBox carInfoCard = new VBox(15);
        carInfoCard.setStyle(
            "-fx-background-color: #F5F5F5; " +
            "-fx-border-color: #E0E0E0; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 20;"
        );

        Label cardTitle = new Label("Car Information to be Deleted:");
        cardTitle.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #333;"
        );

        // Car details
        VBox detailsBox = new VBox(8);
        
        HBox brandModelBox = createInfoRow("🚗", "Brand/Model:", car.getBrand() + " " + car.getModel());
        HBox yearBox = createInfoRow("📅", "Year:", String.valueOf(car.getYear()));
        HBox plateBox = createInfoRow("🔢", "Plate:", car.getPlateNumber());
        HBox categoryBox = createInfoRow("📋", "Category:", car.getCategoryName());
        HBox priceBox = createInfoRow("💰", "Daily Rate:", String.format("%.0f TL", car.getDailyRate()));
        HBox statusBox = createInfoRow("📊", "Status:", getStatusText(car.getStatus()));

        detailsBox.getChildren().addAll(brandModelBox, yearBox, plateBox, categoryBox, priceBox, statusBox);
        carInfoCard.getChildren().addAll(cardTitle, detailsBox);

        // Warning message
        HBox warningBox = new HBox(10);
        warningBox.setAlignment(Pos.CENTER_LEFT);
        warningBox.setStyle(
            "-fx-background-color: #FFF3E0; " +
            "-fx-border-color: #FF9800; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 15;"
        );

        Label warningIconSmall = new Label("⚠️");
        warningIconSmall.setStyle("-fx-font-size: 16px;");

        VBox warningTextBox = new VBox(3);
        Label warningTitle = new Label("Important Warning");
        warningTitle.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #E65100; " +
            "-fx-font-size: 12px;"
        );

        Label warningText = new Label("This car will be permanently deleted from the system. This operation cannot be undone!");
        warningText.setStyle(
            "-fx-text-fill: #BF360C; " +
            "-fx-font-size: 11px;"
        );
        warningText.setWrapText(true);

        warningTextBox.getChildren().addAll(warningTitle, warningText);
        warningBox.getChildren().addAll(warningIconSmall, warningTextBox);

        // Confirmation question
        Label confirmationQuestion = new Label("Are you sure you want to delete this car?");
        confirmationQuestion.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #333; " +
            "-fx-text-alignment: center;"
        );
        confirmationQuestion.setAlignment(Pos.CENTER);

        // Ana container'a ekle
        mainContainer.getChildren().addAll(
            headerBox, 
            carInfoCard, 
            warningBox, 
            confirmationQuestion
        );

        dialogPane.setContent(mainContainer);

        // Customize buttons
        ButtonType deleteButton = new ButtonType("🗑️ Yes, Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("❌ Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(deleteButton, cancelButton);

        // Customize button styles
        dialogPane.lookupButton(deleteButton).setStyle(
            "-fx-background-color: #D32F2F; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        dialogPane.lookupButton(cancelButton).setStyle(
            "-fx-background-color: #757575; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        // Dialog boyutunu ayarla
        dialog.setResizable(false);
        dialogPane.setPrefWidth(500);

        // Sonucu işle
        dialog.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                performDeleteCar(car);
            }
        });
    }

    private HBox createInfoRow(String icon, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");
        iconLabel.setPrefWidth(25);

        Label labelText = new Label(label);
        labelText.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #555; " +
            "-fx-font-size: 12px;"
        );
        labelText.setPrefWidth(100);

        Label valueText = new Label(value);
        valueText.setStyle(
            "-fx-text-fill: #333; " +
            "-fx-font-size: 12px;"
        );

        row.getChildren().addAll(iconLabel, labelText, valueText);
        return row;
    }

    private void performDeleteCar(Car car) {
        boolean success = CarDAO.deleteCar(car.getCarId());
        
        if (success) {
            // Successful deletion - Modern dialog
            showSuccessDialog(car);
            
            // Refresh list
            loadCars();
            displayCars();
            statusLabel.setText("✅ Car deleted - List updated");
        } else {
            // Deletion failed
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Car Could Not Be Deleted");
            error.setHeaderText("This car cannot be deleted!");
            error.setContentText("This car cannot be deleted because it has ACTIVE rentals.\n\n" +
                                "You must first complete or cancel the active rental for this car.\n" +
                                "Cars with only completed/cancelled rental history can be deleted.");
            error.showAndWait();
            statusLabel.setText("❌ Car could not be deleted: Active rentals exist");
        }
    }

    private void showSuccessDialog(Car car) {
        // Create main dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🎉 Operation Successful");
        dialog.setHeaderText(null);

        // Get dialog pane and apply style
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E0E0E0; " +
            "-fx-border-radius: 15; " +
            "-fx-background-radius: 15;"
        );

        // Main container
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setAlignment(Pos.CENTER);

        // Success icon and animation effect
        VBox iconContainer = new VBox(10);
        iconContainer.setAlignment(Pos.CENTER);
        
        Label successIcon = new Label("✅");
        successIcon.setStyle(
            "-fx-font-size: 64px; " +
            "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.6), 20, 0, 0, 0);"
        );
        
        Label successTitle = new Label("CAR DELETED!");
        successTitle.setStyle(
            "-fx-font-size: 24px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32;"
        );
        
        iconContainer.getChildren().addAll(successIcon, successTitle);

        // Car information card
        VBox carInfoCard = new VBox(15);
        carInfoCard.setStyle(
            "-fx-background-color: #F1F8E9; " +
            "-fx-border-color: #4CAF50; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 20;"
        );

        Label cardTitle = new Label("Deleted Car Information:");
        cardTitle.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32;"
        );

        // Car details
        VBox detailsBox = new VBox(10);
        
        HBox brandModelBox = createSuccessInfoRow("🚗", "Brand/Model:", car.getBrand() + " " + car.getModel());
        HBox plateBox = createSuccessInfoRow("🔢", "Plate:", car.getPlateNumber());
        HBox categoryBox = createSuccessInfoRow("📋", "Category:", car.getCategoryName());
        HBox priceBox = createSuccessInfoRow("💰", "Daily Rate:", String.format("%.0f TL", car.getDailyRate()));

        detailsBox.getChildren().addAll(brandModelBox, plateBox, categoryBox, priceBox);
        carInfoCard.getChildren().addAll(cardTitle, detailsBox);

        // Success message
        VBox messageBox = new VBox(8);
        messageBox.setAlignment(Pos.CENTER);
        
        Label mainMessage = new Label("Car has been successfully removed from the system!");
        mainMessage.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #333; " +
            "-fx-text-alignment: center;"
        );
        
        Label subMessage = new Label("Car list has been automatically updated.");
        subMessage.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #666; " +
            "-fx-text-alignment: center;"
        );
        
        messageBox.getChildren().addAll(mainMessage, subMessage);

        // Add to main container
        mainContainer.getChildren().addAll(
            iconContainer,
            carInfoCard,
            messageBox
        );

        dialogPane.setContent(mainContainer);

        // OK button
        ButtonType okButton = new ButtonType("✅ OK", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(okButton);

        // Buton stilini özelleştir
        dialogPane.lookupButton(okButton).setStyle(
            "-fx-background-color: linear-gradient(to right, #4CAF50, #388E3C); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 12 30; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.4), 8, 0, 0, 2);"
        );

        // Dialog boyutunu ayarla
        dialog.setResizable(false);
        dialogPane.setPrefWidth(450);

        // Dialogu göster
        dialog.showAndWait();
    }

    private HBox createSuccessInfoRow(String icon, String label, String value) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        iconLabel.setPrefWidth(30);

        Label labelText = new Label(label);
        labelText.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32; " +
            "-fx-font-size: 13px;"
        );
        labelText.setPrefWidth(110);

        Label valueText = new Label(value);
        valueText.setStyle(
            "-fx-text-fill: #1B5E20; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold;"
        );

        row.getChildren().addAll(iconLabel, labelText, valueText);
        return row;
    }

    @FXML
    private void handleRefresh() {
        loadCars();
        displayCars();
        statusLabel.setText("🔄 Car list refreshed");
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
        } catch (IOException e) {
            statusLabel.setText("❌ Dashboard could not be loaded: " + e.getMessage());
        }
    }
} 