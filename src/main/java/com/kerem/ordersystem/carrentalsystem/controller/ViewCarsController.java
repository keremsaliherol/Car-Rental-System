package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.CarDAO;
import com.kerem.ordersystem.carrentalsystem.database.CarCategoryDAO;
import com.kerem.ordersystem.carrentalsystem.model.Car;
import com.kerem.ordersystem.carrentalsystem.model.CarCategory;
import com.kerem.ordersystem.carrentalsystem.controller.CustomerServiceController;
import com.kerem.ordersystem.carrentalsystem.util.StageUtils;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ViewCarsController implements Initializable {

    @FXML private ScrollPane carsScrollPane;
    @FXML private Label statusLabel;
    @FXML private Label totalCarsLabel;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> fuelFilterComboBox;

    private List<Car> carsList;
    private List<Car> filteredCarsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🚗 ViewCarsController initialize() started...");
        try {
        loadCars();
        setupFilters();
        displayCars();
            System.out.println("✅ ViewCarsController initialize() completed!");
        } catch (Exception e) {
            System.err.println("❌ ViewCarsController initialize() error: " + e.getMessage());
            e.printStackTrace();
            
            // Hata durumunda güvenli varsayılan değerler ayarla
            carsList = new ArrayList<>();
            filteredCarsList = new ArrayList<>();
            if (statusLabel != null) {
                statusLabel.setText("❌ Cars could not be loaded: " + e.getMessage());
            }
            if (totalCarsLabel != null) {
                totalCarsLabel.setText("Total 0 cars");
            }
        }
    }

    private void setupFilters() {
        // Kategori filtresi
        ObservableList<String> categories = FXCollections.observableArrayList();
        categories.add("All Categories");
        categories.addAll(
                "Economic", "Comfort", "Luxury", "SUV", "Sports", "Van", "Compact", "Sedan", "Hatchback", "Coupe"
        );
        categoryFilterComboBox.setItems(categories);
        categories.add("All Categories");
        List<CarCategory> categoryList = CarCategoryDAO.getAllCategoriesAsObjects();
        for (CarCategory category : categoryList) {
            categories.add(category.getCategoryName());
        }
        categoryFilterComboBox.setItems(categories);
        categoryFilterComboBox.setValue("All Categories");

        // Durum filtresi
        ObservableList<String> statuses = FXCollections.observableArrayList();
        statuses.add("All Statuses");
        statuses.add("AVAILABLE");
        statuses.add("RENTED");
        statuses.add("MAINTENANCE");
        statusFilterComboBox.setItems(statuses);
        statusFilterComboBox.setValue("All Statuses");

        // Yakıt tipi filtresi
        ObservableList<String> fuelTypes = FXCollections.observableArrayList();
        fuelTypes.add("All Fuel Types");
        fuelTypes.add("Gasoline");
        fuelTypes.add("Diesel");
        fuelTypes.add("Electric");
        fuelFilterComboBox.setItems(fuelTypes);
        fuelFilterComboBox.setValue("All Fuel Types");
    }

    private void loadCars() {
        System.out.println("🔍 Calling CarDAO.getAllCars()...");
        carsList = CarDAO.getAllCars();
        filteredCarsList = carsList;
        
        System.out.println("📊 Number of cars loaded: " + carsList.size());
        for (Car car : carsList) {
            System.out.println("  - " + car.getBrand() + " " + car.getModel() + 
                             " (Status: " + car.getStatus() + ", Category: " + car.getCategoryName() + ")");
        }
        
        updateStatusLabels();
    }

    private void updateStatusLabels() {
        statusLabel.setText("✅ " + filteredCarsList.size() + " cars showing");
        totalCarsLabel.setText("Total " + filteredCarsList.size() + " cars");
    }

    @FXML
    private void handleApplyFilters() {
        String selectedCategory = categoryFilterComboBox.getValue();
        String selectedStatus = statusFilterComboBox.getValue();
        String selectedFuel = fuelFilterComboBox.getValue();

        System.out.println("=== APPLYING FILTERS ===");
        System.out.println("Selected Category: " + selectedCategory);
        System.out.println("Selected Status: " + selectedStatus);
        System.out.println("Selected Fuel: " + selectedFuel);
        System.out.println("Total cars: " + carsList.size());

        filteredCarsList = carsList.stream()
            .filter(car -> {
                System.out.println("\n--- Checking car: " + car.getBrand() + " " + car.getModel() + " ---");
                
                // Kategori filtresi
                if (!"All Categories".equals(selectedCategory)) {
                    System.out.println("Car category: '" + car.getCategoryName() + "' vs Selected: '" + selectedCategory + "'");
                    if (!selectedCategory.equals(car.getCategoryName())) {
                        System.out.println("Category filter failed!");
                        return false;
                    }
                }

                // Durum filtresi - Veritabanındaki değerlerle karşılaştır
                if (!"All Statuses".equals(selectedStatus)) {
                    String dbStatus = car.getStatus(); // Available, Rented, Maintenance
                    System.out.println("Car status (DB): '" + dbStatus + "' vs Selected: '" + selectedStatus + "'");
                    
                    // Null kontrolü ekle
                    if (dbStatus == null) {
                        System.out.println("Car status is null - filter failed!");
                        return false;
                    }
                    
                    boolean statusMatch = false;
                    switch (selectedStatus) {
                        case "AVAILABLE":
                            statusMatch = "Available".equals(dbStatus);
                            break;
                        case "RENTED":
                            statusMatch = "Rented".equals(dbStatus);
                            break;
                        case "MAINTENANCE":
                            statusMatch = "Maintenance".equals(dbStatus);
                            break;
                    }
                    
                    if (!statusMatch) {
                        System.out.println("Status filter failed!");
                        return false;
                    }
                }

                // Yakıt tipi filtresi
                if (!"All Fuel Types".equals(selectedFuel)) {
                    String carFuelType = extractFuelType(car.getDescription());
                    System.out.println("Car fuel type: '" + carFuelType + "' vs Selected: '" + selectedFuel + "'");
                    if (!selectedFuel.equals(carFuelType)) {
                        System.out.println("Fuel filter failed!");
                        return false;
                    }
                }

                System.out.println("✅ Car passed all filters!");
                return true;
            })
            .collect(Collectors.toList());

        System.out.println("Filter result: " + filteredCarsList.size() + " cars found");
        System.out.println("=== FILTERING COMPLETED ===\n");

        updateStatusLabels();
        displayCars();
        
        statusLabel.setText("🔍 Filter applied - " + filteredCarsList.size() + " cars found");
    }

    @FXML
    private void handleClearFilters() {
        categoryFilterComboBox.setValue("All Categories");
        statusFilterComboBox.setValue("All Statuses");
        fuelFilterComboBox.setValue("All Fuel Types");
        
        filteredCarsList = carsList;
        updateStatusLabels();
        displayCars();
        
        statusLabel.setText("🔄 Filters cleared - Showing all cars");
    }

    @FXML
    private void handleRefreshCars() {
        statusLabel.setText("🔄 Refreshing cars...");
        refreshCars();
        statusLabel.setText("✅ Cars refreshed successfully - " + filteredCarsList.size() + " cars loaded");
    }

    private void displayCars() {
        // Grid layout oluştur
        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setPadding(new Insets(20));
        gridPane.setAlignment(Pos.TOP_CENTER);

        int column = 0;
        int row = 0;
        int carsPerRow = 4; // Her satırda 4 araç

        for (Car car : filteredCarsList) {
            VBox carCard = createCarCard(car);
            gridPane.add(carCard, column, row);

            column++;
            if (column == carsPerRow) {
                column = 0;
                row++;
            }
        }

        carsScrollPane.setContent(gridPane);
    }

    private VBox createCarCard(Car car) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPrefWidth(280);
        card.setPrefHeight(320);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E0E0E0; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
            "-fx-padding: 15;"
        );

        // Ekonomik etiketi (sadece elektrikli araçlar için)
        Label economicLabel = null;
        String carFuelType = extractFuelType(car.getDescription());
        if ("Electric".equals(carFuelType)) {
            economicLabel = new Label("Economic");
            economicLabel.setStyle(
                "-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 12px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 5 10; " +
                "-fx-background-radius: 15;"
            );
            economicLabel.setAlignment(Pos.TOP_LEFT);
        }

        // Araç resmi
        ImageView carImage = new ImageView();
        carImage.setFitWidth(250);
        carImage.setFitHeight(140);
        carImage.setPreserveRatio(true);
        carImage.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");
        
        // Araç resmi yükleme
        try {
            Image carImg = null;
            String imagePath = car.getImagePath();
            
            System.out.println("🖼️ Loading image: " + car.getBrand() + " " + car.getModel() + " -> " + imagePath);
            
            if (imagePath != null && !imagePath.isEmpty()) {
                // Araç özel resmi varsa onu kullan
                try {
                    carImg = new Image(getClass().getResourceAsStream(imagePath));
                    if (carImg.isError()) {
                        System.err.println("❌ Image error: " + imagePath);
                        throw new Exception("Image is in error state");
                    }
                    System.out.println("✅ Image loaded successfully: " + imagePath);
                } catch (Exception e1) {
                    System.err.println("❌ Car image could not be loaded: " + imagePath + " - " + e1.getMessage());
                    // Fallback olarak varsayılan resmi dene
                    carImg = new Image(getClass().getResourceAsStream("/images/indir.jpg"));
                }
            } else {
                // ImagePath yoksa varsayılan resmi kullan
                System.out.println("⚠️ ImagePath null/empty, using default image");
                carImg = new Image(getClass().getResourceAsStream("/images/indir.jpg"));
            }
            
            if (carImg != null && !carImg.isError()) {
            carImage.setImage(carImg);
                System.out.println("✅ Image set to ImageView");
            } else {
                throw new Exception("Image could not be loaded or is invalid");
            }
        } catch (Exception e) {
            // Resim yüklenemezse gri placeholder kullan
            System.err.println("❌ Final image loading error: " + car.getBrand() + " " + car.getModel() + " - " + e.getMessage());
            carImage.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");
            
            // Boş bir Label ekleyerek placeholder görünümü sağla
            Label placeholder = new Label("🚗");
            placeholder.setStyle("-fx-font-size: 48px; -fx-text-fill: #ccc;");
            placeholder.setPrefSize(250, 140);
            placeholder.setAlignment(Pos.CENTER);
        }

        // Araç başlığı
        Label carTitle = new Label(car.getBrand() + " " + car.getModel());
        carTitle.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #333;"
        );

        // Yıl
        Label yearLabel = new Label(String.valueOf(car.getYear()));
        yearLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #666;"
        );

        // Özellikler (yakıt, vites, plaka)
        HBox featuresBox = new HBox();
        featuresBox.setSpacing(15);
        featuresBox.setAlignment(Pos.CENTER_LEFT);

        // Yakıt tipi (description'dan çıkar)
        String fuelType = extractFuelType(car.getDescription());
        Label fuelLabel = createFeatureLabel("⛽", fuelType);
        
        // Kategori
        Label categoryLabel = createFeatureLabel("📋", car.getCategoryName());

        featuresBox.getChildren().addAll(fuelLabel, categoryLabel);

        // Günlük ücret
        HBox priceBox = new HBox();
        priceBox.setSpacing(5);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Label priceTitle = new Label("DAILY");
        priceTitle.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-text-fill: #666; " +
            "-fx-font-weight: bold;"
        );

        Label priceValue = new Label(String.format("%.0f TL", car.getDailyRate()));
        priceValue.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #333;"
        );

        priceBox.getChildren().addAll(priceTitle, priceValue);

        // Durum etiketi
        Label statusLabel = new Label(getStatusText(car.getStatus()));
        if ("Available".equals(car.getStatus())) {
            statusLabel.setStyle(
                "-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 5 10; " +
                "-fx-background-radius: 12;"
            );
        } else {
            statusLabel.setStyle(
                "-fx-background-color: #F44336; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 5 10; " +
                "-fx-background-radius: 12;"
            );
        }

        // Kartı birleştir
        VBox topBox = new VBox(5);
        if (economicLabel != null) {
            topBox.getChildren().addAll(economicLabel, carImage);
        } else {
            topBox.getChildren().add(carImage);
        }
        topBox.setAlignment(Pos.TOP_LEFT);

        VBox infoBox = new VBox(8);
        infoBox.getChildren().addAll(carTitle, yearLabel, featuresBox, priceBox, statusLabel);

        card.getChildren().addAll(topBox, infoBox);
        card.setAlignment(Pos.TOP_CENTER);

        // Kartın tamamına tıklama özelliği ekle
        card.setOnMouseClicked(e -> handleRentCar(car));
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-border-color: #42A5F5; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(66,165,245,0.3), 15, 0, 0, 3); " +
                "-fx-padding: 15; " +
                "-fx-cursor: hand;"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-border-color: #E0E0E0; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                "-fx-padding: 15;"
            );
        });

        return card;
    }

    private Label createFeatureLabel(String icon, String text) {
        Label label = new Label(icon + " " + text);
        label.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-text-fill: #666; " +
            "-fx-background-color: #F5F5F5; " +
            "-fx-padding: 3 8; " +
            "-fx-background-radius: 10;"
        );
        return label;
    }

    private String extractFuelType(String description) {
        if (description != null) {
            // Check for English format first
            if (description.contains("Fuel: ")) {
                String fuelType = description.substring(description.indexOf("Fuel: ") + 6).trim();
                return translateFuelType(fuelType);
            }
            // Check for Turkish format (legacy data)
            if (description.contains("Yakıt: ")) {
                String fuelType = description.substring(description.indexOf("Yakıt: ") + 7).trim();
                return translateFuelType(fuelType);
            }
        }
        return "Gasoline"; // default
    }
    
    private String translateFuelType(String turkishFuelType) {
        if (turkishFuelType == null) return "Gasoline";
        
        switch (turkishFuelType.toLowerCase()) {
            case "benzin": return "Gasoline";
            case "dizel": return "Diesel";
            case "elektrik": return "Electric";
            case "hybrid": return "Hybrid";
            case "lpg": return "LPG";
            case "benzin+lpg": return "Gasoline+LPG";
            // If already in English, return as is
            case "gasoline": return "Gasoline";
            case "diesel": return "Diesel";
            case "electric": return "Electric";
            default: return turkishFuelType; // Return original if not found
        }
    }

    private String getStatusText(String status) {
        if (status == null) {
            return "UNKNOWN";
        }
        switch (status) {
            case "Available": return "AVAILABLE";
            case "Rented": return "RENTED";
            case "Maintenance": return "MAINTENANCE";
            default: return status.toUpperCase();
        }
    }

    private void handleRentCar(Car car) {
        try {
            System.out.println("🚗 handleRentCar called for: " + car.getBrand() + " " + car.getModel());
            
            if (!"Available".equals(car.getStatus())) {
                System.out.println("❌ Car not available, status: " + car.getStatus());
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Car Not Available");
                alert.setHeaderText("This car is currently not available for rental");
                alert.setContentText("Car status: " + getStatusText(car.getStatus()));
                alert.showAndWait();
                return;
            }

            System.out.println("🔧 Loading customer-service.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/customer-service.fxml"));
            
            System.out.println("🔧 Parsing FXML...");
            Parent root = loader.load();
            
            System.out.println("🔧 Getting controller...");
            CustomerServiceController controller = loader.getController();
            
            System.out.println("🔧 Setting pre-selected car...");
            controller.setPreSelectedCar(car);
            
            System.out.println("🔧 Getting stage and setting scene...");
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Customer Service - Car Rental");
            
            System.out.println("✅ Successfully navigated to customer service");
        } catch (Exception e) {
            System.err.println("❌ Error in handleRentCar: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("❌ Page could not be loaded: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Admin Dashboard");
        } catch (IOException e) {
            statusLabel.setText("❌ Dashboard could not be loaded: " + e.getMessage());
        }
    }

    /**
     * Refresh the cars list from database and update the display
     * This method can be called from other controllers to update the view
     */
    public void refreshCars() {
        System.out.println("🔄 Refreshing cars list...");
        loadCars();
        
        // Mevcut filtreleri yeniden uygula
        handleApplyFilters();
        
        System.out.println("✅ Cars list refreshed successfully");
    }

    /**
     * Static method to refresh cars view if it's currently displayed
     */
    public static void refreshCarsViewIfActive() {
        // Bu metod diğer controller'lardan çağrılabilir
        System.out.println("📢 Request to refresh cars view received");
        // Not: Bu implementasyon geliştirilecek - şimdilik log mesajı
    }
}