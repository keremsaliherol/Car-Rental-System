package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.RentalDAO;
import com.kerem.ordersystem.carrentalsystem.model.Rental;
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
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class RentalDetailsController implements Initializable {

    // Header elements
    @FXML private Label rentalIdLabel;
    
    // Status card
    @FXML private Label statusLabel;
    @FXML private Label createdDateLabel;
    
    // Customer info
    @FXML private Label customerNameLabel;
    @FXML private Label customerPhoneLabel;
    
    // Car info
    @FXML private Label carBrandModelLabel;
    @FXML private Label plateNumberLabel;
    
    // Rental period
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label totalDaysLabel;
    
    // Financial info
    @FXML private Label dailyRateLabel;
    @FXML private Label totalAmountLabel;
    
    // Action buttons
    @FXML private Button completeRentalButton;
    @FXML private Button cancelRentalButton;
    
    // Footer status
    @FXML private Label footerStatusLabel;

    private Rental currentRental;
    private RentalDAO rentalDAO = new RentalDAO();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.ENGLISH);
    private String returnPage = "/admin-dashboard.fxml"; // Default return page

    // Static method to set rental data
    private static Rental rentalToShow;
    private static String returnPagePath;

    public static void setRentalData(Rental rental, String returnPage) {
        rentalToShow = rental;
        returnPagePath = returnPage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (rentalToShow != null) {
            currentRental = rentalToShow;
            returnPage = returnPagePath != null ? returnPagePath : "/admin-dashboard.fxml";
            
            // Tam kiralama bilgilerini veritabanından çek
            loadCompleteRentalData();
            
            loadRentalDetails();
            rentalToShow = null; // Clear static data
            returnPagePath = null;
        }
    }

    private void loadCompleteRentalData() {
        if (currentRental == null || currentRental.getRentalId() <= 0) {
            System.err.println("❌ Geçersiz kiralama ID: " + (currentRental != null ? currentRental.getRentalId() : "null"));
            return;
        }

        try {
            System.out.println("🔄 Tam kiralama bilgileri yükleniyor... ID: " + currentRental.getRentalId());
            Map<String, Object> completeInfo = RentalDAO.getCompleteRentalInfo(currentRental.getRentalId());
            
            if (completeInfo.isEmpty()) {
                System.err.println("❌ Kiralama bilgileri bulunamadı: " + currentRental.getRentalId());
                return;
            }

            // Eksik bilgileri doldur
            if (currentRental.getCustomerName() == null || currentRental.getCustomerName().isEmpty()) {
                currentRental.setCustomerName((String) completeInfo.get("CustomerName"));
            }
            
            if (currentRental.getCustomerPhone() == null || currentRental.getCustomerPhone().isEmpty()) {
                currentRental.setCustomerPhone((String) completeInfo.get("CustomerPhone"));
            }
            
            if (currentRental.getCarBrand() == null || currentRental.getCarBrand().isEmpty()) {
                currentRental.setCarBrand((String) completeInfo.get("CarBrand"));
            }
            
            if (currentRental.getCarModel() == null || currentRental.getCarModel().isEmpty()) {
                currentRental.setCarModel((String) completeInfo.get("CarModel"));
            }
            
            if (currentRental.getPlateNumber() == null || currentRental.getPlateNumber().isEmpty()) {
                currentRental.setPlateNumber((String) completeInfo.get("PlateNumber"));
            }
            
            if (currentRental.getDailyRate() == 0.0) {
                Double dailyRate = (Double) completeInfo.get("DailyRate");
                if (dailyRate != null) {
                    currentRental.setDailyRate(dailyRate);
                }
            }
            
            // Tarih bilgilerini kontrol et
            if (currentRental.getStartDate() == null && completeInfo.get("RentDate") != null) {
                java.sql.Date rentDate = (java.sql.Date) completeInfo.get("RentDate");
                currentRental.setStartDate(rentDate.toLocalDate());
            }
            
            if (currentRental.getEndDate() == null && completeInfo.get("ReturnDate") != null) {
                java.sql.Date returnDate = (java.sql.Date) completeInfo.get("ReturnDate");
                currentRental.setEndDate(returnDate.toLocalDate());
            }
            
            // Toplam tutar kontrolü
            if (currentRental.getTotalAmount() == 0.0) {
                Double totalAmount = (Double) completeInfo.get("TotalAmount");
                if (totalAmount != null) {
                    currentRental.setTotalAmount(totalAmount);
                }
            }

            System.out.println("✅ Tam kiralama bilgileri yüklendi:");
            System.out.println("   Müşteri: " + currentRental.getCustomerName());
            System.out.println("   Telefon: " + currentRental.getCustomerPhone());
            System.out.println("   Araç: " + currentRental.getCarBrand() + " " + currentRental.getCarModel());
            System.out.println("   Plaka: " + currentRental.getPlateNumber());
            System.out.println("   Daily Rate: ₺" + currentRental.getDailyRate());
            System.out.println("   Toplam Tutar: ₺" + currentRental.getTotalAmount());

        } catch (Exception e) {
            System.err.println("❌ Tam kiralama bilgileri yükleme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRentalDetails() {
        if (currentRental == null) {
            footerStatusLabel.setText("❌ Rental data not found");
            footerStatusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        try {
            // Header
            rentalIdLabel.setText("Rental #" + currentRental.getRentalId());

            // Status card
            updateStatusDisplay();
            createdDateLabel.setText(currentRental.getCreatedAt() != null ? 
                currentRental.getCreatedAt().format(dateFormatter) : "Unknown");

            // Customer info
            customerNameLabel.setText(currentRental.getCustomerName() != null ? 
                currentRental.getCustomerName() : "Unknown");
            customerPhoneLabel.setText(currentRental.getCustomerPhone() != null ? 
                currentRental.getCustomerPhone() : "Unknown");

            // Car info
            String carInfo = (currentRental.getCarBrand() != null ? currentRental.getCarBrand() : "") + 
                           " " + (currentRental.getCarModel() != null ? currentRental.getCarModel() : "");
            carBrandModelLabel.setText(carInfo.trim().isEmpty() ? "Unknown" : carInfo.trim());
            plateNumberLabel.setText(currentRental.getPlateNumber() != null ? 
                currentRental.getPlateNumber() : "Unknown");

            // Rental period
            startDateLabel.setText(currentRental.getStartDate() != null ? 
                currentRental.getStartDate().format(dateFormatter) : "Unknown");
            endDateLabel.setText(currentRental.getEndDate() != null ? 
                currentRental.getEndDate().format(dateFormatter) : "Unknown");
            
            int days = currentRental.getRentalDays();
            totalDaysLabel.setText(days + " days");

            // Financial info
            dailyRateLabel.setText("₺" + String.format("%.2f", currentRental.getDailyRate()));
            totalAmountLabel.setText("₺" + String.format("%.2f", currentRental.getTotalAmount()));

            // Button states
            updateButtonStates();

            footerStatusLabel.setText("✅ Rental details loaded successfully");
            footerStatusLabel.setStyle("-fx-text-fill: #4CAF50;");

        } catch (Exception e) {
            footerStatusLabel.setText("❌ Error loading details: " + e.getMessage());
            footerStatusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    private void updateStatusDisplay() {
        String status = currentRental.getStatus();
        java.time.LocalDate today = java.time.LocalDate.now();
        
        if (currentRental.getEndDate() != null && currentRental.getEndDate().isBefore(today)) {
            statusLabel.setText("⚠️ OVERDUE");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-padding: 8 16; -fx-background-color: #FF5722; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold;");
        } else if ("Active".equals(status)) {
            statusLabel.setText("✅ ACTIVE");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-padding: 8 16; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold;");
        } else if ("Completed".equals(status)) {
            statusLabel.setText("✅ COMPLETED");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-padding: 8 16; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold;");
        } else if ("Cancelled".equals(status)) {
            statusLabel.setText("❌ CANCELLED");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-padding: 8 16; -fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold;");
        } else {
            statusLabel.setText(status != null ? status.toUpperCase() : "UNKNOWN");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-padding: 8 16; -fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold;");
        }
    }

    private void updateButtonStates() {
        String status = currentRental.getStatus();
        boolean isActive = "Active".equals(status);
        boolean isCompleted = "Completed".equals(status);
        boolean isCancelled = "Cancelled".equals(status);

        completeRentalButton.setDisable(!isActive);
        cancelRentalButton.setDisable(isCompleted || isCancelled);
    }

    @FXML
    private void handleClose() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(returnPage));
            Parent root = loader.load();
            Stage stage = (Stage) rentalIdLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            
            // Set appropriate title based on return page
            if (returnPage.contains("active-rentals")) {
                stage.setTitle("Aktif Kiralamalar");
            } else if (returnPage.contains("all-rentals")) {
                stage.setTitle("Tüm Kiralamalar");
            } else {
                stage.setTitle("Admin Dashboard");
            }
        } catch (IOException e) {
            footerStatusLabel.setText("❌ Geri dönüş hatası: " + e.getMessage());
            footerStatusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    @FXML
    private void handleCompleteRental() {
        // Modern onay dialogu
        Optional<ButtonType> result = createCustomCompletionConfirmationDialog(currentRental);
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                System.out.println("🔄 Kiralama tamamlanıyor... ID: " + currentRental.getRentalId());
                boolean success = RentalDAO.completeRental(currentRental.getRentalId());
                if (success) {
                    System.out.println("✅ Kiralama başarıyla tamamlandı");
                    currentRental.setStatus("Completed");
                    updateStatusDisplay();
                    updateButtonStates();
                    
                    footerStatusLabel.setText("✅ Kiralama başarıyla tamamlandı");
                    footerStatusLabel.setStyle("-fx-text-fill: #4CAF50;");
                    
                    // Modern success dialog
                    showRentalCompletionSuccessDialog(currentRental);
                } else {
                    System.err.println("❌ Kiralama tamamlama işlemi başarısız");
                    footerStatusLabel.setText("❌ Kiralama tamamlanamadı");
                    footerStatusLabel.setStyle("-fx-text-fill: #f44336;");
                    
                    // Error alert
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Hata");
                    errorAlert.setHeaderText("Tamamlama İşlemi Başarısız");
                    errorAlert.setContentText("Kiralama tamamlanamadı. Lütfen tekrar deneyin veya sistem yöneticisine başvurun.");
                    errorAlert.showAndWait();
                }
            } catch (Exception e) {
                System.err.println("❌ Kiralama tamamlama hatası: " + e.getMessage());
                e.printStackTrace();
                footerStatusLabel.setText("❌ Hata: " + e.getMessage());
                footerStatusLabel.setStyle("-fx-text-fill: #f44336;");
                
                // Error alert
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Sistem Hatası");
                errorAlert.setHeaderText("Beklenmeyen Hata");
                errorAlert.setContentText("Kiralama tamamlanırken bir hata oluştu:\n" + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }



    @FXML
    private void handleCancelRental() {
        // Modern onay dialogu
        createCustomCancellationConfirmationDialog(currentRental);
    }

    private void createCustomCancellationConfirmationDialog(Rental rental) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("❌ Rental Cancellation");
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

        // Warning icon and title
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER);
        
        Label warningIcon = new Label("⚠️");
        warningIcon.setStyle("-fx-font-size: 48px;");
        
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("RENTAL CANCELLATION");
        titleLabel.setStyle(
            "-fx-font-size: 24px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #D32F2F;"
        );
        
        Label subtitleLabel = new Label("This operation cannot be undone!");
        subtitleLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #666; " +
            "-fx-font-style: italic;"
        );
        
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        headerBox.getChildren().addAll(warningIcon, titleBox);

        // Rental information card
        VBox rentalInfoCard = new VBox(15);
        rentalInfoCard.setStyle(
            "-fx-background-color: #F5F5F5; " +
            "-fx-border-color: #E0E0E0; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 20;"
        );

        Label cardTitle = new Label("Rental Information to be Cancelled:");
        cardTitle.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #333;"
        );

        // Rental details
        VBox detailsBox = new VBox(8);
        
        HBox customerBox = createCancelInfoRow("👤", "Customer:", rental.getCustomerName());
        HBox phoneBox = createCancelInfoRow("📞", "Phone:", rental.getCustomerPhone());
        HBox carBox = createCancelInfoRow("🚗", "Car:", rental.getCarBrand() + " " + rental.getCarModel());
        HBox plateBox = createCancelInfoRow("🔢", "Plate:", rental.getPlateNumber());
        HBox startDateBox = createCancelInfoRow("📅", "Start Date:", rental.getStartDate().toString());
        HBox endDateBox = createCancelInfoRow("📅", "End Date:", rental.getEndDate().toString());
        HBox amountBox = createCancelInfoRow("💰", "Total Amount:", String.format("₺%.2f", rental.getTotalAmount()));

        detailsBox.getChildren().addAll(customerBox, phoneBox, carBox, plateBox, startDateBox, endDateBox, amountBox);
        rentalInfoCard.getChildren().addAll(cardTitle, detailsBox);

        // Warning message
        HBox warningBox = new HBox(10);
        warningBox.setAlignment(Pos.CENTER_LEFT);
        warningBox.setStyle(
            "-fx-background-color: #FFEBEE; " +
            "-fx-border-color: #F44336; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 15;"
        );

        Label warningIconSmall = new Label("⚠️");
        warningIconSmall.setStyle("-fx-font-size: 16px;");

        VBox warningTextBox = new VBox(3);
        Label warningTitle = new Label("Important Warning");
        warningTitle.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #D32F2F; " +
            "-fx-font-size: 12px;"
        );

        Label warningText = new Label("This rental will be cancelled and the car will become available. This operation cannot be undone after completion.");
        warningText.setStyle(
            "-fx-text-fill: #C62828; " +
            "-fx-font-size: 11px;"
        );
        warningText.setWrapText(true);

        warningTextBox.getChildren().addAll(warningTitle, warningText);
        warningBox.getChildren().addAll(warningIconSmall, warningTextBox);

        // Confirmation question
        Label confirmationQuestion = new Label("Are you sure you want to cancel this rental?");
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
        ButtonType cancelRentalButton = new ButtonType("❌ Yes, Cancel", ButtonBar.ButtonData.OK_DONE);
        ButtonType keepRentalButton = new ButtonType("✅ No, Continue", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(cancelRentalButton, keepRentalButton);

        // Customize button styles
        dialogPane.lookupButton(cancelRentalButton).setStyle(
            "-fx-background-color: #F44336; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        dialogPane.lookupButton(keepRentalButton).setStyle(
            "-fx-background-color: #4CAF50; " +
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
            if (response == cancelRentalButton) {
                performCancelRental(rental);
            }
        });
    }

    private HBox createCancelInfoRow(String icon, String label, String value) {
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

    private void performCancelRental(Rental rental) {
        try {
            System.out.println("🔄 Kiralama iptal ediliyor... ID: " + rental.getRentalId());
            boolean success = RentalDAO.cancelRental(rental.getRentalId());
            if (success) {
                System.out.println("✅ Kiralama başarıyla iptal edildi");
                currentRental.setStatus("Cancelled");
                updateStatusDisplay();
                updateButtonStates();
                
                footerStatusLabel.setText("✅ Kiralama başarıyla iptal edildi");
                footerStatusLabel.setStyle("-fx-text-fill: #4CAF50;");
                
                // Success notification
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Başarılı");
                successAlert.setHeaderText("Kiralama İptal Edildi");
                successAlert.setContentText("Kiralama başarıyla iptal edildi ve araç müsait duruma getirildi.");
                successAlert.showAndWait();
            } else {
                System.err.println("❌ Kiralama iptal işlemi başarısız");
                footerStatusLabel.setText("❌ Kiralama iptal edilemedi");
                footerStatusLabel.setStyle("-fx-text-fill: #f44336;");
                
                // Error alert
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Hata");
                errorAlert.setHeaderText("İptal İşlemi Başarısız");
                errorAlert.setContentText("Kiralama iptal edilemedi. Lütfen tekrar deneyin veya sistem yöneticisine başvurun.");
                errorAlert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("❌ Kiralama iptal hatası: " + e.getMessage());
            e.printStackTrace();
            footerStatusLabel.setText("❌ Hata: " + e.getMessage());
            footerStatusLabel.setStyle("-fx-text-fill: #f44336;");
            
            // Error alert
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Sistem Hatası");
            errorAlert.setHeaderText("Beklenmeyen Hata");
            errorAlert.setContentText("Kiralama iptal edilirken bir hata oluştu:\n" + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private Optional<ButtonType> createCustomCompletionConfirmationDialog(Rental rental) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("✅ Rental Completion");
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

        // Success icon and title
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER);
        
        Label successIcon = new Label("✅");
        successIcon.setStyle("-fx-font-size: 48px;");
        
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("RENTAL COMPLETION");
        titleLabel.setStyle(
            "-fx-font-size: 24px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #4CAF50;"
        );
        
        Label subtitleLabel = new Label("Mark this rental as completed");
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
            "-fx-background-color: #F8FFF8; " +
            "-fx-border-color: #4CAF50; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 20;"
        );

        Label cardTitle = new Label("Rental Information to be Completed:");
        cardTitle.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32;"
        );

        // Rental details
        VBox detailsBox = new VBox(8);
        
        HBox customerBox = createCompletionConfirmInfoRow("👤", "Customer:", rental.getCustomerName());
        HBox phoneBox = createCompletionConfirmInfoRow("📞", "Phone:", rental.getCustomerPhone() != null ? rental.getCustomerPhone() : "N/A");
        HBox carBox = createCompletionConfirmInfoRow("🚗", "Car:", rental.getCarBrand() + " " + rental.getCarModel());
        HBox plateBox = createCompletionConfirmInfoRow("🔢", "Plate:", rental.getPlateNumber());
        HBox startDateBox = createCompletionConfirmInfoRow("📅", "Start Date:", rental.getStartDate().toString());
        HBox endDateBox = createCompletionConfirmInfoRow("📅", "End Date:", rental.getEndDate().toString());
        HBox durationBox = createCompletionConfirmInfoRow("⏱️", "Duration:", rental.getRentalDays() + " days");
        HBox amountBox = createCompletionConfirmInfoRow("💰", "Total Amount:", String.format("₺%.2f", rental.getTotalAmount()));

        detailsBox.getChildren().addAll(customerBox, phoneBox, carBox, plateBox, startDateBox, endDateBox, durationBox, amountBox);
        rentalInfoCard.getChildren().addAll(cardTitle, detailsBox);

        // Success message
        HBox successBox = new HBox(10);
        successBox.setAlignment(Pos.CENTER_LEFT);
        successBox.setStyle(
            "-fx-background-color: #E8F5E8; " +
            "-fx-border-color: #4CAF50; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 15;"
        );

        Label successIconSmall = new Label("✅");
        successIconSmall.setStyle("-fx-font-size: 16px;");

        VBox successTextBox = new VBox(3);
        Label successTitle = new Label("Completion Benefits");
        successTitle.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32; " +
            "-fx-font-size: 12px;"
        );

        Label successText = new Label("This rental will be marked as completed and the car will become available for new rentals.");
        successText.setStyle(
            "-fx-text-fill: #388E3C; " +
            "-fx-font-size: 11px;"
        );
        successText.setWrapText(true);

        successTextBox.getChildren().addAll(successTitle, successText);
        successBox.getChildren().addAll(successIconSmall, successTextBox);

        // Confirmation question
        Label confirmationQuestion = new Label("Are you sure you want to complete this rental?");
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
            successBox, 
            confirmationQuestion
        );

        dialogPane.setContent(mainContainer);

        // Add buttons with custom styling
        ButtonType completeButtonType = new ButtonType("✅ Complete Rental", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("❌ Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        dialog.getDialogPane().getButtonTypes().addAll(completeButtonType, cancelButtonType);

        // Style the buttons
        Button completeButton = (Button) dialog.getDialogPane().lookupButton(completeButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);

        completeButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45A049); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12 24; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.4), 8, 0, 0, 2);"
        );

        cancelButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #f44336, #d32f2f); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12 24; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(244,67,54,0.4), 8, 0, 0, 2);"
        );

        // Add hover effects
        completeButton.setOnMouseEntered(e -> completeButton.setStyle(
            completeButton.getStyle() + "-fx-background-color: linear-gradient(to bottom, #45A049, #3d8b40);"
        ));
        completeButton.setOnMouseExited(e -> completeButton.setStyle(
            completeButton.getStyle().replace("-fx-background-color: linear-gradient(to bottom, #45A049, #3d8b40);", "")
        ));

        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(
            cancelButton.getStyle() + "-fx-background-color: linear-gradient(to bottom, #d32f2f, #b71c1c);"
        ));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(
            cancelButton.getStyle().replace("-fx-background-color: linear-gradient(to bottom, #d32f2f, #b71c1c);", "")
        ));

        // Set dialog size
        dialog.getDialogPane().setPrefSize(600, 650);
        dialog.setResizable(false);

        // Show dialog and return result
        return dialog.showAndWait();
    }

    private HBox createCompletionConfirmInfoRow(String icon, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");
        iconLabel.setPrefWidth(25);

        Label labelText = new Label(label);
        labelText.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32; " +
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

    private void showRentalCompletionSuccessDialog(Rental rental) {
        // Ana dialog oluştur
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🎉 İşlem Başarılı");
        dialog.setHeaderText(null);

        // Dialog pane'i al ve modern stil uygula
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #F8FFF8, #E8F5E8); " +
            "-fx-border-color: #4CAF50; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 20; " +
            "-fx-background-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.4), 25, 0, 0, 8);"
        );

        // Ana container
        VBox mainContainer = new VBox(30);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setAlignment(Pos.CENTER);

        // Başarı header - büyük ikon ve başlık
        VBox headerBox = new VBox(20);
        headerBox.setAlignment(Pos.CENTER);
        
        Label successIcon = new Label("🎉");
        successIcon.setStyle(
            "-fx-font-size: 80px; " +
            "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.6), 15, 0, 0, 3);"
        );
        
        Label successTitle = new Label("KİRALAMA TAMAMLANDI!");
        successTitle.setStyle(
            "-fx-font-size: 28px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32; " +
            "-fx-effect: dropshadow(gaussian, rgba(46,125,50,0.3), 3, 0, 0, 1);"
        );
        
        Label successSubtitle = new Label("Araç başarıyla teslim alındı ve işlem tamamlandı");
        successSubtitle.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-text-fill: #388E3C; " +
            "-fx-font-style: italic;"
        );
        
        headerBox.getChildren().addAll(successIcon, successTitle, successSubtitle);

        // Kiralama bilgileri kartı - modern tasarım
        VBox rentalInfoCard = new VBox(20);
        rentalInfoCard.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #4CAF50; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 15; " +
            "-fx-background-radius: 15; " +
            "-fx-padding: 25; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );

        Label cardTitle = new Label("📋 Tamamlanan Kiralama Bilgileri");
        cardTitle.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32;"
        );

        // Kiralama detayları - grid layout
        VBox detailsBox = new VBox(12);
        
        HBox customerBox = createCompletionInfoRow("👤", "Müşteri:", rental.getCustomerName());
        HBox phoneBox = createCompletionInfoRow("📞", "Telefon:", rental.getCustomerPhone() != null ? rental.getCustomerPhone() : "N/A");
        HBox carBox = createCompletionInfoRow("🚗", "Araç:", rental.getCarBrand() + " " + rental.getCarModel());
        HBox plateBox = createCompletionInfoRow("🔢", "Plaka:", rental.getPlateNumber());
        HBox durationBox = createCompletionInfoRow("📅", "Süre:", rental.getRentalDays() + " gün");
        HBox amountBox = createCompletionInfoRow("💰", "Toplam Tutar:", String.format("₺%.2f", rental.getTotalAmount()));

        detailsBox.getChildren().addAll(customerBox, phoneBox, carBox, plateBox, durationBox, amountBox);
        rentalInfoCard.getChildren().addAll(cardTitle, detailsBox);

        // Başarı mesaj kutusu
        VBox successMessageBox = new VBox(10);
        successMessageBox.setAlignment(Pos.CENTER);
        successMessageBox.setStyle(
            "-fx-background-color: #E8F5E8; " +
            "-fx-border-color: #4CAF50; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 20;"
        );
        
        HBox checkIconBox = new HBox(10);
        checkIconBox.setAlignment(Pos.CENTER);
        
        Label checkIcon = new Label("✅");
        checkIcon.setStyle("-fx-font-size: 24px;");
        
        Label mainMessage = new Label("Kiralama başarıyla tamamlandı!");
        mainMessage.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32;"
        );
        
        checkIconBox.getChildren().addAll(checkIcon, mainMessage);
        
        Label subMessage = new Label("Araç müsait duruma getirildi ve sistem güncellendi.");
        subMessage.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #388E3C; " +
            "-fx-text-alignment: center;"
        );
        
        successMessageBox.getChildren().addAll(checkIconBox, subMessage);

        // Bilgi kutusu - modern stil
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle(
            "-fx-background-color: #FFF3E0; " +
            "-fx-border-color: #FF9800; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 15;"
        );

        Label infoIcon = new Label("📋");
        infoIcon.setStyle("-fx-font-size: 20px;");

        Label infoText = new Label("Araç artık yeni kiralamalar için müsait durumda");
        infoText.setStyle(
            "-fx-text-fill: #E65100; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold;"
        );

        infoBox.getChildren().addAll(infoIcon, infoText);

        // Ana container'a ekle
        mainContainer.getChildren().addAll(
            headerBox,
            rentalInfoCard,
            successMessageBox,
            infoBox
        );

        dialogPane.setContent(mainContainer);

        // Tamam butonu - modern stil
        ButtonType okButton = new ButtonType("✅ Tamam", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(okButton);

        // Buton stilini özelleştir
        dialogPane.lookupButton(okButton).setStyle(
            "-fx-background-color: linear-gradient(to right, #4CAF50, #66BB6A); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-padding: 15 35; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.5), 12, 0, 0, 4);"
        );

        // Dialog boyutunu ayarla
        dialog.setResizable(false);
        dialogPane.setPrefWidth(550);
        dialogPane.setPrefHeight(650);

        // Dialogu göster
        dialog.showAndWait();
    }

    private HBox createCompletionInfoRow(String icon, String label, String value) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
            "-fx-background-color: #F8FFF8; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 8 12;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px;");
        iconLabel.setPrefWidth(35);

        Label labelText = new Label(label);
        labelText.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32; " +
            "-fx-font-size: 14px;"
        );
        labelText.setPrefWidth(120);

        Label valueText = new Label(value);
        valueText.setStyle(
            "-fx-text-fill: #1B5E20; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold;"
        );
        valueText.setWrapText(true);

        row.getChildren().addAll(iconLabel, labelText, valueText);
        return row;
    }
} 