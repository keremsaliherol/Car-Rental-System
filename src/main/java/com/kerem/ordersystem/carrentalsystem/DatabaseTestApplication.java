package com.kerem.ordersystem.carrentalsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX Application to test all 50 database objects:
 * 10 Views + 10 Stored Procedures + 10 Functions + 10 Indexes + 10 Triggers
 */
public class DatabaseTestApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("🚀 Starting Database Test Application...");
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kerem/ordersystem/carrentalsystem/database-test-view.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1200, 800);
        
        primaryStage.setTitle("🗄️ Car Rental System - Database Objects Test Interface");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        
        System.out.println("✅ Database Test Application started successfully!");
        System.out.println("📊 Ready to test 50 database objects:");
        System.out.println("   • 10 Views");
        System.out.println("   • 10 Stored Procedures");
        System.out.println("   • 10 Functions");
        System.out.println("   • 10 Indexes");
        System.out.println("   • 10 Triggers");
    }

    public static void main(String[] args) {
        launch(args);
    }
} 