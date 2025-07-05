package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.util.StageUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EmailSettingsController implements Initializable {

    @FXML private Label emailStatusLabel;
    @FXML private Label emailsSentLabel;
    @FXML private Label failedEmailsLabel;
    @FXML private Label queueSizeLabel;
    @FXML private Label statusLabel;

    // SMTP Configuration Fields
    @FXML private TextField smtpServerField;
    @FXML private TextField smtpPortField;
    @FXML private TextField smtpUsernameField;
    @FXML private PasswordField smtpPasswordField;
    @FXML private TextField fromNameField;
    @FXML private TextField fromEmailField;
    @FXML private CheckBox enableSSLCheckBox;
    @FXML private CheckBox enableAuthCheckBox;

    // Notification Settings
    @FXML private CheckBox welcomeEmailCheckBox;
    @FXML private CheckBox rentalConfirmationCheckBox;
    @FXML private CheckBox reminderEmailCheckBox;
    @FXML private CheckBox returnReminderCheckBox;
    @FXML private CheckBox paymentConfirmationCheckBox;
    @FXML private CheckBox adminNotificationCheckBox;

    // Email Log Table
    @FXML private TableView<EmailLogEntry> emailLogTable;
    @FXML private TableColumn<EmailLogEntry, String> timestampColumn;
    @FXML private TableColumn<EmailLogEntry, String> recipientColumn;
    @FXML private TableColumn<EmailLogEntry, String> subjectColumn;
    @FXML private TableColumn<EmailLogEntry, String> typeColumn;
    @FXML private TableColumn<EmailLogEntry, String> statusColumn;
    @FXML private TableColumn<EmailLogEntry, String> errorColumn;

    private List<EmailLogEntry> emailLogs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("🔧 EmailSettingsController initialize() started...");
            statusLabel.setText("🔄 Initializing email settings...");
            
            setupTable();
            loadCurrentSettings();
            loadEmailLogs();
            updateStatistics();
            
            System.out.println("✅ EmailSettingsController initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error in EmailSettingsController initialize: " + e.getMessage());
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("❌ Initialization error: " + e.getMessage());
            }
        }
    }

    private void setupTable() {
        timestampColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimestamp()));
        recipientColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRecipient()));
        subjectColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSubject()));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        errorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getError()));
    }

    private void loadCurrentSettings() {
        try {
            // Load current SMTP settings (in a real app, these would come from database/config file)
            smtpServerField.setText("smtp.gmail.com");
            smtpPortField.setText("587");
            smtpUsernameField.setText("carrentalsystem@gmail.com");
            fromNameField.setText("Car Rental System");
            fromEmailField.setText("noreply@carrentalsystem.com");
            
            enableSSLCheckBox.setSelected(true);
            enableAuthCheckBox.setSelected(true);
            
            // Load notification settings
            welcomeEmailCheckBox.setSelected(true);
            rentalConfirmationCheckBox.setSelected(true);
            reminderEmailCheckBox.setSelected(true);
            returnReminderCheckBox.setSelected(true);
            paymentConfirmationCheckBox.setSelected(true);
            adminNotificationCheckBox.setSelected(true);
            
            statusLabel.setText("✅ Current settings loaded");
            
        } catch (Exception e) {
            statusLabel.setText("❌ Error loading settings: " + e.getMessage());
            System.err.println("❌ Error loading settings: " + e.getMessage());
        }
    }

    private void loadEmailLogs() {
        try {
            System.out.println("🔧 Loading email logs...");
            statusLabel.setText("🔄 Loading email logs...");
            
            emailLogs = new ArrayList<>();
            
            // Create sample email log entries
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            
            emailLogs.add(new EmailLogEntry(
                now.minusMinutes(5).format(formatter),
                "keremsaliherol1@gmail.com",
                "Welcome to Car Rental System",
                "Welcome",
                "✅ Sent",
                ""
            ));
            
            emailLogs.add(new EmailLogEntry(
                now.minusMinutes(10).format(formatter),
                "keremsaliherol1@gmail.com",
                "Rental Confirmation - BMW X5",
                "Confirmation",
                "✅ Sent",
                ""
            ));
            
            emailLogs.add(new EmailLogEntry(
                now.minusMinutes(15).format(formatter),
                "test@example.com",
                "Payment Confirmation",
                "Payment",
                "❌ Failed",
                "Invalid email address"
            ));
            
            emailLogs.add(new EmailLogEntry(
                now.minusMinutes(30).format(formatter),
                "admin@carrentalsystem.com",
                "Daily Report",
                "Admin",
                "✅ Sent",
                ""
            ));
            
            emailLogs.add(new EmailLogEntry(
                now.minusMinutes(45).format(formatter),
                "customer@example.com",
                "Return Reminder - Toyota Camry",
                "Reminder",
                "✅ Sent",
                ""
            ));
            
            displayEmailLogs();
            statusLabel.setText("✅ Email logs loaded successfully - " + emailLogs.size() + " entries");
            
        } catch (Exception e) {
            statusLabel.setText("❌ Error loading email logs: " + e.getMessage());
            System.err.println("❌ Error loading email logs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayEmailLogs() {
        ObservableList<EmailLogEntry> tableData = FXCollections.observableArrayList(emailLogs);
        emailLogTable.setItems(tableData);
    }

    private void updateStatistics() {
        try {
            if (emailLogs == null) {
                emailsSentLabel.setText("0");
                failedEmailsLabel.setText("0");
                queueSizeLabel.setText("0");
                return;
            }
            
            int sentEmails = (int) emailLogs.stream().filter(e -> "✅ Sent".equals(e.getStatus())).count();
            int failedEmails = (int) emailLogs.stream().filter(e -> "❌ Failed".equals(e.getStatus())).count();
            int queueSize = 0; // Simulated queue size
            
            emailsSentLabel.setText(String.valueOf(sentEmails));
            failedEmailsLabel.setText(String.valueOf(failedEmails));
            queueSizeLabel.setText(String.valueOf(queueSize));
            
            // Update email service status
            if (failedEmails == 0) {
                emailStatusLabel.setText("Active");
                emailStatusLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1B5E20;");
            } else {
                emailStatusLabel.setText("Issues");
                emailStatusLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #F44336;");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error updating statistics: " + e.getMessage());
            emailsSentLabel.setText("0");
            failedEmailsLabel.setText("0");
            queueSizeLabel.setText("0");
        }
    }

    @FXML
    private void handleBackToSecurity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/security-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Security Dashboard");
        } catch (IOException e) {
            statusLabel.setText("❌ Security Dashboard could not be loaded: " + e.getMessage());
        }
    }

    @FXML
    private void handleTestEmail() {
        statusLabel.setText("📧 Sending test email...");
        
        // Simulate sending test email
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simulate email sending delay
                javafx.application.Platform.runLater(() -> {
                    // Add test email to log
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    emailLogs.add(0, new EmailLogEntry(
                        LocalDateTime.now().format(formatter),
                        smtpUsernameField.getText(),
                        "Test Email from Car Rental System",
                        "Test",
                        "✅ Sent",
                        ""
                    ));
                    displayEmailLogs();
                    updateStatistics();
                    statusLabel.setText("✅ Test email sent successfully!");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @FXML
    private void handleSaveSMTPSettings() {
        try {
            // Validate SMTP settings
            if (smtpServerField.getText().trim().isEmpty()) {
                statusLabel.setText("❌ SMTP Server is required");
                return;
            }
            
            if (smtpPortField.getText().trim().isEmpty()) {
                statusLabel.setText("❌ SMTP Port is required");
                return;
            }
            
            // In a real application, save to database or config file
            statusLabel.setText("💾 SMTP settings saved successfully");
            System.out.println("SMTP Settings saved:");
            System.out.println("Server: " + smtpServerField.getText());
            System.out.println("Port: " + smtpPortField.getText());
            System.out.println("Username: " + smtpUsernameField.getText());
            System.out.println("From Name: " + fromNameField.getText());
            System.out.println("From Email: " + fromEmailField.getText());
            System.out.println("SSL Enabled: " + enableSSLCheckBox.isSelected());
            System.out.println("Auth Enabled: " + enableAuthCheckBox.isSelected());
            
        } catch (Exception e) {
            statusLabel.setText("❌ Error saving SMTP settings: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetSMTPSettings() {
        smtpServerField.setText("smtp.gmail.com");
        smtpPortField.setText("587");
        smtpUsernameField.setText("");
        smtpPasswordField.setText("");
        fromNameField.setText("Car Rental System");
        fromEmailField.setText("noreply@carrentalsystem.com");
        enableSSLCheckBox.setSelected(true);
        enableAuthCheckBox.setSelected(true);
        statusLabel.setText("🔄 SMTP settings reset to default values");
    }

    @FXML
    private void handleSaveNotificationSettings() {
        try {
            // In a real application, save to database or config file
            statusLabel.setText("💾 Notification settings saved successfully");
            System.out.println("Notification Settings saved:");
            System.out.println("Welcome Email: " + welcomeEmailCheckBox.isSelected());
            System.out.println("Rental Confirmation: " + rentalConfirmationCheckBox.isSelected());
            System.out.println("Reminder Email: " + reminderEmailCheckBox.isSelected());
            System.out.println("Return Reminder: " + returnReminderCheckBox.isSelected());
            System.out.println("Payment Confirmation: " + paymentConfirmationCheckBox.isSelected());
            System.out.println("Admin Notification: " + adminNotificationCheckBox.isSelected());
            
        } catch (Exception e) {
            statusLabel.setText("❌ Error saving notification settings: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditWelcomeTemplate() {
        statusLabel.setText("✏️ Welcome email template editor - Coming soon!");
    }

    @FXML
    private void handleEditConfirmationTemplate() {
        statusLabel.setText("✏️ Confirmation email template editor - Coming soon!");
    }

    @FXML
    private void handleEditReminderTemplate() {
        statusLabel.setText("✏️ Reminder email template editor - Coming soon!");
    }

    @FXML
    private void handleRefreshLog() {
        loadEmailLogs();
        updateStatistics();
        statusLabel.setText("🔄 Email log refreshed");
    }

    @FXML
    private void handleClearLog() {
        emailLogs.clear();
        displayEmailLogs();
        updateStatistics();
        statusLabel.setText("🗑️ Email log cleared");
    }

    // EmailLogEntry class for table data
    public static class EmailLogEntry {
        private final String timestamp;
        private final String recipient;
        private final String subject;
        private final String type;
        private final String status;
        private final String error;

        public EmailLogEntry(String timestamp, String recipient, String subject, 
                           String type, String status, String error) {
            this.timestamp = timestamp;
            this.recipient = recipient;
            this.subject = subject;
            this.type = type;
            this.status = status;
            this.error = error;
        }

        public String getTimestamp() { return timestamp; }
        public String getRecipient() { return recipient; }
        public String getSubject() { return subject; }
        public String getType() { return type; }
        public String getStatus() { return status; }
        public String getError() { return error; }
    }
} 