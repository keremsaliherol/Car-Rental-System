package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.util.StageUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AuditTrailController implements Initializable {

    @FXML private Label totalEventsLabel;
    @FXML private Label securityEventsLabel;
    @FXML private Label todayEventsLabel;
    @FXML private Label totalRecordsLabel;
    @FXML private Label statusLabel;

    @FXML private ComboBox<String> eventTypeComboBox;
    @FXML private ComboBox<String> severityComboBox;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;

    @FXML private TableView<AuditEvent> auditTable;
    @FXML private TableColumn<AuditEvent, String> timestampColumn;
    @FXML private TableColumn<AuditEvent, String> eventTypeColumn;
    @FXML private TableColumn<AuditEvent, String> userColumn;
    @FXML private TableColumn<AuditEvent, String> actionColumn;
    @FXML private TableColumn<AuditEvent, String> resourceColumn;
    @FXML private TableColumn<AuditEvent, String> ipAddressColumn;
    @FXML private TableColumn<AuditEvent, String> severityColumn;
    @FXML private TableColumn<AuditEvent, String> detailsColumn;

    private List<AuditEvent> allEvents;
    private List<AuditEvent> filteredEvents;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("🔧 AuditTrailController initialize() started...");
            statusLabel.setText("🔄 Initializing audit trail...");
            
            setupTable();
            setupFilters();
            loadAuditEvents();
            updateStatistics();
            
            System.out.println("✅ AuditTrailController initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error in AuditTrailController initialize: " + e.getMessage());
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("❌ Initialization error: " + e.getMessage());
            }
        }
    }

    private void setupTable() {
        timestampColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimestamp()));
        eventTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEventType()));
        userColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser()));
        actionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAction()));
        resourceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResource()));
        ipAddressColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIpAddress()));
        severityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSeverity()));
        detailsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDetails()));
    }

    private void setupFilters() {
        // Event Type filter
        ObservableList<String> eventTypes = FXCollections.observableArrayList();
        eventTypes.addAll("All Events", "Login", "Logout", "Data Access", "Configuration Change", "Security Alert");
        eventTypeComboBox.setItems(eventTypes);
        eventTypeComboBox.setValue("All Events");

        // Severity filter
        ObservableList<String> severities = FXCollections.observableArrayList();
        severities.addAll("All Severity", "Low", "Medium", "High", "Critical");
        severityComboBox.setItems(severities);
        severityComboBox.setValue("All Severity");
    }

    private void loadAuditEvents() {
        try {
            System.out.println("🔧 Loading audit events...");
            statusLabel.setText("🔄 Loading audit events...");
            
            allEvents = new ArrayList<>();
            
            // Create sample audit events
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            
            allEvents.add(new AuditEvent(
                now.minusMinutes(5).format(formatter),
                "Login",
                "admin",
                "User login successful",
                "Authentication System",
                "192.168.1.100",
                "Low",
                "Administrator logged in successfully"
            ));
            
            allEvents.add(new AuditEvent(
                now.minusMinutes(10).format(formatter),
                "Data Access",
                "admin",
                "Viewed user management",
                "User Management",
                "192.168.1.100",
                "Medium",
                "Administrator accessed user management interface"
            ));
            
            allEvents.add(new AuditEvent(
                now.minusMinutes(15).format(formatter),
                "Security Alert",
                "system",
                "Failed login attempt",
                "Authentication System",
                "192.168.1.200",
                "High",
                "Multiple failed login attempts detected"
            ));
            
            allEvents.add(new AuditEvent(
                now.minusMinutes(20).format(formatter),
                "Configuration Change",
                "admin",
                "Updated security settings",
                "Security Dashboard",
                "192.168.1.100",
                "Medium",
                "Security configuration modified"
            ));
            
            allEvents.add(new AuditEvent(
                now.minusMinutes(30).format(formatter),
                "Data Access",
                "admin",
                "Viewed rental history",
                "Rental System",
                "192.168.1.100",
                "Low",
                "Administrator accessed rental history"
            ));
            
            filteredEvents = new ArrayList<>(allEvents);
            displayEvents();
            statusLabel.setText("✅ Audit events loaded successfully - " + allEvents.size() + " events");
            
        } catch (Exception e) {
            statusLabel.setText("❌ Error loading audit events: " + e.getMessage());
            System.err.println("❌ Error loading audit events: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayEvents() {
        ObservableList<AuditEvent> tableData = FXCollections.observableArrayList(filteredEvents);
        auditTable.setItems(tableData);
        totalRecordsLabel.setText("Total: " + filteredEvents.size() + " events");
    }

    private void updateStatistics() {
        try {
            if (allEvents == null) {
                totalEventsLabel.setText("0");
                securityEventsLabel.setText("0");
                todayEventsLabel.setText("0");
                return;
            }
            
            int totalEvents = allEvents.size();
            int securityEvents = (int) allEvents.stream()
                .filter(e -> "Security Alert".equals(e.getEventType()) || "High".equals(e.getSeverity()) || "Critical".equals(e.getSeverity()))
                .count();
            int todayEvents = allEvents.size(); // All sample events are from today
            
            totalEventsLabel.setText(String.valueOf(totalEvents));
            securityEventsLabel.setText(String.valueOf(securityEvents));
            todayEventsLabel.setText(String.valueOf(todayEvents));
            
        } catch (Exception e) {
            System.err.println("❌ Error updating statistics: " + e.getMessage());
            totalEventsLabel.setText("0");
            securityEventsLabel.setText("0");
            todayEventsLabel.setText("0");
        }
    }

    @FXML
    private void handleBackToSecurity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/security-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            StageUtils.initializeDimensions(stage);
            StageUtils.setFullscreenScene(stage, root, "Security Dashboard");
        } catch (IOException e) {
            statusLabel.setText("❌ Security Dashboard could not be loaded: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadAuditEvents();
        updateStatistics();
        statusLabel.setText("🔄 Audit trail refreshed");
    }

    @FXML
    private void handleApplyFilters() {
        try {
            statusLabel.setText("🔄 Applying filters...");
            
            filteredEvents = allEvents.stream()
                .filter(event -> {
                    // Event type filter
                    String eventTypeFilter = eventTypeComboBox.getValue();
                    if (!"All Events".equals(eventTypeFilter) && !eventTypeFilter.equals(event.getEventType())) {
                        return false;
                    }
                    
                    // Severity filter
                    String severityFilter = severityComboBox.getValue();
                    if (!"All Severity".equals(severityFilter) && !severityFilter.equals(event.getSeverity())) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
            
            displayEvents();
            statusLabel.setText("✅ Filters applied - " + filteredEvents.size() + " events found");
            
        } catch (Exception e) {
            statusLabel.setText("❌ Error applying filters: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearFilters() {
        eventTypeComboBox.setValue("All Events");
        severityComboBox.setValue("All Severity");
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        
        filteredEvents = new ArrayList<>(allEvents);
        displayEvents();
        statusLabel.setText("🔄 Filters cleared - Showing all events");
    }

    // AuditEvent class for table data
    public static class AuditEvent {
        private final String timestamp;
        private final String eventType;
        private final String user;
        private final String action;
        private final String resource;
        private final String ipAddress;
        private final String severity;
        private final String details;

        public AuditEvent(String timestamp, String eventType, String user, String action, 
                         String resource, String ipAddress, String severity, String details) {
            this.timestamp = timestamp;
            this.eventType = eventType;
            this.user = user;
            this.action = action;
            this.resource = resource;
            this.ipAddress = ipAddress;
            this.severity = severity;
            this.details = details;
        }

        public String getTimestamp() { return timestamp; }
        public String getEventType() { return eventType; }
        public String getUser() { return user; }
        public String getAction() { return action; }
        public String getResource() { return resource; }
        public String getIpAddress() { return ipAddress; }
        public String getSeverity() { return severity; }
        public String getDetails() { return details; }
    }
} 