package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChangePasswordController {

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    private static int currentUserId = 1; // Will be set from login session

    // Method to set current user ID from login session
    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    // Method to get current user ID
    public static int getCurrentUserId() {
        return currentUserId;
    }

    @FXML
    private void handleChangePassword() {
        String currentPass = currentPasswordField.getText().trim();
        String newPass = newPasswordField.getText().trim();
        String confirmPass = confirmPasswordField.getText().trim();

        // Validation
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            statusLabel.setText("❗ Tüm alanlar zorunludur.");
            statusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            statusLabel.setText("❗ Yeni şifreler eşleşmiyor.");
            statusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (newPass.length() < 8) {
            statusLabel.setText("❗ Yeni şifre en az 8 karakter olmalıdır.");
            statusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (currentPass.equals(newPass)) {
            statusLabel.setText("❗ Yeni şifre mevcut şifreden farklı olmalıdır.");
            statusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        // Database operations
        String checkSql = "SELECT Password FROM Users WHERE UserID = ?";
        String updateSql = "UPDATE Users SET Password = ? WHERE UserID = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, currentUserId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("Password");
                
                // Verify current password using BCrypt
                if (!BCrypt.checkpw(currentPass, storedPassword)) {
                    statusLabel.setText("❌ Mevcut şifre yanlış.");
                    statusLabel.setStyle("-fx-text-fill: #f44336;");
                    return;
                }

                // Hash new password
                String hashedNewPassword = BCrypt.hashpw(newPass, BCrypt.gensalt());

                // Update password
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, hashedNewPassword);
                    updateStmt.setInt(2, currentUserId);
                    int rows = updateStmt.executeUpdate();

                    if (rows > 0) {
                        statusLabel.setText("✅ Şifre başarıyla güncellendi.");
                        statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                        clearFields();
                    } else {
                        statusLabel.setText("❌ Şifre güncelleme başarısız.");
                        statusLabel.setStyle("-fx-text-fill: #f44336;");
                    }
                }
            } else {
                statusLabel.setText("❌ Kullanıcı bulunamadı.");
                statusLabel.setStyle("-fx-text-fill: #f44336;");
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Veritabanı hatası: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
        statusLabel.setText("🔄 Form temizlendi.");
        statusLabel.setStyle("-fx-text-fill: #42A5F5;");
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/customer-dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) currentPasswordField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Customer Dashboard");
        } catch (IOException e) {
            statusLabel.setText("❌ Dashboard açma hatası: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    private void clearFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
} 