package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.CarDAO;
import com.kerem.ordersystem.carrentalsystem.database.CarCategoryDAO;
import com.kerem.ordersystem.carrentalsystem.database.ActivityDAO;
import com.kerem.ordersystem.carrentalsystem.model.CarCategory;
import com.kerem.ordersystem.carrentalsystem.util.StageUtils;
import com.kerem.ordersystem.carrentalsystem.util.ComboBoxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.geometry.Side;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AddCarController implements Initializable {

    @FXML private TextField plateNumberField;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private TextField dailyRateField;
    @FXML private ComboBox<CarCategory> categoryComboBox;
    @FXML private TextField mileageField;
    @FXML private ComboBox<String> fuelTypeComboBox;
    
    @FXML private Button addCarButton;
    @FXML private Button clearFormButton;
    @FXML private Label statusLabel;

    private ObservableList<CarCategory> categoriesList = FXCollections.observableArrayList();
    private ObservableList<String> fuelTypesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCategories();
        loadFuelTypes();
        setupValidation();
        setupComboBoxBehavior();
        
        // ComboBox için özel string converter
        categoryComboBox.setConverter(new javafx.util.StringConverter<CarCategory>() {
            @Override
            public String toString(CarCategory category) {
                return category != null ? category.getCategoryName() : "";
            }

            @Override
            public CarCategory fromString(String string) {
                return categoriesList.stream()
                    .filter(cat -> cat.getCategoryName().equals(string))
                    .findFirst()
                    .orElse(null);
            }
        });
    }
    
    private void setupComboBoxBehavior() {
        // Use ComboBoxUtils to fix dropdown positioning
        ComboBoxUtils.fixDropdownPositions(categoryComboBox, fuelTypeComboBox);
        System.out.println("✅ ComboBox dropdown positioning configured using ComboBoxUtils");
    }

    private void loadCategories() {
        try {
            List<CarCategory> categories = CarCategoryDAO.getAllCategoriesAsObjects();
            categoriesList.clear();
            categoriesList.addAll(categories);
            categoryComboBox.setItems(categoriesList);
            
            if (!categoriesList.isEmpty()) {
                categoryComboBox.getSelectionModel().selectFirst();
            }
            
            statusLabel.setText("✅ " + categories.size() + " categories loaded");
        } catch (Exception e) {
            statusLabel.setText("❌ Error loading categories: " + e.getMessage());
        }
    }

    private void loadFuelTypes() {
        fuelTypesList.addAll("Gasoline", "Diesel", "LPG", "Hybrid", "Electric", "Gasoline+LPG");
        fuelTypeComboBox.setItems(fuelTypesList);
        fuelTypeComboBox.getSelectionModel().selectFirst(); // Select first option as default
    }

    private void setupValidation() {
        // Allow only numeric input
        yearField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                yearField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        mileageField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                mileageField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Decimal number control for daily rate
        dailyRateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([.]\\d*)?")) {
                dailyRateField.setText(oldValue);
            }
        });
    }

    @FXML
    private void handleAddCar() {
        if (!validateInputs()) {
            return;
        }

        try {
            String plateNumber = plateNumberField.getText().trim().toUpperCase();
            String brand = brandField.getText().trim();
            String model = modelField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());
            double dailyRate = Double.parseDouble(dailyRateField.getText().trim());
            CarCategory selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
            int mileage = Integer.parseInt(mileageField.getText().trim());
            String fuelType = fuelTypeComboBox.getSelectionModel().getSelectedItem();

            boolean success = CarDAO.insertCar(
                plateNumber, brand, model, year, dailyRate, 
                selectedCategory.getCategoryId(), mileage, "Fuel: " + fuelType
            );

            if (success) {
                statusLabel.setText("✅ Car added successfully!");
                
                // Create activity record
                try {
                    ActivityDAO.createTableIfNotExists();
                    boolean activityLogged = ActivityDAO.logCarAdded(plateNumber, brand, model);
                    if (activityLogged) {
                        System.out.println("Activity logged: Car added - " + brand + " " + model + " (" + plateNumber + ")");
                    } else {
                        System.err.println("Failed to log car addition activity");
                    }
                } catch (Exception e) {
                    System.err.println("Error logging car addition activity: " + e.getMessage());
                    e.printStackTrace();
                }
                
                clearForm();
                
                // Return to dashboard after 2 seconds
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                pause.setOnFinished(e -> handleBackToDashboard());
                pause.play();
            } else {
                statusLabel.setText("❌ Error adding car! Database operation failed.");
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Please enter numeric fields correctly!");
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
            e.printStackTrace(); // To see full error in console
        }
    }

    private boolean validateInputs() {
        if (plateNumberField.getText().trim().isEmpty()) {
            statusLabel.setText("❌ License plate cannot be empty!");
            plateNumberField.requestFocus();
            return false;
        }
        
        if (brandField.getText().trim().isEmpty()) {
            statusLabel.setText("❌ Brand cannot be empty!");
            brandField.requestFocus();
            return false;
        }
        
        if (modelField.getText().trim().isEmpty()) {
            statusLabel.setText("❌ Model cannot be empty!");
            modelField.requestFocus();
            return false;
        }
        
        if (yearField.getText().trim().isEmpty()) {
            statusLabel.setText("❌ Year cannot be empty!");
            yearField.requestFocus();
            return false;
        }
        
        try {
            int year = Integer.parseInt(yearField.getText().trim());
            if (year < 1900 || year > 2025) {
                statusLabel.setText("❌ Please enter a valid year (1900-2025)!");
                yearField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Year must be a numeric value!");
            yearField.requestFocus();
            return false;
        }
        
        if (dailyRateField.getText().trim().isEmpty()) {
            statusLabel.setText("❌ Daily rate cannot be empty!");
            dailyRateField.requestFocus();
            return false;
        }
        
        try {
            double rate = Double.parseDouble(dailyRateField.getText().trim());
            if (rate <= 0) {
                statusLabel.setText("❌ Daily rate must be greater than zero!");
                dailyRateField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Daily rate must be a numeric value!");
            dailyRateField.requestFocus();
            return false;
        }
        
        if (categoryComboBox.getSelectionModel().getSelectedItem() == null) {
            statusLabel.setText("❌ Please select a category!");
            categoryComboBox.requestFocus();
            return false;
        }
        
        if (mileageField.getText().trim().isEmpty()) {
            statusLabel.setText("❌ Mileage cannot be empty!");
            mileageField.requestFocus();
            return false;
        }
        
        try {
            int mileage = Integer.parseInt(mileageField.getText().trim());
            if (mileage < 0) {
                statusLabel.setText("❌ Mileage cannot be negative!");
                mileageField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Mileage must be a numeric value!");
            mileageField.requestFocus();
            return false;
        }
        
        if (fuelTypeComboBox.getSelectionModel().getSelectedItem() == null) {
            statusLabel.setText("❌ Please select a fuel type!");
            fuelTypeComboBox.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        statusLabel.setText("📝 Form cleared");
    }

    private void clearForm() {
        plateNumberField.clear();
        brandField.clear();
        modelField.clear();
        yearField.clear();
        dailyRateField.clear();
        mileageField.clear();
        
        if (!categoriesList.isEmpty()) {
            categoryComboBox.getSelectionModel().selectFirst();
        }
        
        if (!fuelTypesList.isEmpty()) {
            fuelTypeComboBox.getSelectionModel().selectFirst();
        }
        
        plateNumberField.requestFocus();
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
            statusLabel.setText("❌ Dashboard yüklenemedi: " + e.getMessage());
        }
    }
} 