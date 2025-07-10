package com.prakhar.j2mepcemu;

import javax.swing.*;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

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

 //       JMenuItem themeItem = new JMenuItem("Change Theme...");
//        themeItem.addActionListener()

        // Create "Change Theme" submenu
        JMenu changeThemeMenu = new JMenu("Change Theme");

        // Light Theme option
        JMenuItem lightThemeItem = new JMenuItem("Light Theme");
        lightThemeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    for (Window window : Window.getWindows()) {
                        SwingUtilities.updateComponentTreeUI(window);
                    }
                } catch (UnsupportedLookAndFeelException ex) {
                    JOptionPane.showMessageDialog(parentFrame,
                            "Failed to switch to light theme.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        changeThemeMenu.add(lightThemeItem);

        // Dark Theme option
        JMenuItem darkThemeItem = new JMenuItem("Dark Theme");
        darkThemeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    for (Window window : Window.getWindows()) {
                        SwingUtilities.updateComponentTreeUI(window);
                    }
                } catch (UnsupportedLookAndFeelException ex) {
                    JOptionPane.showMessageDialog(parentFrame,
                            "Failed to switch to dark theme.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        changeThemeMenu.add(darkThemeItem);

        // Add the "Change Theme" submenu to the "Options" menu
        optionsMenu.add(changeThemeMenu);

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
}
