package com.kerem.ordersystem.carrentalsystem.model;

public class Car {
    private int carId;
    private String plateNumber;
    private String brand;
    private String model;
    private int year;
    private double dailyRate;
    private int categoryId;
    private String status;
    private int mileage;
    private String description;
    private String categoryName; // ekstra olarak JOIN sonucu
    private String imagePath; // resim yolu

    // Default constructor
    public Car() {
    }

    public Car(int carId, String plateNumber, String brand, String model, int year, double dailyRate,
               int categoryId, String status, int mileage, String description, String categoryName) {
        this.carId = carId;
        this.plateNumber = plateNumber;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.dailyRate = dailyRate;
        this.categoryId = categoryId;
        this.status = status;
        this.mileage = mileage;
        this.description = description;
        this.categoryName = categoryName;
    }

    public Car(int carId, String plateNumber, String brand, String model, int year, double dailyRate,
               int categoryId, String status, int mileage, String description, String categoryName, String imagePath) {
        this.carId = carId;
        this.plateNumber = plateNumber;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.dailyRate = dailyRate;
        this.categoryId = categoryId;
        this.status = status;
        this.mileage = mileage;
        this.description = description;
        this.categoryName = categoryName;
        this.imagePath = imagePath;
    }

    public int getCarId() { return carId; }
    public String getPlateNumber() { return plateNumber; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public double getDailyRate() { return dailyRate; }
    public int getCategoryId() { return categoryId; }
    public String getStatus() { return status; }
    public int getMileage() { return mileage; }
    public String getDescription() { return description; }
    public String getCategoryName() { return categoryName; }
    public String getImagePath() { return imagePath; }
    
    // Setter methods
    public void setCarId(int carId) { this.carId = carId; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setYear(int year) { this.year = year; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setStatus(String status) { this.status = status; }
    public void setMileage(int mileage) { this.mileage = mileage; }
    public void setDescription(String description) { this.description = description; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
