package com.kerem.ordersystem.carrentalsystem.model;

/**
 * Model class for Car Category
 */
public class CarCategory {
    private int categoryId;
    private String categoryName;
    private String description;

    // Default constructor
    public CarCategory() {
    }

    // Constructor with ID and name
    public CarCategory(int categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // Constructor with all fields
    public CarCategory(int categoryId, String categoryName, String description) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
    }

    // Getters
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getDescription() { return description; }

    // Setters
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return categoryName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CarCategory that = (CarCategory) obj;
        return categoryId == that.categoryId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(categoryId);
    }
} 