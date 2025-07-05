package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.RentalDAO;
import com.kerem.ordersystem.carrentalsystem.database.ActivityDAO;
import com.kerem.ordersystem.carrentalsystem.model.Rental;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class ActiveRentalsController implements Initializable {

    @FXML private TableView<Rental> activeRentalsTable;
    @FXML private TableColumn<Rental, String> rentalIdColumn;
    @FXML private TableColumn<Rental, String> customerNameColumn;
    @FXML private TableColumn<Rental, String> customerPhoneColumn;
    @FXML private TableColumn<Rental, String> carInfoColumn;
    @FXML private TableColumn<Rental, String> plateColumn;
    @FXML private TableColumn<Rental, String> startDateColumn;
    @FXML private TableColumn<Rental, String> endDateColumn;
    @FXML private TableColumn<Rental, String> totalAmountColumn;
    @FXML private TableColumn<Rental, String> statusColumn;

    @FXML private Label statusLabel;
    @FXML private Button completeRentalButton;
    
    // Yeni istatistik labelları
    @FXML private Label totalRentalsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label lastUpdateLabel;

    private ObservableList<Rental> activeRentalsList = FXCollections.observableArrayList();
    private RentalDAO rentalDAO = new RentalDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadActiveRentals();
        
        // Tablo seçimi dinleyicisi
        activeRentalsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                completeRentalButton.setDisable(newSelection == null);
            }
        );
        
        // Buton hover efektleri
        setupButtonHoverEffects();
    }

    private void setupTableColumns() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        rentalIdColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getRentalId())));
        customerNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
        customerPhoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerPhone()));
        carInfoColumn.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getCarBrand() + " " + data.getValue().getCarModel()
        ));
        plateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlateNumber()));
        startDateColumn.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getStartDate().format(dateFormatter)
        ));
        endDateColumn.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getEndDate().format(dateFormatter)
        ));
        totalAmountColumn.setCellValueFactory(data -> new SimpleStringProperty(
            "₺" + String.format("%.2f", data.getValue().getTotalAmount())
        ));
        statusColumn.setCellValueFactory(data -> {
            Rental rental = data.getValue();
            java.time.LocalDate today = java.time.LocalDate.now();
            if (rental.getEndDate() != null && rental.getEndDate().isBefore(today)) {
                return new SimpleStringProperty("⚠️ OVERDUE");
            }
            return new SimpleStringProperty("✅ ACTIVE");
        });
        
        // Satır renklendirme ve seçim stili
        activeRentalsTable.setRowFactory(tv -> {
            TableRow<Rental> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldRental, newRental) -> {
                updateRowStyle(row, newRental);
            });
            
            // Seçim durumu değiştiğinde stil güncelle
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                updateRowStyle(row, row.getItem());
            });
            
            return row;
        });
    }
    
    private void updateRowStyle(TableRow<Rental> row, Rental rental) {
        if (rental == null) {
            row.setStyle("");
            return;
        }
        
        java.time.LocalDate today = java.time.LocalDate.now();
        boolean isSelected = row.isSelected();
        
        String baseStyle = "";
        String textColor = "-fx-text-fill: #333;"; // Varsayılan metin rengi
        
        if (rental.getEndDate() != null && rental.getEndDate().isBefore(today)) {
            // Gecikmiş kiralamalar için kırmızı arka plan
            baseStyle = "-fx-background-color: #ffebee; -fx-border-color: #f44336; -fx-border-width: 0 0 0 4;";
        } else if (rental.getEndDate() != null && rental.getEndDate().isEqual(today)) {
            // Bugün biten kiralamalar için sarı arka plan
            baseStyle = "-fx-background-color: #fff8e1; -fx-border-color: #ff9800; -fx-border-width: 0 0 0 4;";
        }
        
        if (isSelected) {
            // Seçili satır için özel stil - koyu mavi arka plan ve beyaz metin
            baseStyle += " -fx-background-color: #1976D2; -fx-text-fill: white;";
        } else {
            // Seçili değilse normal metin rengi
            baseStyle += " " + textColor;
        }
        
        row.setStyle(baseStyle);
    }

    private void loadActiveRentals() {
        activeRentalsList.clear();
        
        System.out.println("🔄 Aktif kiralamalar yükleniyor...");
        
        // Convert Map data to Rental objects
        List<Map<String, Object>> rentalData = rentalDAO.getActiveRentals();
        for (Map<String, Object> data : rentalData) {
            Rental rental = new Rental();
            
            // Temel bilgiler
            rental.setRentalId((Integer) data.get("RentalID"));
            rental.setCustomerId((Integer) data.get("CustomerID"));
            rental.setCarId((Integer) data.get("CarID"));
            
            // Müşteri bilgileri
            rental.setCustomerName((String) data.get("CustomerName"));
            rental.setCustomerPhone((String) data.get("CustomerPhone"));
            
            // Araç bilgileri
            rental.setPlateNumber((String) data.get("PlateNumber"));
            rental.setCarBrand((String) data.get("CarBrand"));
            rental.setCarModel((String) data.get("CarModel"));
            
            // Ödeme bilgileri
            if (data.get("DailyRate") != null) {
                rental.setDailyRate(((Number) data.get("DailyRate")).doubleValue());
            }
            if (data.get("TotalAmount") != null) {
                rental.setTotalAmount(((Number) data.get("TotalAmount")).doubleValue());
            }
            
            // Tarih bilgileri
            if (data.get("RentDate") != null) {
                rental.setStartDate(((java.sql.Date) data.get("RentDate")).toLocalDate());
            }
            if (data.get("ReturnDate") != null) {
                rental.setEndDate(((java.sql.Date) data.get("ReturnDate")).toLocalDate());
            }
            if (data.get("CreatedAt") != null) {
                rental.setCreatedAt(((java.sql.Date) data.get("CreatedAt")).toLocalDate());
            }
            
            // Durum bilgisi
            rental.setStatus((String) data.get("Status"));
            
            System.out.println("✅ Kiralama yüklendi: ID=" + rental.getRentalId() + 
                             ", Müşteri=" + rental.getCustomerName() + 
                             ", Telefon=" + rental.getCustomerPhone() +
                             ", Araç=" + rental.getCarBrand() + " " + rental.getCarModel() +
                             ", Durum=" + rental.getStatus());
            
            activeRentalsList.add(rental);
        }
        
        activeRentalsTable.setItems(activeRentalsList);
        
        // Update statistics
        updateStatistics();
        
        statusLabel.setText("✅ " + activeRentalsList.size() + " active rental" + (activeRentalsList.size() == 1 ? "" : "s") + " listed");
        
        // Set last update time
        if (lastUpdateLabel != null) {
            lastUpdateLabel.setText("Last update: " + java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
            ));
        }
        
        System.out.println("🎯 Total " + activeRentalsList.size() + " active rental" + (activeRentalsList.size() == 1 ? "" : "s") + " loaded");
    }
    
    private void updateStatistics() {
        int totalRentals = activeRentalsList.size();
        double totalRevenue = 0.0;
        
        for (Rental rental : activeRentalsList) {
            totalRevenue += rental.getTotalAmount();
        }
        
        // Update statistic labels
        if (totalRentalsLabel != null) {
            totalRentalsLabel.setText(String.valueOf(totalRentals));
        }
        if (totalRevenueLabel != null) {
            totalRevenueLabel.setText("₺" + String.format("%.2f", totalRevenue));
        }
    }

    @FXML
    private void handleCompleteRental() {
        Rental selectedRental = activeRentalsTable.getSelectionModel().getSelectedItem();
        if (selectedRental == null) {
            statusLabel.setText("❌ Please select a rental to return!");
            return;
        }

        // Modern confirmation dialog
        createCustomCompletionConfirmationDialog(selectedRental);
    }

        private void createCustomCompletionConfirmationDialog(Rental rental) {
        // Create main dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🚗 Rental Return Confirmation");
        dialog.setHeaderText(null);

        // Get dialog pane and apply style
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E0E0E0; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12;"
        );

        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setAlignment(Pos.CENTER);

        // Title icon and title
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER);
        
        Label successIcon = new Label("✅");
        successIcon.setStyle("-fx-font-size: 48px;");
        
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("RETURN CAR");
        titleLabel.setStyle(
            "-fx-font-size: 24px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32;"
        );
        
        Label subtitleLabel = new Label("Rental will be completed");
        subtitleLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #666; " +
            "-fx-font-style: italic;"
        );
        
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        headerBox.getChildren().addAll(successIcon, titleBox);

        // Rental information card
        VBox rentalInfoCard = new VBox(15);
        rentalInfoCard.setStyle(
            "-fx-background-color: #F5F5F5; " +
            "-fx-border-color: #E0E0E0; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 20;"
        );

        Label cardTitle = new Label("Rental Information to be Returned:");
        cardTitle.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #333;"
        );

        // Rental details
        VBox detailsBox = new VBox(8);
        
        HBox customerBox = createInfoRow("👤", "Customer:", rental.getCustomerName());
        HBox phoneBox = createInfoRow("📞", "Phone:", rental.getCustomerPhone());
        HBox carBox = createInfoRow("🚗", "Car:", rental.getCarBrand() + " " + rental.getCarModel());
        HBox plateBox = createInfoRow("🔢", "Plate:", rental.getPlateNumber());
        HBox startDateBox = createInfoRow("📅", "Start Date:", rental.getStartDate().toString());
        HBox endDateBox = createInfoRow("📅", "End Date:", rental.getEndDate().toString());
        HBox amountBox = createInfoRow("💰", "Total Amount:", String.format("₺%.2f", rental.getTotalAmount()));

        detailsBox.getChildren().addAll(customerBox, phoneBox, carBox, plateBox, startDateBox, endDateBox, amountBox);
        rentalInfoCard.getChildren().addAll(cardTitle, detailsBox);

        // Warning message
        HBox warningBox = new HBox(10);
        warningBox.setAlignment(Pos.CENTER_LEFT);
        warningBox.setStyle(
            "-fx-background-color: #FFF3E0; " +
            "-fx-border-color: #FF9800; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 15;"
        );

        Label warningIconSmall = new Label("⚠️");
        warningIconSmall.setStyle("-fx-font-size: 16px;");

        VBox warningTextBox = new VBox(3);
        Label warningTitle = new Label("Important Information");
        warningTitle.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #E65100; " +
            "-fx-font-size: 12px;"
        );

        Label warningText = new Label("This rental will be marked as completed and the car will become available.");
        warningText.setStyle(
            "-fx-text-fill: #BF360C; " +
            "-fx-font-size: 11px;"
        );
        warningText.setWrapText(true);

        warningTextBox.getChildren().addAll(warningTitle, warningText);
        warningBox.getChildren().addAll(warningIconSmall, warningTextBox);

        // Confirmation question
        Label confirmationQuestion = new Label("Are you sure you want to return this rental?");
        confirmationQuestion.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #333; " +
            "-fx-text-alignment: center;"
        );
        confirmationQuestion.setAlignment(Pos.CENTER);

        // Add to main container
        mainContainer.getChildren().addAll(
            headerBox, 
            rentalInfoCard, 
            warningBox, 
            confirmationQuestion
        );

        dialogPane.setContent(mainContainer);

        // Customize buttons
        ButtonType completeButton = new ButtonType("✅ Yes, Return Car", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("❌ Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(completeButton, cancelButton);

        // Customize button styles
        dialogPane.lookupButton(completeButton).setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        dialogPane.lookupButton(cancelButton).setStyle(
            "-fx-background-color: #757575; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        // Set dialog size
        dialog.setResizable(false);
        dialogPane.setPrefWidth(500);

        // Handle result
        dialog.showAndWait().ifPresent(response -> {
            if (response == completeButton) {
                performCompleteRental(rental);
            }
        });
    }

    private HBox createInfoRow(String icon, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");
        iconLabel.setPrefWidth(25);

        Label labelText = new Label(label);
        labelText.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #555; " +
            "-fx-font-size: 12px;"
        );
        labelText.setPrefWidth(100);

        Label valueText = new Label(value);
        valueText.setStyle(
            "-fx-text-fill: #333; " +
            "-fx-font-size: 12px;"
        );

        row.getChildren().addAll(iconLabel, labelText, valueText);
        return row;
    }

    private VBox createDetailSection(String title, String color) {
        VBox section = new VBox(10);
        section.setStyle(
            "-fx-background-color: #FAFAFA; " +
            "-fx-border-color: " + color + "; " +
            "-fx-border-width: 0 0 0 4; " +
            "-fx-padding: 15; " +
            "-fx-background-radius: 6;"
        );

        Label sectionTitle = new Label(title);
        sectionTitle.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: " + color + ";"
        );

        section.getChildren().add(sectionTitle);
        return section;
    }

    private HBox createEnhancedInfoRow(String icon, String label, String value, String textColor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 5 0;");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        iconLabel.setPrefWidth(30);

        Label labelText = new Label(label);
        labelText.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #666; " +
            "-fx-font-size: 13px;"
        );
        labelText.setPrefWidth(120);

        Label valueText = new Label(value != null ? value : "Bilinmiyor");
        valueText.setStyle(
            "-fx-text-fill: " + textColor + "; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: " + (textColor.equals("#2E7D32") ? "bold" : "normal") + ";"
        );
        valueText.setWrapText(true);

        row.getChildren().addAll(iconLabel, labelText, valueText);
        return row;
    }

    private Label createActionItem(String text) {
        Label actionItem = new Label(text);
        actionItem.setStyle(
            "-fx-text-fill: #388E3C; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 2 0;"
        );
        return actionItem;
    }

    private void performCompleteRental(Rental rental) {
        boolean success = rentalDAO.completeRental(rental.getRentalId());
        
        if (success) {
            // Başarı dialog'unu göster
            showCompletionSuccessDialog(rental);
            
            // Aktivite kaydı oluştur
            ActivityDAO.logCarReturned(
                rental.getCustomerName(),
                rental.getPlateNumber(),
                rental.getCarBrand(),
                rental.getCarModel()
            );
            
            // Ödeme alındı aktivitesi
            ActivityDAO.logPaymentReceived(
                rental.getTotalAmount(),
                rental.getCustomerName()
            );
            
            loadActiveRentals(); // Listeyi yenile
        } else {
            statusLabel.setText("❌ Kiralama teslim alınamadı!");
            showErrorDialog("Teslim Alma Hatası", "Kiralama teslim alınamadı. Lütfen tekrar deneyin.");
        }
    }

    private void showCompletionSuccessDialog(Rental rental) {
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("🎉 Return Successful!");
        successAlert.setHeaderText(null);

        // Customize dialog pane
        DialogPane dialogPane = successAlert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #E8F5E8, #F1F8E9); " +
            "-fx-border-color: #4CAF50; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 15; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.3), 15, 0, 0, 5);"
        );

        // Create content
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(25));

        // Success icon and title
        VBox headerBox = new VBox(15);
        headerBox.setAlignment(Pos.CENTER);
        
        Label successIcon = new Label("🎉");
        successIcon.setStyle("-fx-font-size: 72px;");
        
        Label titleLabel = new Label("RETURN COMPLETED!");
        titleLabel.setStyle(
            "-fx-font-size: 24px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32;"
        );
        
        Label subtitleLabel = new Label("Car successfully returned");
        subtitleLabel.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-text-fill: #388E3C; " +
            "-fx-font-style: italic;"
        );
        
        headerBox.getChildren().addAll(successIcon, titleLabel, subtitleLabel);

        // Summary information
        VBox summaryBox = new VBox(10);
        summaryBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #4CAF50; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 20;"
        );
        
        Label summaryTitle = new Label("📋 Transaction Summary");
        summaryTitle.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32;"
        );
        
        VBox summaryDetails = new VBox(8);
        summaryDetails.getChildren().addAll(
            createSummaryRow("👤", "Customer:", rental.getCustomerName()),
            createSummaryRow("🚗", "Car:", rental.getCarBrand() + " " + rental.getCarModel()),
            createSummaryRow("🔢", "Plate:", rental.getPlateNumber()),
            createSummaryRow("💰", "Amount:", String.format("₺%.2f", rental.getTotalAmount())),
            createSummaryRow("📅", "Return Date:", java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.ENGLISH)
            ))
        );
        
        summaryBox.getChildren().addAll(summaryTitle, summaryDetails);

        // Success message
        Label successMessage = new Label("✅ Car has been made available and all records have been updated.");
        successMessage.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #2E7D32; " +
            "-fx-font-weight: bold; " +
            "-fx-text-alignment: center;"
        );
        successMessage.setWrapText(true);

        content.getChildren().addAll(headerBox, summaryBox, successMessage);
        dialogPane.setContent(content);

        // Customize button style
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("✅ OK");
        okButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #4CAF50, #66BB6A); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 12 25; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.4), 10, 0, 0, 3);"
        );

        dialogPane.setPrefWidth(500);
        successAlert.showAndWait();
        
        statusLabel.setText("✅ Rental successfully returned!");
        statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
    }

    private HBox createSummaryRow(String icon, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");
        iconLabel.setPrefWidth(25);

        Label labelText = new Label(label);
        labelText.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #666; " +
            "-fx-font-size: 13px;"
        );
        labelText.setPrefWidth(100);

        Label valueText = new Label(value);
        valueText.setStyle(
            "-fx-text-fill: #333; " +
            "-fx-font-size: 13px;"
        );

        row.getChildren().addAll(iconLabel, labelText, valueText);
        return row;
    }

    private void showErrorDialog(String title, String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(message);
        
        DialogPane dialogPane = errorAlert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #FFEBEE; " +
            "-fx-border-color: #F44336; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10;"
        );
        
        errorAlert.showAndWait();
    }

    @FXML
    private void handleRefreshRentals() {
        loadActiveRentals();
    }



    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
        } catch (IOException e) {
            statusLabel.setText("❌ Dashboard could not be loaded: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewRentalDetails() {
        Rental selectedRental = activeRentalsTable.getSelectionModel().getSelectedItem();
        if (selectedRental == null) {
            statusLabel.setText("❌ Please select a rental to view details!");
            return;
        }
        
        try {
            System.out.println("DEBUG: Opening rental details...");
            System.out.println("DEBUG: Selected rental ID: " + selectedRental.getRentalId());
            
            // Open new rental details interface
            RentalDetailsController.setRentalData(selectedRental, "/active-rentals.fxml");
            
            System.out.println("DEBUG: RentalDetailsController.setRentalData called");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/rental-details.fxml"));
            System.out.println("DEBUG: FXMLLoader created");
            
            Parent root = loader.load();
            System.out.println("DEBUG: FXML loaded");
            
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Rental Details");
            
            System.out.println("DEBUG: Scene changed");
            
        } catch (IOException e) {
            System.err.println("DEBUG: IOException: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("❌ Detail page could not be opened: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("DEBUG: Exception: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("❌ Unexpected error: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleExportToExcel() {
        statusLabel.setText("📊 Excel export feature will be added soon...");
    }
    
    @FXML
    private void handlePrintReport() {
        statusLabel.setText("🖨️ Report printing feature will be added soon...");
    }
    
    private void setupButtonHoverEffects() {
        // Return Car button hover effect
        if (completeRentalButton != null) {
            completeRentalButton.setOnMouseEntered(e -> {
                if (!completeRentalButton.isDisabled()) {
                    completeRentalButton.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #66BB6A, #4CAF50); -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(76,175,80,0.6), 15, 0, 0, 5); -fx-border-radius: 10; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
                }
            });
            completeRentalButton.setOnMouseExited(e -> {
                if (!completeRentalButton.isDisabled()) {
                    completeRentalButton.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to right, #4CAF50, #388E3C); -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(76,175,80,0.4), 12, 0, 0, 4); -fx-border-radius: 10; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
                }
            });
        }
    }
} 