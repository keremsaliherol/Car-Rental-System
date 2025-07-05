package com.kerem.ordersystem.carrentalsystem.util;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.stage.PopupWindow;

/**
 * Utility class to fix ComboBox dropdown positioning issues in JavaFX
 */
public class ComboBoxUtils {
    
    /**
     * Fixes ComboBox dropdown positioning to appear below the ComboBox
     * @param comboBox The ComboBox to fix
     */
    public static void fixDropdownPosition(ComboBox<?> comboBox) {
        if (comboBox == null) return;
        
        Platform.runLater(() -> {
            try {
                // Set up event handler for when ComboBox is about to show
                comboBox.setOnShowing(event -> {
                    Platform.runLater(() -> {
                        try {
                            // Get the ComboBox skin
                            if (comboBox.getSkin() instanceof ComboBoxListViewSkin) {
                                ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) comboBox.getSkin();
                                
                                // Use reflection to access the popup
                                var popupField = ComboBoxListViewSkin.class.getDeclaredField("popup");
                                popupField.setAccessible(true);
                                PopupWindow popup = (PopupWindow) popupField.get(skin);
                                
                                if (popup != null) {
                                    // Set anchor location to appear below ComboBox
                                    popup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_LEFT);
                                    popup.setAutoFix(false);
                                    
                                    // Calculate position below ComboBox
                                    var bounds = comboBox.localToScreen(comboBox.getBoundsInLocal());
                                    if (bounds != null) {
                                        popup.setX(bounds.getMinX());
                                        popup.setY(bounds.getMaxY());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Fallback: try alternative approach
                            System.err.println("ComboBox positioning fallback: " + e.getMessage());
                        }
                    });
                });
                
                System.out.println("✅ ComboBox dropdown positioning configured for: " + comboBox.getId());
            } catch (Exception e) {
                System.err.println("❌ Error configuring ComboBox dropdown positioning: " + e.getMessage());
            }
        });
    }
    
    /**
     * Applies dropdown position fix to multiple ComboBoxes
     * @param comboBoxes Array of ComboBoxes to fix
     */
    public static void fixDropdownPositions(ComboBox<?>... comboBoxes) {
        for (ComboBox<?> comboBox : comboBoxes) {
            fixDropdownPosition(comboBox);
        }
    }
} 