package com.kerem.ordersystem.carrentalsystem.model;

import java.time.LocalDateTime;

public class Activity {
    private int activityId;
    private String activityType;
    private String description;
    private String icon;
    private LocalDateTime createdAt;
    private String details;

    public Activity() {}

    public Activity(String activityType, String description, String icon, String details) {
        this.activityType = activityType;
        this.description = description;
        this.icon = icon;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    // Helper method to get time ago string
    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();
        
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        
        long hours = minutes / 60;
        if (hours < 24) return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        
        long days = hours / 24;
        if (days < 7) return days + " day" + (days == 1 ? "" : "s") + " ago";
        
        long weeks = days / 7;
        return weeks + " week" + (weeks == 1 ? "" : "s") + " ago";
    }
} 