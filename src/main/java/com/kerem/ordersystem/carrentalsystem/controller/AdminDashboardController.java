package com.kerem.ordersystem.carrentalsystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import com.kerem.ordersystem.carrentalsystem.database.ActivityDAO;
import com.kerem.ordersystem.carrentalsystem.model.Activity;
import com.kerem.ordersystem.carrentalsystem.util.StageUtils;
import com.kerem.ordersystem.carrentalsystem.util.UserCustomerLinkFixer;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label statusLabel;
    @FXML private ScrollPane activitiesScrollPane;
    @FXML private VBox activitiesContainer;

    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize Activities table
        ActivityDAO.createTableIfNotExists();
        
        loadRecentActivities();
        statusLabel.setText("✅ Dashboard yüklendi");
    }

    @FXML
    private void handleManageUsers() {
        statusLabel.setText("🚧 Manage Users - Under Development");
    }

    @FXML
    private void handleViewAllRentals() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/all-rentals.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "All Rentals");
        } catch (IOException e) {
            statusLabel.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenRevenueSummary() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/revenue-summary.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Revenue Summary");
        } catch (IOException e) {
            statusLabel.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    private void handleManageCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/category-management.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Category Management");
        } catch (IOException e) {
            statusLabel.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenUpdateCarStatus() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/update-car-status.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Update Car Status");
        } catch (IOException e) {
            statusLabel.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    private void handleViewLogs() {
        statusLabel.setText("📜 Viewing logs not implemented yet.");
    }

    @FXML
    private void handleGoToCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/customer-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Customer List");
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshData() {
        statusLabel.setText("🔄 Veriler yenileniyor...");
        loadRecentActivities();
        statusLabel.setText("✅ Veriler yenilendi");
    }

    @FXML
    private void handleRefreshActivities() {
        loadRecentActivities();
        statusLabel.setText("✅ Activities refreshed");
    }

    private void loadRecentActivities() {
        try {
            // Ensure table exists before querying
            ActivityDAO.createTableIfNotExists();
            
            List<Activity> activities = ActivityDAO.getRecentActivities(5);
            
            activitiesContainer.getChildren().clear();
            
            if (activities.isEmpty()) {
                Label noActivitiesLabel = new Label("📝 No activities yet");
                noActivitiesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
                activitiesContainer.getChildren().add(noActivitiesLabel);
            } else {
                for (Activity activity : activities) {
                    HBox activityBox = createActivityBox(activity);
                    activitiesContainer.getChildren().add(activityBox);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Dashboard loadRecentActivities error: " + e.getMessage());
            e.printStackTrace();
            
            Label errorLabel = new Label("❌ Error loading activities: " + e.getMessage());
            errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f44336; -fx-padding: 10;");
            activitiesContainer.getChildren().clear();
            activitiesContainer.getChildren().add(errorLabel);
        }
    }

    private HBox createActivityBox(Activity activity) {
        HBox activityBox = new HBox();
        activityBox.setSpacing(15);
        activityBox.setAlignment(Pos.CENTER_LEFT);
        activityBox.setStyle("-fx-padding: 10; -fx-border-radius: 6; -fx-background-color: #f8f9fa; -fx-background-radius: 6;");
        
        // Icon
        Label iconLabel = new Label(activity.getIcon());
        iconLabel.setStyle("-fx-font-size: 16px;");
        
        // Content
        VBox contentBox = new VBox();
        contentBox.setSpacing(2);
        
        Label titleLabel = new Label(activity.getDescription());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label detailsLabel = new Label(activity.getDetails() + " - " + activity.getTimeAgo());
        detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        detailsLabel.setWrapText(true);
        
        contentBox.getChildren().addAll(titleLabel, detailsLabel);
        
        activityBox.getChildren().addAll(iconLabel, contentBox);
        
        return activityBox;
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Car Rental System - Login");
            statusLabel.setText("✅ Başarıyla çıkış yapıldı");
        } catch (IOException e) {
            statusLabel.setText("❌ Çıkış hatası: " + e.getMessage());
        }
    }

    @FXML
    private void handleCustomerService() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/customer-service.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Müşteri Hizmetleri - Araç Kiralama");
        } catch (IOException e) {
            statusLabel.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add-car.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Yeni Araç Ekleme");
        } catch (IOException e) {
            statusLabel.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    private void handleViewCars() {
        try {
            System.out.println("🚗 Araçlar butonu tıklandı, view-cars.fxml yükleniyor...");
            statusLabel.setText("🔄 Araçlar sayfası yükleniyor...");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view-cars.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Araçlar");
            
            System.out.println("✅ view-cars.fxml başarıyla yüklendi!");
        } catch (IOException e) {
            System.err.println("❌ Araçlar sayfası yükleme hatası: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("❌ Araçlar sayfası yüklenemedi: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewActiveRentals() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/active-rentals.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Aktif Kiralamalar");
        } catch (IOException e) {
            statusLabel.setText("❌ Sayfa yüklenemedi: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/delete-cars.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Araç Silme");
        } catch (IOException e) {
            statusLabel.setText("❌ Sayfa yüklenemedi: " + e.getMessage());
        }
    }

    @FXML
    private void handleRentalHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/rental-history.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Rental History - Completed & Cancelled");
        } catch (IOException e) {
            statusLabel.setText("❌ Rental History page could not be loaded: " + e.getMessage());
        }
    }

    @FXML
    private void handleSecurityDashboard() {
        try {
            System.out.println("🔧 Security Dashboard button clicked - starting navigation...");
            statusLabel.setText("🔄 Loading Security Dashboard...");
            
            System.out.println("🔧 Loading FXML resource...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/security-dashboard.fxml"));
            
            System.out.println("🔧 FXML resource loaded, parsing...");
            Parent root = loader.load();
            
            System.out.println("🔧 FXML parsed successfully, getting stage...");
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            
            System.out.println("🔧 Stage obtained, initializing dimensions...");
            StageUtils.initializeDimensions(stage);
            
            System.out.println("🔧 Setting fullscreen scene...");
            StageUtils.setFullscreenScene(stage, root, "Security Dashboard");
            
            System.out.println("✅ Security Dashboard loaded successfully!");
        } catch (Exception e) {
            System.err.println("❌ Security Dashboard loading error: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("❌ Security Dashboard could not be loaded: " + e.getMessage());
        }
    }





    @FXML
    private void handleReportsDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reports-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Reports Dashboard");
        } catch (IOException e) {
            statusLabel.setText("❌ Reports Dashboard could not be loaded: " + e.getMessage());
        }
    }


}