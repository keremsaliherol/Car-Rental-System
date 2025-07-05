package com.kerem.ordersystem.carrentalsystem.model;

import java.sql.Date;
import java.time.LocalDate;

public class Customer {
    private int customerId;
    private int userId;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String driverLicenseNo;
    private LocalDate dateOfBirth;

    // Default constructor
    public Customer() {
    }

    public Customer(int customerId, String fullName, String phone, String address) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
    }

    public Customer(int customerId, String fullName, String phone, String address, String driverLicenseNo, Date dateOfBirth) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.driverLicenseNo = driverLicenseNo;
        this.dateOfBirth = dateOfBirth != null ? dateOfBirth.toLocalDate() : null;
    }

    // Getters
    public int getCustomerId() { return customerId; }
    public int getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getDriverLicenseNo() { return driverLicenseNo; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    // Setters
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setDriverLicenseNo(String driverLicenseNo) { this.driverLicenseNo = driverLicenseNo; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}