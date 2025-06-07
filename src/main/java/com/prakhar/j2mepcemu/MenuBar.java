package com.prakhar.j2mepcemu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuBar {

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
        // Future items like "Exit" can be added here
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
}
