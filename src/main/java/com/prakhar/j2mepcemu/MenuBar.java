package com.prakhar.j2mepcemu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

        JMenuItem settingsItem = new JMenuItem("Settings..."); // Added ellipsis
        settingsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSettingsDialog();
            }
        });
        optionsMenu.add(settingsItem);

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
