package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class CarFormController {

    @FXML private TextField plateField;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private TextField rateField;
    @FXML private ComboBox<CarCategoryItem> categoryComboBox;
    @FXML private TextField mileageField;
    @FXML private TextArea descriptionField;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        ObservableList<CarCategoryItem> list = FXCollections.observableArrayList();
        String sql = "SELECT CategoryID, CategoryName FROM CarCategories ORDER BY CategoryName";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new CarCategoryItem(rs.getInt("CategoryID"), rs.getString("CategoryName")));
            }

            categoryComboBox.setItems(list);

        } catch (SQLException e) {
            statusLabel.setText("❌ Category load error: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveCar() {
        try {
            String plate = plateField.getText().trim();
            String brand = brandField.getText().trim();
            String model = modelField.getText().trim();
            String yearText = yearField.getText().trim();
            String rateText = rateField.getText().trim();
            String mileageText = mileageField.getText().trim();
            String desc = descriptionField.getText().trim();
            CarCategoryItem selectedCategory = categoryComboBox.getValue();

            // Validation
            if (plate.isEmpty() || brand.isEmpty() || model.isEmpty() || 
                yearText.isEmpty() || rateText.isEmpty() || mileageText.isEmpty()) {
                statusLabel.setText("❌ Please fill all required fields.");
                return;
            }

            if (selectedCategory == null) {
                statusLabel.setText("❌ Please select a category.");
                return;
            }

            int year = Integer.parseInt(yearText);
            double rate = Double.parseDouble(rateText);
            int mileage = Integer.parseInt(mileageText);

            // Insert car into database
            String sql = """
                INSERT INTO Cars (PlateNumber, Brand, Model, Year, DailyRate, CategoryID, Mileage, Description, StatusID)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)
                """;

            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, plate);
                stmt.setString(2, brand);
                stmt.setString(3, model);
                stmt.setInt(4, year);
                stmt.setDouble(5, rate);
                stmt.setInt(6, selectedCategory.id()); // CategoryID
                stmt.setInt(7, mileage);
                stmt.setString(8, desc);

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    statusLabel.setText("✅ Car added successfully.");
                    clearFields();
                } else {
                    statusLabel.setText("❌ Failed to add car.");
                }

            } catch (SQLException e) {
                statusLabel.setText("❌ Database error: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Please enter valid numbers for year, rate, and mileage.");
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    private void clearFields() {
        plateField.clear();
        brandField.clear();
        modelField.clear();
        yearField.clear();
        rateField.clear();
        mileageField.clear();
        descriptionField.clear();
        categoryComboBox.setValue(null);
    }

    public record CarCategoryItem(int id, String name) {
        @Override 
        public String toString() {
            return name;
        }
    }
} 