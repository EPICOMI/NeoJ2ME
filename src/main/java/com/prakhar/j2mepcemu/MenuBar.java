package com.prakhar.j2mepcemu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.io.*;
import java.util.Properties;

// Ensure this class declaration was not accidentally removed or malformed
public class MenuBar {

    private JFrame parentFrame; // To link actions to the main frame

    public MenuBar(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = createFileMenu();
        menuBar.add(fileMenu);

        // Options Menu
        JMenu optionsMenu = createOptionsMenu();
        menuBar.add(optionsMenu);

        return menuBar;
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");

        // "Hidden Games" menu item
        JMenuItem hiddenGamesItem = new JMenuItem("Hidden Games..."); // Ellipsis indicates it opens a dialog
        hiddenGamesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHiddenGamesDialog();
            }
        });
        fileMenu.add(hiddenGamesItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        return fileMenu;
    }

    private JMenu createOptionsMenu() {
        JMenu optionsMenu = new JMenu("Options");

        JMenuItem settingsItem = new JMenuItem("Settings"); // Added ellipsis
        settingsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSettingsDialog();
            }
        });
        optionsMenu.add(settingsItem);

        // Create "Change Theme" submenu
        JMenu changeThemeMenu = new JMenu("Change Theme");

        // Light Theme option
//        JMenuItem lightThemeItem = new JMenuItem("Light Theme");
//        lightThemeItem.addActionListener(e -> MenuBar.this.setTheme("light"));
//        changeThemeMenu.add(lightThemeItem);

        // Dark Theme option
//        JMenuItem darkThemeItem = new JMenuItem("Dark Theme");
//        darkThemeItem.addActionListener(e -> MenuBar.this.setTheme("dark"));
//        changeThemeMenu.add(darkThemeItem);

        // Light Theme option
        JMenuItem lightThemeItem = new JMenuItem("Light Theme");
        lightThemeItem.addActionListener(e -> setTheme("light"));
        changeThemeMenu.add(lightThemeItem);

        // Dark Theme option
        JMenuItem darkThemeItem = new JMenuItem("Dark Theme");
        darkThemeItem.addActionListener(e -> setTheme("dark"));
        changeThemeMenu.add(darkThemeItem);


/*
        JMenuItem customColorsItem = new JMenuItem("Customize...");
        customColorsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newBackground = JColorChooser.showDialog(parentFrame, "Choose Background Color", null);
                if (newBackground != null) {
                    UIManager.put("Panel.background", newBackground);
                    SwingUtilities.updateComponentTreeUI(parentFrame);
                }
            }
        });
        changeThemeMenu.add(customColorsItem);
*/

        // Submenu for color customization
        JMenu customizeColorsMenu = new JMenu("Customize...");

//        // Background Color
//        JMenuItem backgroundColorItem = new JMenuItem("Background Color");
//        backgroundColorItem.addActionListener(e -> customizeColor("Panel.background", "Choose Background Color"));
//        customizeColorsMenu.add(backgroundColorItem);

//        // Text Color
//        JMenuItem textColorItem = new JMenuItem("Text Color");
//        textColorItem.addActionListener(e -> customizeColor("Label.foreground", "Choose Text Color"));
//        customizeColorsMenu.add(textColorItem);

        // Selection Color
        JMenuItem selectionColorItem = new JMenuItem("Selection Color");
        selectionColorItem.addActionListener(e -> customizeColor("List.selectionBackground", "Choose Selection Color"));
        customizeColorsMenu.add(selectionColorItem);

        changeThemeMenu.add(customizeColorsMenu);

        // Add the "Change Theme" submenu to the "Options" menu
        optionsMenu.add(changeThemeMenu);

        UIManager.put( "Button.arc", 999);

        return optionsMenu;
    }

    // Method to show the SettingsDialog
    private void showSettingsDialog() {
        if (parentFrame == null) {
            System.err.println("Cannot show SettingsDialog: parentFrame is null.");
            return;
        }
        SettingsDialog settingsDialog = new SettingsDialog(parentFrame);
        settingsDialog.setVisible(true);
    }

    // Method to show the HiddenGamesDialog
    private void showHiddenGamesDialog() {
        if (parentFrame == null) {
            System.err.println("Cannot show HiddenGamesDialog: parentFrame is null.");
            return;
        }
        HiddenGamesDialog dialog = new HiddenGamesDialog(parentFrame);
        dialog.setVisible(true);
    }

    private void customizeColor(String uiKey, String chooserTitle) {
        Color currentColor = UIManager.getColor(uiKey);
        Color newColor = JColorChooser.showDialog(parentFrame, chooserTitle, currentColor);
        if (newColor != null) {
            // Apply the color to the UI immediately
            UIManager.put(uiKey, newColor);
            updateAllWindowsUI();

            // Save the color to the properties file
            try {
                Properties props = new Properties();
                File configDir = new File(System.getProperty("user.home") + "/.myapp");
                File configFile = new File(configDir, "config.properties");

                if (configFile.exists()) {
                    try (FileInputStream in = new FileInputStream(configFile)) {
                        props.load(in);
                    }
                }

                // Convert color to hex format #RRGGBB
                String hexColor = String.format("#%02x%02x%02x", newColor.getRed(), newColor.getGreen(), newColor.getBlue());
                props.setProperty("selection.background", hexColor);

                configDir.mkdirs();
                try (FileOutputStream out = new FileOutputStream(configFile)) {
                    props.store(out, "Theme configuration");
                }

            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parentFrame,
                        "Error saving color setting.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateAllWindowsUI() {
        for (Window window : Window.getWindows()) {
            try {
                SwingUtilities.updateComponentTreeUI(window);
            } catch (Exception ex) {
                System.err.println("Failed to update UI for window: " + window);
                ex.printStackTrace();
            }
        }
    }
// setTheme was originally static
    public void setTheme(String theme) {
        try {
            Properties props = new Properties();
            File configDir = new File(System.getProperty("user.home") + "/.myapp");
            File configFile = new File(configDir, "config.properties");

            // Load existing properties if the file exists
            if (configFile.exists()) {
                try (FileInputStream in = new FileInputStream(configFile)) {
                    props.load(in);
                }
            }

            // Update the theme property
            props.setProperty("theme", theme);

            // Save the updated properties
            configDir.mkdirs(); // Ensure the directory exists
            try (FileOutputStream out = new FileOutputStream(configFile)) {
                props.store(out, "Theme configuration");
            }

            // Apply the new look and feel
            if ("light".equals(theme)) {
                FlatLightLaf.setup();
            } else if ("dark".equals(theme)) {
                // Apply the base FlatDarkLaf
                FlatDarkLaf.setup();

                // Override specific colors to make it darker
                Color darkerBackground = Color.decode("#303030"); // A darker charcoal
                UIManager.put("Panel.background", darkerBackground);
                UIManager.put("View.background", darkerBackground); // For JScrollPane viewport backgrounds
                UIManager.put("List.background", darkerBackground);
                UIManager.put("TextField.background", darkerBackground);
                UIManager.put("TextArea.background", darkerBackground);
                UIManager.put("Button.background", Color.decode("#424242")); // Slightly lighter for buttons
                UIManager.put("Button.hoverBackground", Color.decode("#505050"));
                UIManager.put("Button.pressedBackground", Color.decode("#585858"));

            }

            // Update the UI immediately for all windows
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame,
                    "Failed to apply theme: " + theme,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }



}

