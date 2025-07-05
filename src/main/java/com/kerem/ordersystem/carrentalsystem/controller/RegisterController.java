package com.kerem.ordersystem.carrentalsystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.kerem.ordersystem.carrentalsystem.service.CarRentalService;
import com.kerem.ordersystem.carrentalsystem.model.Customer;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class RegisterController {
    
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;
    @FXML private TextField driverLicenseField;
    @FXML private DatePicker birthDatePicker;
    @FXML private CheckBox agreeTermsCheckBox;
    @FXML private Label messageLabel;
    
    private CarRentalService carRentalService;
    
    public RegisterController() {
        try {
            this.carRentalService = new CarRentalService();
        } catch (Exception e) {
            System.err.println("Error initializing CarRentalService in RegisterController: " + e.getMessage());
        }
    }
    
    @FXML
    private void initialize() {
        // Clear any previous messages
        if (messageLabel != null) {
            messageLabel.setText("");
        }
        
        // Ensure all fields are editable and focusable
        if (fullNameField != null) {
            fullNameField.setPromptText("Adınız ve soyadınız...");
            fullNameField.setEditable(true);
            fullNameField.setDisable(false);
            fullNameField.setFocusTraversable(true);
        }
        if (emailField != null) {
            emailField.setPromptText("ornek@email.com");
            emailField.setEditable(true);
            emailField.setDisable(false);
            emailField.setFocusTraversable(true);
        }
        if (usernameField != null) {
            usernameField.setPromptText("Benzersiz kullanıcı adı...");
            usernameField.setEditable(true);
            usernameField.setDisable(false);
            usernameField.setFocusTraversable(true);
        }
        if (passwordField != null) {
            passwordField.setPromptText("En az 6 karakter...");
            passwordField.setEditable(true);
            passwordField.setDisable(false);
            passwordField.setFocusTraversable(true);
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setPromptText("Şifrenizi tekrar girin...");
            confirmPasswordField.setEditable(true);
            confirmPasswordField.setDisable(false);
            confirmPasswordField.setFocusTraversable(true);
        }
        if (phoneField != null) {
            phoneField.setPromptText("+90 5XX XXX XX XX");
            phoneField.setEditable(true);
            phoneField.setDisable(false);
            phoneField.setFocusTraversable(true);
        }
        if (driverLicenseField != null) {
            driverLicenseField.setPromptText("Ehliyet numaranız...");
            driverLicenseField.setEditable(true);
            driverLicenseField.setDisable(false);
            driverLicenseField.setFocusTraversable(true);
        }
        if (birthDatePicker != null) {
            birthDatePicker.setEditable(true);
            birthDatePicker.setDisable(false);
            birthDatePicker.setFocusTraversable(true);
        }
    }
    
    @FXML
    private void handleRegister() {
        if (!validateForm()) {
            return;
        }
        
        try {
            // Use the registerCustomer method from CarRentalService
            boolean success = carRentalService.registerCustomer(
                1, // Default userId - in a real app, you'd create a User first
                fullNameField.getText().trim(),
                phoneField.getText().trim(),
                "", // Default address - could add an address field
                driverLicenseField.getText().trim(),
                birthDatePicker.getValue()
            );
            
            if (success) {
                showMessage("✅ Hesap başarıyla oluşturuldu! Giriş yapabilirsiniz.", "success");
                
                // Wait a bit and then redirect to login
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(this::handleBackToLogin);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
                
            } else {
                showMessage("❌ Hesap oluşturulurken hata oluştu!", "error");
            }
            
        } catch (Exception e) {
            showMessage("❌ Hata: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBackToLogin() {
        try {
            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("🚗 Car Rental Management System - Giriş");
            
        } catch (Exception e) {
            showMessage("❌ Hata: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }
    
    private boolean validateForm() {
        // Name validation
        if (fullNameField.getText().trim().isEmpty()) {
            showMessage("❌ Ad soyad boş olamaz!", "error");
            fullNameField.requestFocus();
            return false;
        }
        
        if (fullNameField.getText().trim().length() < 3) {
            showMessage("❌ Ad soyad en az 3 karakter olmalı!", "error");
            fullNameField.requestFocus();
            return false;
        }
        
        // Email validation
        if (emailField.getText().trim().isEmpty()) {
            showMessage("❌ E-posta boş olamaz!", "error");
            emailField.requestFocus();
            return false;
        }
        
        if (!isValidEmail(emailField.getText().trim())) {
            showMessage("❌ Geçerli bir e-posta adresi girin!", "error");
            emailField.requestFocus();
            return false;
        }
        
        // Username validation
        if (usernameField.getText().trim().isEmpty()) {
            showMessage("❌ Kullanıcı adı boş olamaz!", "error");
            usernameField.requestFocus();
            return false;
        }
        
        if (usernameField.getText().trim().length() < 3) {
            showMessage("❌ Kullanıcı adı en az 3 karakter olmalı!", "error");
            usernameField.requestFocus();
            return false;
        }
        
        // Password validation
        if (passwordField.getText().isEmpty()) {
            showMessage("❌ Şifre boş olamaz!", "error");
            passwordField.requestFocus();
            return false;
        }
        
        if (passwordField.getText().length() < 6) {
            showMessage("❌ Şifre en az 6 karakter olmalı!", "error");
            passwordField.requestFocus();
            return false;
        }
        
        // Confirm password validation
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showMessage("❌ Şifreler eşleşmiyor!", "error");
            confirmPasswordField.requestFocus();
            return false;
        }
        
        // Phone validation
        if (phoneField.getText().trim().isEmpty()) {
            showMessage("❌ Telefon numarası boş olamaz!", "error");
            phoneField.requestFocus();
            return false;
        }
        
        // Driver license validation
        if (driverLicenseField.getText().trim().isEmpty()) {
            showMessage("❌ Ehliyet numarası boş olamaz!", "error");
            driverLicenseField.requestFocus();
            return false;
        }
        
        // Birth date validation
        if (birthDatePicker.getValue() == null) {
            showMessage("❌ Doğum tarihi seçin!", "error");
            birthDatePicker.requestFocus();
            return false;
        }
        
        // Age validation (must be at least 18)
        LocalDate today = LocalDate.now();
        LocalDate birthDate = birthDatePicker.getValue();
        if (today.getYear() - birthDate.getYear() < 18) {
            showMessage("❌ En az 18 yaşında olmalısınız!", "error");
            birthDatePicker.requestFocus();
            return false;
        }
        
        // Terms acceptance validation
        if (!agreeTermsCheckBox.isSelected()) {
            showMessage("❌ Şartları ve koşulları kabul etmelisiniz!", "error");
            agreeTermsCheckBox.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
    
    private void showMessage(String message, String type) {
        // Print to console if messageLabel is not available
        if (messageLabel == null) {
            System.out.println("[" + type.toUpperCase() + "] " + message);
            return;
        }
        
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("success-message", "error-message", "info-message");
        messageLabel.getStyleClass().add(type + "-message");
        
        // Auto-clear message after 3 seconds for success/info messages
        if (!"error".equals(type)) {
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    javafx.application.Platform.runLater(() -> {
                        if (messageLabel != null) {
                            messageLabel.setText("");
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
} 