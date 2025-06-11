package com.prakhar.j2mepcemu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// import com.prakhar.j2mepcemu.HiddenGamesDialog; // Might be needed

    private JFrame parentFrame; // To link actions to the main frame if needed

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

        // New "Hidden Games" menu item
        JMenuItem hiddenGamesItem = new JMenuItem("Hidden Games..."); // Ellipsis indicates it opens a dialog
        hiddenGamesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHiddenGamesDialog();
            }
        });
        fileMenu.add(hiddenGamesItem);

        // Add a separator for visual distinction before Exit, if desired
        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        return fileMenu;
    }

    private JMenu createOptionsMenu() {
        JMenu optionsMenu = new JMenu("Options");

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSettingsDialog();
            }
        });
        optionsMenu.add(settingsItem);

        return optionsMenu;
    }

    public void showSettingsDialog() {
        SettingsDialog settingsDialog = new SettingsDialog(parentFrame);
        settingsDialog.setVisible(true);
    }

    private void showHiddenGamesDialog() {
        if (parentFrame == null) {
            // This should ideally not happen if MenuBar is constructed properly
            System.err.println("Cannot show HiddenGamesDialog: parentFrame is null.");
            return;
        }
        HiddenGamesDialog dialog = new HiddenGamesDialog(parentFrame);
        dialog.setVisible(true); // Show the dialog
    }
}
