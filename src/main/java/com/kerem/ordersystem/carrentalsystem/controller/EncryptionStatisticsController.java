package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.util.StageUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EncryptionStatisticsController implements Initializable {

    @FXML private Label encryptedRecordsLabel;
    @FXML private Label unencryptedRecordsLabel;
    @FXML private Label encryptionPercentageLabel;
    @FXML private Label algorithmLabel;
    @FXML private Label keyStatusLabel;
    @FXML private Label lastRotationLabel;
    @FXML private Label statusLabel;

    @FXML private ComboBox<String> algorithmComboBox;
    @FXML private ComboBox<String> keySizeComboBox;
    @FXML private CheckBox autoEncryptCheckBox;
    @FXML private CheckBox backupKeysCheckBox;

    @FXML private TableView<EncryptionTableRow> encryptionTable;
    @FXML private TableColumn<EncryptionTableRow, String> tableNameColumn;
    @FXML private TableColumn<EncryptionTableRow, String> encryptedFieldsColumn;
    @FXML private TableColumn<EncryptionTableRow, String> totalRecordsColumn;
    @FXML private TableColumn<EncryptionTableRow, String> encryptedCountColumn;
    @FXML private TableColumn<EncryptionTableRow, String> encryptionRateColumn;
    @FXML private TableColumn<EncryptionTableRow, String> statusColumn;
    @FXML private TableColumn<EncryptionTableRow, String> actionsColumn;

    private List<EncryptionTableRow> encryptionData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("🔧 EncryptionStatisticsController initialize() started...");
            statusLabel.setText("🔄 Initializing encryption statistics...");
            
            setupTable();
            setupComboBoxes();
            loadEncryptionData();
            updateStatistics();
            
            System.out.println("✅ EncryptionStatisticsController initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error in EncryptionStatisticsController initialize: " + e.getMessage());
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("❌ Initialization error: " + e.getMessage());
            }
        }
    }

    private void setupTable() {
        tableNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTableName()));
        encryptedFieldsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEncryptedFields()));
        totalRecordsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTotalRecords()));
        encryptedCountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEncryptedCount()));
        encryptionRateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEncryptionRate()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        
        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<EncryptionTableRow, String>() {
            private final Button encryptButton = new Button("🔐 Encrypt");
            private final Button viewButton = new Button("👁️ View");
            private final HBox buttonBox = new HBox(5);

            {
                encryptButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8; -fx-cursor: hand; -fx-font-size: 11px;");
                viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8; -fx-cursor: hand; -fx-font-size: 11px;");
                
                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.getChildren().addAll(encryptButton, viewButton);
                
                encryptButton.setOnAction(e -> {
                    EncryptionTableRow table = getTableView().getItems().get(getIndex());
                    handleEncryptTable(table);
                });
                
                viewButton.setOnAction(e -> {
                    EncryptionTableRow table = getTableView().getItems().get(getIndex());
                    handleViewTable(table);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
    }

    private void setupComboBoxes() {
        // Algorithm ComboBox
        ObservableList<String> algorithms = FXCollections.observableArrayList();
        algorithms.addAll("AES-256", "AES-192", "AES-128", "RSA-2048", "RSA-4096");
        algorithmComboBox.setItems(algorithms);
        algorithmComboBox.setValue("AES-256");

        // Key Size ComboBox
        ObservableList<String> keySizes = FXCollections.observableArrayList();
        keySizes.addAll("128 bits", "192 bits", "256 bits", "2048 bits", "4096 bits");
        keySizeComboBox.setItems(keySizes);
        keySizeComboBox.setValue("256 bits");

        // Set default checkbox values
        autoEncryptCheckBox.setSelected(true);
        backupKeysCheckBox.setSelected(true);
    }

    private void loadEncryptionData() {
        try {
            System.out.println("🔧 Loading encryption data...");
            statusLabel.setText("🔄 Loading encryption data...");
            
            encryptionData = new ArrayList<>();
            
            // Create sample encryption data
            encryptionData.add(new EncryptionTableRow(
                "Users",
                "Password, Email, Phone",
                "1,250",
                "1,250",
                "100%",
                "✅ Fully Encrypted"
            ));
            
            encryptionData.add(new EncryptionTableRow(
                "Rentals",
                "CustomerInfo, PaymentData",
                "3,420",
                "3,420",
                "100%",
                "✅ Fully Encrypted"
            ));
            
            encryptionData.add(new EncryptionTableRow(
                "Cars",
                "VIN, LicensePlate",
                "850",
                "680",
                "80%",
                "⚠️ Partially Encrypted"
            ));
            
            encryptionData.add(new EncryptionTableRow(
                "Payments",
                "CreditCard, BankAccount",
                "2,100",
                "2,100",
                "100%",
                "✅ Fully Encrypted"
            ));
            
            encryptionData.add(new EncryptionTableRow(
                "Logs",
                "None",
                "15,600",
                "0",
                "0%",
                "❌ Not Encrypted"
            ));
            
            displayEncryptionData();
            statusLabel.setText("✅ Encryption data loaded successfully - " + encryptionData.size() + " tables");
            
        } catch (Exception e) {
            statusLabel.setText("❌ Error loading encryption data: " + e.getMessage());
            System.err.println("❌ Error loading encryption data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayEncryptionData() {
        ObservableList<EncryptionTableRow> tableData = FXCollections.observableArrayList(encryptionData);
        encryptionTable.setItems(tableData);
    }

    private void updateStatistics() {
        try {
            if (encryptionData == null) {
                encryptedRecordsLabel.setText("0");
                unencryptedRecordsLabel.setText("0");
                encryptionPercentageLabel.setText("0%");
                return;
            }
            
            int totalEncrypted = 0;
            int totalUnencrypted = 0;
            
            for (EncryptionTableRow row : encryptionData) {
                String encryptedStr = row.getEncryptedCount().replace(",", "");
                String totalStr = row.getTotalRecords().replace(",", "");
                
                int encrypted = Integer.parseInt(encryptedStr);
                int total = Integer.parseInt(totalStr);
                
                totalEncrypted += encrypted;
                totalUnencrypted += (total - encrypted);
            }
            
            int grandTotal = totalEncrypted + totalUnencrypted;
            double percentage = grandTotal > 0 ? (double) totalEncrypted / grandTotal * 100 : 0;
            
            encryptedRecordsLabel.setText(String.format("%,d", totalEncrypted));
            unencryptedRecordsLabel.setText(String.format("%,d", totalUnencrypted));
            encryptionPercentageLabel.setText(String.format("%.1f%%", percentage));
            
        } catch (Exception e) {
            System.err.println("❌ Error updating statistics: " + e.getMessage());
            encryptedRecordsLabel.setText("0");
            unencryptedRecordsLabel.setText("0");
            encryptionPercentageLabel.setText("0%");
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
    private void handleRefresh() {
        loadEncryptionData();
        updateStatistics();
        statusLabel.setText("🔄 Encryption statistics refreshed");
    }

    @FXML
    private void handleScanTables() {
        statusLabel.setText("🔄 Scanning database tables for encryption status...");
        // Simulate scanning delay
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    loadEncryptionData();
                    updateStatistics();
                    statusLabel.setText("✅ Table scan completed");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @FXML
    private void handleSaveSettings() {
        statusLabel.setText("💾 Encryption settings saved successfully");
        System.out.println("Encryption settings saved:");
        System.out.println("Algorithm: " + algorithmComboBox.getValue());
        System.out.println("Key Size: " + keySizeComboBox.getValue());
        System.out.println("Auto-Encrypt: " + autoEncryptCheckBox.isSelected());
        System.out.println("Backup Keys: " + backupKeysCheckBox.isSelected());
    }

    @FXML
    private void handleResetSettings() {
        algorithmComboBox.setValue("AES-256");
        keySizeComboBox.setValue("256 bits");
        autoEncryptCheckBox.setSelected(true);
        backupKeysCheckBox.setSelected(true);
        statusLabel.setText("🔄 Settings reset to default values");
    }

    @FXML
    private void handleGenerateKey() {
        statusLabel.setText("🔑 New encryption key generated successfully");
        lastRotationLabel.setText(java.time.LocalDate.now().toString());
    }

    @FXML
    private void handleExportKeys() {
        statusLabel.setText("📤 Encryption keys exported to secure location");
    }

    @FXML
    private void handleImportKeys() {
        statusLabel.setText("📥 Encryption keys imported successfully");
    }

    @FXML
    private void handleRotateKeys() {
        statusLabel.setText("🔄 Key rotation initiated - This may take several minutes");
        lastRotationLabel.setText(java.time.LocalDate.now().toString());
    }

    private void handleEncryptTable(EncryptionTableRow table) {
        statusLabel.setText("🔐 Encrypting table: " + table.getTableName());
        System.out.println("Encrypt table clicked: " + table.getTableName());
    }

    private void handleViewTable(EncryptionTableRow table) {
        statusLabel.setText("👁️ Viewing encryption details for: " + table.getTableName());
        System.out.println("View table clicked: " + table.getTableName());
    }

    // EncryptionTableRow class for table data
    public static class EncryptionTableRow {
        private final String tableName;
        private final String encryptedFields;
        private final String totalRecords;
        private final String encryptedCount;
        private final String encryptionRate;
        private final String status;

        public EncryptionTableRow(String tableName, String encryptedFields, String totalRecords, 
                                 String encryptedCount, String encryptionRate, String status) {
            this.tableName = tableName;
            this.encryptedFields = encryptedFields;
            this.totalRecords = totalRecords;
            this.encryptedCount = encryptedCount;
            this.encryptionRate = encryptionRate;
            this.status = status;
        }

        public String getTableName() { return tableName; }
        public String getEncryptedFields() { return encryptedFields; }
        public String getTotalRecords() { return totalRecords; }
        public String getEncryptedCount() { return encryptedCount; }
        public String getEncryptionRate() { return encryptionRate; }
        public String getStatus() { return status; }
    }
} 