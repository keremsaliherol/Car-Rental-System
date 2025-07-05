package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import com.kerem.ordersystem.carrentalsystem.model.CarCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class CategoryManagementController {

    @FXML private TextField categoryNameField;
    @FXML private TableView<CarCategory> categoryTable;
    @FXML private TableColumn<CarCategory, Integer> idColumn;
    @FXML private TableColumn<CarCategory, String> nameColumn;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        setupTable();
        loadCategories();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
    }

    private void loadCategories() {
        ObservableList<CarCategory> categories = FXCollections.observableArrayList();
        String sql = "SELECT CategoryID, CategoryName FROM CarCategories ORDER BY CategoryName";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(new CarCategory(
                    rs.getInt("CategoryID"),
                    rs.getString("CategoryName")
                ));
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Error loading categories: " + e.getMessage());
        }

        categoryTable.setItems(categories);
    }

    @FXML
    private void handleAddCategory() {
        String categoryName = categoryNameField.getText().trim();

        if (categoryName.isEmpty()) {
            statusLabel.setText("❗ Please enter a category name.");
            return;
        }

        String sql = "INSERT INTO CarCategories (CategoryName) VALUES (?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryName);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                statusLabel.setText("✅ Category added successfully.");
                categoryNameField.clear();
                loadCategories(); // Refresh table
            } else {
                statusLabel.setText("❌ Failed to add category.");
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateCategory() {
        CarCategory selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        String newCategoryName = categoryNameField.getText().trim();

        if (selectedCategory == null) {
            statusLabel.setText("❗ Please select a category to update.");
            return;
        }

        if (newCategoryName.isEmpty()) {
            statusLabel.setText("❗ Please enter a new category name.");
            return;
        }

        String sql = "UPDATE CarCategories SET CategoryName = ? WHERE CategoryID = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newCategoryName);
            stmt.setInt(2, selectedCategory.getCategoryId());
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                statusLabel.setText("✅ Category updated successfully.");
                categoryNameField.clear();
                loadCategories(); // Refresh table
            } else {
                statusLabel.setText("❌ Failed to update category.");
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCategory() {
        CarCategory selectedCategory = categoryTable.getSelectionModel().getSelectedItem();

        if (selectedCategory == null) {
            statusLabel.setText("❗ Please select a category to delete.");
            return;
        }

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Confirmation");
        confirmation.setHeaderText("Are you sure you want to delete this category?");
        confirmation.setContentText("Category: " + selectedCategory.getCategoryName());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteCategory(selectedCategory.getCategoryId());
            }
        });
    }

    private void deleteCategory(int categoryId) {
        String sql = "DELETE FROM CarCategories WHERE CategoryID = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                statusLabel.setText("✅ Category deleted successfully.");
                loadCategories(); // Refresh table
            } else {
                statusLabel.setText("❌ Failed to delete category.");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("FOREIGN KEY constraint")) {
                statusLabel.setText("❌ Cannot delete category - it's being used by cars.");
            } else {
                statusLabel.setText("❌ Database error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) categoryTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
        } catch (IOException e) {
            statusLabel.setText("❌ Error returning to dashboard: " + e.getMessage());
        }
    }
}
