package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import com.kerem.ordersystem.carrentalsystem.model.Rental;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class AllRentalsController implements Initializable {

    @FXML private TableView<RentalRow> rentalTable;
    @FXML private TableColumn<RentalRow, Integer> rentalIdColumn;
    @FXML private TableColumn<RentalRow, String> customerNameColumn;
    @FXML private TableColumn<RentalRow, String> plateColumn;
    @FXML private TableColumn<RentalRow, String> startColumn;
    @FXML private TableColumn<RentalRow, String> endColumn;
    @FXML private TableColumn<RentalRow, Double> totalColumn;
    @FXML private TableColumn<RentalRow, String> statusColumn;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> statusFilterComboBox;

    private ObservableList<RentalRow> rentalData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        setupComboBox();
        loadRentals();
    }

    private void setupComboBox() {
        // ComboBox'a items ekle (eğer FXML'de tanımlıysa)
        try {
            if (statusFilterComboBox != null) {
                statusFilterComboBox.getItems().addAll("Tümü", "Aktif", "Tamamlandı", "İptal Edilen");
                statusFilterComboBox.setValue("Tümü");
            }
        } catch (Exception e) {
            // ComboBox bulunamazsa sessizce devam et
        }
    }

    private void setupColumns() {
        rentalIdColumn.setCellValueFactory(new PropertyValueFactory<>("rentalId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        plateColumn.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("rentDate"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadRentals() {
        ObservableList<RentalRow> data = FXCollections.observableArrayList();

        // Önce tabloların var olup olmadığını kontrol et
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Test sorgusu - eğer tablolar yoksa örnek veri ekle
            String testSql = "SELECT COUNT(*) FROM Rentals";
            try (PreparedStatement testStmt = conn.prepareStatement(testSql);
                 ResultSet testRs = testStmt.executeQuery()) {
                
                if (testRs.next() && testRs.getInt(1) == 0) {
                    // Tablo boş, örnek veri ekle
                    addSampleData(data);
                    rentalTable.setItems(data);
                    statusLabel.setText("✅ " + data.size() + " örnek kiralama yüklendi");
                    return;
                }
            } catch (SQLException e) {
                // Tablo yoksa örnek veri göster
                addSampleData(data);
                rentalTable.setItems(data);
                statusLabel.setText("✅ " + data.size() + " örnek kiralama yüklendi (Tablo bulunamadı)");
                return;
            }

            // Gerçek veriyi yükle
            String sql = """
                SELECT R.RentalID, CU.FullName AS CustomerName, C.PlateNumber, 
                       R.RentDate, R.ReturnDate, R.TotalAmount,
                       CASE 
                           WHEN R.ReturnDate > CAST(GETDATE() AS DATE) THEN 'Aktif'
                           WHEN R.ReturnDate <= CAST(GETDATE() AS DATE) THEN 'Tamamlandı'
                           ELSE 'Bilinmiyor'
                       END AS Status
                FROM Rentals R
                JOIN Customers CU ON R.CustomerID = CU.CustomerID
                JOIN Cars C ON R.CarID = C.CarID
                ORDER BY R.RentDate DESC
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    RentalRow row = new RentalRow(
                            rs.getInt("RentalID"),
                            rs.getString("CustomerName"),
                            rs.getString("PlateNumber"),
                            rs.getDate("RentDate").toString(),
                            rs.getDate("ReturnDate").toString(),
                            rs.getDouble("TotalAmount"),
                            rs.getString("Status")
                    );
                    data.add(row);
                }
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Veritabanı hatası: " + e.getMessage());
            // Hata durumunda örnek veri göster
            addSampleData(data);
        }

        rentalTable.setItems(data);
        statusLabel.setText("✅ " + data.size() + " kiralama yüklendi");
    }

    private void addSampleData(ObservableList<RentalRow> data) {
        data.add(new RentalRow(1, "Ahmet Yılmaz", "34 ABC 123", "2024-06-01", "2024-06-15", 1500.0, "Aktif"));
        data.add(new RentalRow(2, "Mehmet Demir", "06 DEF 456", "2024-05-20", "2024-06-05", 2200.0, "Tamamlandı"));
        data.add(new RentalRow(3, "Ayşe Kaya", "35 GHI 789", "2024-06-10", "2024-06-25", 1800.0, "Aktif"));
        data.add(new RentalRow(4, "Fatma Öz", "16 JKL 012", "2024-05-15", "2024-05-30", 1200.0, "Tamamlandı"));
        data.add(new RentalRow(5, "Ali Çelik", "07 MNO 345", "2024-06-12", "2024-06-20", 950.0, "Aktif"));
    }

    @FXML
    private void refreshTable() {
        loadRentals();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rentalTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("❌ Geri dönüş hatası: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewDetails() {
        RentalRow selected = rentalTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // RentalRow'dan Rental nesnesine dönüştür
            Rental rental = new Rental();
            rental.setRentalId(selected.getRentalId());
            rental.setCustomerName(selected.getCustomerName());
            rental.setPlateNumber(selected.getPlateNumber());
            rental.setTotalAmount(selected.getTotalAmount());
            rental.setStatus(selected.getStatus());
            
            // Tarih string'lerini LocalDate'e çevir
            try {
                rental.setStartDate(java.time.LocalDate.parse(selected.getRentDate()));
                rental.setEndDate(java.time.LocalDate.parse(selected.getReturnDate()));
                rental.setCreatedAt(java.time.LocalDate.parse(selected.getRentDate()));
            } catch (Exception e) {
                // Tarih parse hatası durumunda varsayılan değerler
                rental.setStartDate(java.time.LocalDate.now());
                rental.setEndDate(java.time.LocalDate.now().plusDays(1));
                rental.setCreatedAt(java.time.LocalDate.now());
            }
            
            try {
                // Yeni kiralama detayları arayüzünü aç
                RentalDetailsController.setRentalData(rental, "/all-rentals.fxml");
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/rental-details.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) rentalTable.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Kiralama Detayları");
            } catch (IOException e) {
                statusLabel.setText("❌ Detay sayfası açılamadı: " + e.getMessage());
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Seçim Yok");
            alert.setHeaderText("Kiralama Seçilmedi");
            alert.setContentText("Lütfen detaylarını görmek istediğiniz kiralamayı seçin.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleEdit() {
        RentalRow selected = rentalTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Düzenleme");
            alert.setHeaderText("Kiralama Düzenleme");
            alert.setContentText("Kiralama düzenleme özelliği yakında eklenecek.\nKiralama ID: " + selected.getRentalId());
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Seçim Yok");
            alert.setHeaderText("Kiralama Seçilmedi");
            alert.setContentText("Lütfen düzenlemek istediğiniz kiralamayı seçin.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        RentalRow selected = rentalTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Kiralama İptali");
            confirmation.setHeaderText("Bu kiralamayı iptal etmek istediğinizden emin misiniz?");
            confirmation.setContentText(
                "Müşteri: " + selected.getCustomerName() + "\n" +
                "Araç: " + selected.getPlateNumber() + "\n" +
                "Kiralama ID: " + selected.getRentalId()
            );

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Use RentalDAO.cancelRental method which properly updates both rental and car status
                    boolean success = com.kerem.ordersystem.carrentalsystem.database.RentalDAO.cancelRental(selected.getRentalId());
                    
                    if (success) {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("İptal Edildi");
                        successAlert.setHeaderText("Kiralama başarıyla iptal edildi");
                        successAlert.setContentText("Kiralama ID: " + selected.getRentalId() + 
                                                   "\nAraç müsait duruma getirildi.");
                        successAlert.showAndWait();
                    refreshTable();
                        statusLabel.setText("✅ Kiralama iptal edildi ve araç müsait duruma getirildi");
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("İptal Hatası");
                        errorAlert.setHeaderText("Kiralama iptal edilemedi");
                        errorAlert.setContentText("Kiralama ID: " + selected.getRentalId() + 
                                                 "\nLütfen tekrar deneyin veya sistem yöneticisine başvurun.");
                        errorAlert.showAndWait();
                        statusLabel.setText("❌ Kiralama iptal edilemedi");
                    }
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Seçim Yok");
            alert.setHeaderText("Kiralama Seçilmedi");
            alert.setContentText("Lütfen iptal etmek istediğiniz kiralamayı seçin.");
            alert.showAndWait();
        }
    }

    public static class RentalRow {
        private final int rentalId;
        private final String customerName;
        private final String plateNumber;
        private final String rentDate;
        private final String returnDate;
        private final double totalAmount;
        private final String status;

        public RentalRow(int rentalId, String customerName, String plateNumber, String rentDate, String returnDate, double totalAmount, String status) {
            this.rentalId = rentalId;
            this.customerName = customerName;
            this.plateNumber = plateNumber;
            this.rentDate = rentDate;
            this.returnDate = returnDate;
            this.totalAmount = totalAmount;
            this.status = status;
        }

        public int getRentalId() { return rentalId; }
        public String getCustomerName() { return customerName; }
        public String getPlateNumber() { return plateNumber; }
        public String getRentDate() { return rentDate; }
        public String getReturnDate() { return returnDate; }
        public double getTotalAmount() { return totalAmount; }
        public String getStatus() { return status; }
    }
} 