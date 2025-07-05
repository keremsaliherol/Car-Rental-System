package com.kerem.ordersystem.carrentalsystem.controller;

import com.kerem.ordersystem.carrentalsystem.database.CustomerDAO;
import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import com.kerem.ordersystem.carrentalsystem.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CustomerViewController {

    @FXML
    private TableView<Customer> customerTable;

    @FXML
    private TableColumn<Customer, Integer> idColumn;

    @FXML
    private TableColumn<Customer, String> nameColumn;

    @FXML
    private TableColumn<Customer, String> phoneColumn;

    @FXML
    private TableColumn<Customer, String> addressColumn;

    @FXML
    public void initialize() {
        // Sütunları modeldeki property'lerle eşleştir
        idColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        refreshTable(); // ekran ilk açıldığında tabloyu doldur
    }

    // Tabloyu güncelleyen metot
    public void refreshTable() {
        ObservableList<Customer> customers = FXCollections.observableArrayList(CustomerDAO.getAllCustomers());
        customerTable.setItems(customers);
    }

    @FXML
    private void handleUpdate() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/customer-update-form.fxml"));
                Parent root = loader.load();

                // Controller'a seçilen müşteriyi aktar
                CustomerFormController formController = loader.getController();
                formController.setCustomerToEdit(selectedCustomer);

                Stage stage = (Stage) customerTable.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Update Customer");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Uyarı göster
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Customer Selected");
            alert.setContentText("Please select a customer to update.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDelete() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Confirmation");
            confirmation.setHeaderText("Are you sure you want to delete this customer?");
            confirmation.setContentText("Customer: " + selectedCustomer.getFullName());

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    deleteCustomerById(selectedCustomer.getCustomerId());
                    refreshTable(); // tabloyu yenile
                }
            });

        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Customer Selected");
            alert.setContentText("Please select a customer to delete.");
            alert.showAndWait();
        }
    }

    private void deleteCustomerById(int customerId) {
        String sql = "DELETE FROM Customers WHERE CustomerID = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            int deleted = stmt.executeUpdate();

            if (deleted > 0) {
                System.out.println("Customer deleted successfully.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Could not delete customer.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void openMyRentals() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/my-rentals.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) customerTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Rentals");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit-profile.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) customerTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Profile");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenChangePassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/change-password.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) customerTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Change Password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) customerTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) customerTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not navigate back to dashboard");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
