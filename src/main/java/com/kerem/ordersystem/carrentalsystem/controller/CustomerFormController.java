package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import com.kerem.ordersystem.carrentalsystem.model.Customer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class CustomerFormController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField licenseField;
    @FXML private DatePicker dobPicker;
    @FXML private Label statusLabel;

    private Customer customerToEdit;

    public void setCustomerToEdit(Customer customer) {
        this.customerToEdit = customer;
        // Alanlara veriyi doldur
        nameField.setText(customer.getFullName());
        phoneField.setText(customer.getPhone());
        addressField.setText(customer.getAddress());
        licenseField.setText(customer.getDriverLicenseNo());
        dobPicker.setValue(customer.getDateOfBirth());
    }

    @FXML
    private void updateCustomer() {
        String fullName = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String license = licenseField.getText().trim();
        
        // Validation
        if (fullName.isEmpty()) {
            statusLabel.setText("❌ Full name is required!");
            return;
        }
        if (phone.isEmpty()) {
            statusLabel.setText("❌ Phone is required!");
            return;
        }
        if (!license.isEmpty() && license.length() < 5) {
            statusLabel.setText("❌ Driver license must be at least 5 characters!");
            return;
        }
        
        String sql = "UPDATE Customers SET FullName=?, Phone=?, Address=?, DriverLicenseNo=?, DateOfBirth=? WHERE CustomerID=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fullName);
            stmt.setString(2, phone);
            stmt.setString(3, address);
            stmt.setString(4, license);
            stmt.setDate(5, Date.valueOf(dobPicker.getValue()));
            stmt.setInt(6, customerToEdit.getCustomerId());

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                statusLabel.setText("✅ Customer updated!");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/customer-view.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) nameField.getScene().getWindow();
                stage.setScene(new Scene(root));
            } else {
                statusLabel.setText("❌ Update failed.");
            }

        } catch (SQLException | IOException e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    @FXML
    private void saveCustomer() {
        String fullName = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String license = licenseField.getText().trim();
        LocalDate dobLocal = dobPicker.getValue();
        Date dob = dobLocal != null ? Date.valueOf(dobLocal) : null;
        
        // Validation
        if (fullName.isEmpty()) {
            statusLabel.setText("❌ Full name is required!");
            return;
        }
        if (phone.isEmpty()) {
            statusLabel.setText("❌ Phone is required!");
            return;
        }
        if (!license.isEmpty() && license.length() < 5) {
            statusLabel.setText("❌ Driver license must be at least 5 characters!");
            return;
        }

        String sql = "INSERT INTO Customers (UserID, FullName, Phone, Address, DriverLicenseNo, DateOfBirth) " +
                "VALUES (NULL, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fullName);
            stmt.setString(2, phone);
            stmt.setString(3, address);
            stmt.setString(4, license);
            stmt.setDate(5, dob);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                statusLabel.setText("✅ Customer saved!");

                // Yönlendirme ve tablo yenileme
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/customer-view.fxml"));
                Parent root = loader.load();

                CustomerViewController controller = loader.getController();
                controller.refreshTable(); // tabloyu yenile

                Stage stage = (Stage) nameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Customer List");
            } else {
                statusLabel.setText("❌ Insert failed.");
            }

        } catch (SQLException | IOException e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }
}
