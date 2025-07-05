package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class EditProfileController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private TextField licenseField;
    @FXML private DatePicker dobPicker;
    @FXML private Label statusLabel;

    private static int currentCustomerId = 1; // Will be set from login session

    // Method to set current customer ID from login session
    public static void setCurrentCustomerId(int customerId) {
        currentCustomerId = customerId;
    }

    @FXML
    public void initialize() {
        loadProfile();
    }

    private void loadProfile() {
        String sql = "SELECT FullName, Phone, Address, DriverLicenseNo, DateOfBirth FROM Customers WHERE CustomerID = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentCustomerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("FullName"));
                phoneField.setText(rs.getString("Phone"));
                addressField.setText(rs.getString("Address"));
                licenseField.setText(rs.getString("DriverLicenseNo"));
                if (rs.getDate("DateOfBirth") != null) {
                    dobPicker.setValue(rs.getDate("DateOfBirth").toLocalDate());
                }
            } else {
                statusLabel.setText("❌ Müşteri profili bulunamadı.");
                statusLabel.setStyle("-fx-text-fill: #f44336;");
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Profil yükleme hatası: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    @FXML
    private void handleUpdate() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String license = licenseField.getText().trim();
        LocalDate dob = dobPicker.getValue();

        // Validation
        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            statusLabel.setText("❗ Ad, telefon ve adres alanları zorunludur.");
            statusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }
        
        if (!license.isEmpty() && license.length() < 5) {
            statusLabel.setText("❗ Ehliyet numarası en az 5 karakter olmalıdır.");
            statusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        String sql = """
            UPDATE Customers
            SET FullName = ?, Phone = ?, Address = ?, DriverLicenseNo = ?, DateOfBirth = ?
            WHERE CustomerID = ?
            """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, address);
            stmt.setString(4, license.isEmpty() ? null : license);
            stmt.setDate(5, dob != null ? Date.valueOf(dob) : null);
            stmt.setInt(6, currentCustomerId);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                statusLabel.setText("✅ Profil başarıyla güncellendi!");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
            } else {
                statusLabel.setText("❌ Profil güncelleme başarısız.");
                statusLabel.setStyle("-fx-text-fill: #f44336;");
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Veritabanı hatası: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    @FXML
    private void handleReset() {
        nameField.clear();
        phoneField.clear();
        addressField.clear();
        licenseField.clear();
        dobPicker.setValue(null);
        statusLabel.setText("🔄 Form temizlendi.");
        statusLabel.setStyle("-fx-text-fill: #42A5F5;");
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/customer-dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Customer Dashboard");
        } catch (IOException e) {
            statusLabel.setText("❌ Dashboard açma hatası: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }
} 