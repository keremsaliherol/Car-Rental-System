package com.kerem.ordersystem.carrentalsystem.util;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;

/**
 * Utility class for configuring JavaFX stages with consistent fullscreen settings
 * and smooth transitions
 */
public class StageUtils {
    
    // Store the current stage dimensions to prevent flickering
    private static double currentWidth = 0;
    private static double currentHeight = 0;
    private static boolean isFullscreen = false;
    
    /**
     * Configure a stage with fullscreen settings
     * @param stage The stage to configure
     * @param title The window title
     */
    public static void configureFullscreenStage(Stage stage, String title) {
        // Get screen bounds
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        // Store current dimensions if not set
        if (currentWidth == 0 || currentHeight == 0) {
            currentWidth = screenBounds.getWidth();
            currentHeight = screenBounds.getHeight();
        }
        
        stage.setTitle(title);
        stage.setResizable(true);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        // Set size before fullscreen to prevent flickering
        if (!isFullscreen) {
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
        }
        
        stage.setMaximized(true);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("Press ESC to exit fullscreen mode");
        
        isFullscreen = true;
    }
    
    /**
     * Create a new scene and configure the stage for fullscreen with smooth transition
     * @param stage The stage to configure
     * @param root The root node for the scene
     * @param title The window title
     */
    public static void setFullscreenScene(Stage stage, Parent root, String title) {
        // Store current stage properties
        boolean wasFullscreen = stage.isFullScreen();
        boolean wasMaximized = stage.isMaximized();
        double stageWidth = stage.getWidth();
        double stageHeight = stage.getHeight();
        double stageX = stage.getX();
        double stageY = stage.getY();
        
        // Update stored dimensions
        if (stageWidth > 0 && stageHeight > 0) {
            currentWidth = stageWidth;
            currentHeight = stageHeight;
        }
        
        // Create scene with preserved dimensions
        Scene scene = new Scene(root, currentWidth, currentHeight);
        
        // Temporarily disable fullscreen to prevent flickering
        if (wasFullscreen) {
            stage.setFullScreen(false);
        }
        
        // Set the new scene
        stage.setScene(scene);
        
        // Restore stage properties smoothly
        Platform.runLater(() -> {
            stage.setTitle(title);
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            // Restore position and size
            stage.setX(stageX);
            stage.setY(stageY);
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
            
            // Restore maximized and fullscreen states
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            if (wasFullscreen) {
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("Press ESC to exit fullscreen mode");
            }
        });
    }
    
    /**
     * Show a new fullscreen stage with smooth transition
     * @param stage The stage to show
     * @param root The root node for the scene
     * @param title The window title
     */
    public static void showFullscreenStage(Stage stage, Parent root, String title) {
        setFullscreenScene(stage, root, title);
        if (!stage.isShowing()) {
            stage.show();
        }
    }
    
    /**
     * Initialize stage dimensions from current stage
     * @param stage The current stage to get dimensions from
     */
    public static void initializeDimensions(Stage stage) {
        if (stage != null) {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            currentWidth = stage.getWidth() > 0 ? stage.getWidth() : screenBounds.getWidth();
            currentHeight = stage.getHeight() > 0 ? stage.getHeight() : screenBounds.getHeight();
            isFullscreen = stage.isFullScreen();
        }
    }
} 