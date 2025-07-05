package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import com.kerem.ordersystem.carrentalsystem.util.StageUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label adminUsersLabel;
    @FXML private Label totalRecordsLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<UserRow> usersTable;
    @FXML private TableColumn<UserRow, String> userIdColumn;
    @FXML private TableColumn<UserRow, String> usernameColumn;
    @FXML private TableColumn<UserRow, String> emailColumn;
    @FXML private TableColumn<UserRow, String> roleColumn;
    @FXML private TableColumn<UserRow, String> statusColumn;
    @FXML private TableColumn<UserRow, String> createdDateColumn;
    @FXML private TableColumn<UserRow, String> actionsColumn;

    private List<UserRow> allUsers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("🔧 UserManagementController initialize() started...");
            statusLabel.setText("🔄 Initializing user management...");
            
            setupTable();
            loadUsers();
            updateStatistics();
            
            System.out.println("✅ UserManagementController initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error in UserManagementController initialize: " + e.getMessage());
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("❌ Initialization error: " + e.getMessage());
            }
        }
    }

    private void setupTable() {
        userIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserId()));
        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        createdDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedDate()));
        
        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<UserRow, String>() {
            private final Button editButton = new Button("✏️ Edit");
            private final Button deleteButton = new Button("🗑️ Delete");
            private final Button resetButton = new Button("🔑 Reset");
            private final HBox buttonBox = new HBox(5);

            {
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8; -fx-cursor: hand; -fx-font-size: 11px;");
                deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8; -fx-cursor: hand; -fx-font-size: 11px;");
                resetButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8; -fx-cursor: hand; -fx-font-size: 11px;");
                
                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.getChildren().addAll(editButton, resetButton, deleteButton);
                
                editButton.setOnAction(e -> {
                    UserRow user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });
                
                resetButton.setOnAction(e -> {
                    UserRow user = getTableView().getItems().get(getIndex());
                    handleResetPassword(user);
                });
                
                deleteButton.setOnAction(e -> {
                    UserRow user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
    }

    private void loadUsers() {
        try {
            System.out.println("🔧 Loading users...");
            statusLabel.setText("🔄 Loading users...");
            
            allUsers = new ArrayList<>();
            
            // Create sample users for testing
            allUsers.add(new UserRow("1", "admin", "admin@carrentalsystem.com", "Administrator", "Active", "2024-01-01"));
            allUsers.add(new UserRow("2", "manager", "manager@carrentalsystem.com", "Manager", "Active", "2024-01-15"));
            allUsers.add(new UserRow("3", "employee", "employee@carrentalsystem.com", "Employee", "Inactive", "2024-02-01"));
            
            displayUsers();
            statusLabel.setText("✅ Users loaded successfully - " + allUsers.size() + " users");
            
        } catch (Exception e) {
            statusLabel.setText("❌ Error loading users: " + e.getMessage());
            System.err.println("❌ Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayUsers() {
        ObservableList<UserRow> tableData = FXCollections.observableArrayList(allUsers);
        usersTable.setItems(tableData);
        totalRecordsLabel.setText("Total: " + allUsers.size() + " users");
    }

    private void updateStatistics() {
        try {
            if (allUsers == null) {
                totalUsersLabel.setText("0");
                activeUsersLabel.setText("0");
                adminUsersLabel.setText("0");
                return;
            }
            
            int totalUsers = allUsers.size();
            int activeUsers = (int) allUsers.stream().filter(u -> "Active".equals(u.getStatus())).count();
            int adminUsers = (int) allUsers.stream().filter(u -> "Administrator".equals(u.getRole())).count();
            
            totalUsersLabel.setText(String.valueOf(totalUsers));
            activeUsersLabel.setText(String.valueOf(activeUsers));
            adminUsersLabel.setText(String.valueOf(adminUsers));
            
        } catch (Exception e) {
            System.err.println("❌ Error updating statistics: " + e.getMessage());
            totalUsersLabel.setText("0");
            activeUsersLabel.setText("0");
            adminUsersLabel.setText("0");
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
    private void handleAddUser() {
        statusLabel.setText("🔄 Add User functionality - Coming soon!");
        System.out.println("Add User clicked");
    }

    private void handleEditUser(UserRow user) {
        statusLabel.setText("🔄 Edit User: " + user.getUsername());
        System.out.println("Edit User clicked: " + user.getUsername());
    }

    private void handleResetPassword(UserRow user) {
        statusLabel.setText("🔄 Reset Password for: " + user.getUsername());
        System.out.println("Reset Password clicked: " + user.getUsername());
    }

    private void handleDeleteUser(UserRow user) {
        statusLabel.setText("🔄 Delete User: " + user.getUsername());
        System.out.println("Delete User clicked: " + user.getUsername());
    }

    // UserRow class for table data
    public static class UserRow {
        private final String userId;
        private final String username;
        private final String email;
        private final String role;
        private final String status;
        private final String createdDate;

        public UserRow(String userId, String username, String email, String role, String status, String createdDate) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.role = role;
            this.status = status;
            this.createdDate = createdDate;
        }

        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getStatus() { return status; }
        public String getCreatedDate() { return createdDate; }
    }
} 