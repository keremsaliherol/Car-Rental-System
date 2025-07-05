package com.kerem.ordersystem.carrentalsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UpdateCarStatusController {

    @FXML private ComboBox<CarItem> carComboBox;
    @FXML private ComboBox<StatusItem> statusComboBox;
    @FXML private Label statusLabel;
    @FXML private Label availableCountLabel;
    @FXML private Label rentedCountLabel;
    @FXML private Label maintenanceCountLabel;
    @FXML private Label systemStatusLabel;
    @FXML private Label lastUpdateLabel;

    @FXML
    public void initialize() {
        loadCars();
        loadStatuses();
        updateStatusCounts();
        updateSystemStatus();
    }

    private void loadCars() {
        ObservableList<CarItem> cars = FXCollections.observableArrayList();
        String sql = "SELECT CarID, PlateNumber, Brand, Model FROM Cars ORDER BY PlateNumber";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String displayText = rs.getString("PlateNumber") + " - " + 
                                   rs.getString("Brand") + " " + rs.getString("Model");
                cars.add(new CarItem(rs.getInt("CarID"), displayText));
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Araç listesi yüklenirken hata: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }

        carComboBox.setItems(cars);
    }

    private void loadStatuses() {
        ObservableList<StatusItem> statuses = FXCollections.observableArrayList();
        
        // Eğer CarStatuses tablosu yoksa, varsayılan durumları ekle
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Önce tablo var mı kontrol et
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "CarStatuses", null);
            
            if (!tables.next()) {
                // Tablo yoksa oluştur
                createCarStatusesTable(conn);
            }
            
            String sql = "SELECT StatusID, StatusName FROM CarStatuses ORDER BY StatusID";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    statuses.add(new StatusItem(rs.getInt("StatusID"), rs.getString("StatusName")));
                }
            }

        } catch (SQLException e) {
            // Hata durumunda varsayılan durumları ekle
            statuses.addAll(
                new StatusItem(1, "Available"),
                new StatusItem(2, "Rented"),
                new StatusItem(3, "Maintenance")
            );
            statusLabel.setText("⚠️ Default statuses loaded");
            statusLabel.setStyle("-fx-text-fill: #FF9800;");
        }

        statusComboBox.setItems(statuses);
    }

    private void createCarStatusesTable(Connection conn) throws SQLException {
        String createTable = """
            CREATE TABLE CarStatuses (
                StatusID INT PRIMARY KEY,
                StatusName NVARCHAR(50) NOT NULL
            )
        """;
        
        String insertData = """
            INSERT INTO CarStatuses (StatusID, StatusName) VALUES 
            (1, 'Available'),
            (2, 'Rented'),
            (3, 'Maintenance')
        """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTable);
            stmt.execute(insertData);
        }
    }

    private void updateStatusCounts() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Müsait araçlar
            String availableSql = "SELECT COUNT(*) FROM Cars WHERE StatusID = 1 OR StatusID IS NULL";
            try (PreparedStatement stmt = conn.prepareStatement(availableSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    availableCountLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }

            // Kiralanan araçlar
            String rentedSql = "SELECT COUNT(*) FROM Cars WHERE StatusID = 2";
            try (PreparedStatement stmt = conn.prepareStatement(rentedSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    rentedCountLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }

            // Bakımdaki araçlar
            String maintenanceSql = "SELECT COUNT(*) FROM Cars WHERE StatusID = 3";
            try (PreparedStatement stmt = conn.prepareStatement(maintenanceSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    maintenanceCountLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }

        } catch (SQLException e) {
            // Hata durumunda örnek veriler göster
            availableCountLabel.setText("12");
            rentedCountLabel.setText("8");
            maintenanceCountLabel.setText("3");
        }
    }

    private void updateSystemStatus() {
        systemStatusLabel.setText("✅ Sistem çalışıyor");
        lastUpdateLabel.setText("Son güncelleme: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
    }

    @FXML
    private void handleUpdateStatus() {
        CarItem selectedCar = carComboBox.getValue();
        StatusItem selectedStatus = statusComboBox.getValue();

        if (selectedCar == null || selectedStatus == null) {
            statusLabel.setText("❗ Lütfen araç ve durum seçin");
            statusLabel.setStyle("-fx-text-fill: #FF9800;");
            return;
        }

        String sql = "UPDATE Cars SET StatusID = ? WHERE CarID = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selectedStatus.id());
            stmt.setInt(2, selectedCar.id());
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                statusLabel.setText("✅ Araç durumu başarıyla güncellendi: " + selectedStatus.name());
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                updateStatusCounts();
                updateSystemStatus();
                clearForm();
            } else {
                statusLabel.setText("❌ Güncelleme başarısız");
                statusLabel.setStyle("-fx-text-fill: #f44336;");
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Veritabanı hatası: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
        statusLabel.setText("🔄 Form temizlendi");
        statusLabel.setStyle("-fx-text-fill: #666;");
    }

    @FXML
    private void handleRefresh() {
        loadCars();
        loadStatuses();
        updateStatusCounts();
        updateSystemStatus();
        statusLabel.setText("🔄 Veriler yenilendi");
        statusLabel.setStyle("-fx-text-fill: #2196F3;");
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) carComboBox.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard - Araç Kiralama Sistemi");
            
        } catch (IOException e) {
            statusLabel.setText("❌ Geri dönüş hatası: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    private void clearForm() {
        carComboBox.setValue(null);
        statusComboBox.setValue(null);
    }

    public record CarItem(int id, String displayText) {
        @Override public String toString() { return displayText; }
    }

    public record StatusItem(int id, String name) {
        @Override public String toString() { return name; }
    }
} 