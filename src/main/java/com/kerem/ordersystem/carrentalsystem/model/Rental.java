package com.kerem.ordersystem.carrentalsystem.model;

import java.time.LocalDate;

public class Rental {
    private int rentalId;
    private int customerId;
    private int carId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalAmount;
    private String status;
    private LocalDate createdAt;
    
    // Ek bilgiler JOIN'den gelecek
    private String customerName;
    private String customerPhone;
    private String carBrand;
    private String carModel;
    private String plateNumber;
    private double dailyRate;

    public Rental() {}

    public Rental(int rentalId, int customerId, int carId, LocalDate startDate, 
                  LocalDate endDate, double totalAmount, String status, LocalDate createdAt) {
        this.rentalId = rentalId;
        this.customerId = customerId;
        this.carId = carId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getRentalId() { return rentalId; }
    public void setRentalId(int rentalId) { this.rentalId = rentalId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getCarId() { return carId; }
    public void setCarId(int carId) { this.carId = carId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    // JOIN bilgileri için getters/setters
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCarBrand() { return carBrand; }
    public void setCarBrand(String carBrand) { this.carBrand = carBrand; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }

    // Kiralama süresi hesaplama
    public int getRentalDays() {
        if (startDate != null && endDate != null) {
            return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format("Rental{id=%d, customer='%s', car='%s %s', plates='%s', days=%d, total=%.2f, status='%s'}", 
            rentalId, customerName, carBrand, carModel, plateNumber, getRentalDays(), totalAmount, status);
    }
} 