package com.mygameapp.neoj2me;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder; // For padding
import java.io.File;
import javax.swing.JFileChooser;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException; // Added for exception handling
// Ensure com.prakhar.j2mepcemu.GameDirectoryConfig is implicitly available or import it if in different package.


public class SettingsDialog extends JDialog {

    private JTabbedPane tabbedPane;
    private JList<String> gameDirectoriesList;
    private DefaultListModel<String> gameDirectoriesListModel;
    private JButton addButton;
    private JButton removeButton;

    public SettingsDialog(JFrame parent) {
        super(parent, "Settings", true); // true for modal
        initComponents();
        setSize(500, 400);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        // Game Directories Tab
        JPanel gameDirectoriesPanel = new JPanel(new BorderLayout(5, 5)); // Added layout and gaps
        gameDirectoriesPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Added padding

        gameDirectoriesListModel = new DefaultListModel<>();
        gameDirectoriesList = new JList<>(gameDirectoriesListModel);
        // TODO: Load saved directories into gameDirectoriesListModel in a later step

        JScrollPane scrollPane = new JScrollPane(gameDirectoriesList);
        gameDirectoriesPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanelSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("Add");
        removeButton = new JButton("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(e -> removeDirectoryAction()); // Add this line

        // Add ActionListeners for these buttons in the next step (functionality)
        // For now, just adding them to the panel
        buttonPanelSouth.add(addButton);
        buttonPanelSouth.add(removeButton);
        addButton.addActionListener(e -> addDirectoryAction()); // Add this line


        gameDirectoriesPanel.add(buttonPanelSouth, BorderLayout.SOUTH);

        tabbedPane.addTab("Game Directories", gameDirectoriesPanel);

        // Add ListSelectionListener to enable/disable removeButton
        gameDirectoriesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                removeButton.setEnabled(gameDirectoriesList.getSelectedIndex() != -1);
            }
        });

        loadConfiguredDirectories(); // Load saved directories when dialog is created

        add(tabbedPane, BorderLayout.CENTER);

        // OK and Cancel buttons (or Apply/Close)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            // Changes are already saved on Add/Remove.
            // If there were other settings, they might be applied here.
            // The main purpose of OK here is to close the dialog.
            // If a refresh mechanism for Main.java was in place, it might be triggered here too or on Add/Remove.
            setVisible(false);
            dispose();
        });

        cancelButton.addActionListener(e -> {
            setVisible(false);
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addDirectoryAction() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Game Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDir = chooser.getSelectedFile();
            if (selectedDir != null) {
                String dirPath = selectedDir.getAbsolutePath();
                if (!gameDirectoriesListModel.contains(dirPath)) {
                    gameDirectoriesListModel.addElement(dirPath);
                    saveConfiguredDirectories(); // This saves game_dirs.conf

                    // New: Clear permanently removed games under this newly added directory path
                    try {
                        PermanentlyRemovedGamesConfig.removePermanentlyRemovedGamesUnderDirectory(dirPath);
                        // System.out.println("Cleared any permanently removed entries under: " + dirPath);
                    } catch (IOException e_perm_remove) {
                        System.err.println("Error clearing permanently removed games for directory '" + dirPath + "': " + e_perm_remove.getMessage());
                        // Optionally, show a non-critical error to the user if this fails
                        JOptionPane.showMessageDialog(this,
                                "Note: Could not automatically un-remove games previously removed from '" + selectedDir.getName() + "'.\n" +
                                "If any games from this directory don't appear, try drag-and-dropping them.",
                                "Notice",
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                    Main.refreshGameList(); // Refresh main list
                } else {
                    JOptionPane.showMessageDialog(this, "Directory already added.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    private void removeDirectoryAction() {
        int selectedIndex = gameDirectoriesList.getSelectedIndex();
        if (selectedIndex != -1) {
            // String dirToRemove = gameDirectoriesListModel.getElementAt(selectedIndex); // For logging if needed
            gameDirectoriesListModel.remove(selectedIndex);
            saveConfiguredDirectories(); // Save after removing
            Main.refreshGameList();

            if (gameDirectoriesListModel.isEmpty()) {
                removeButton.setEnabled(false);
            } else {
                // Adjust selection after removal
                if (selectedIndex >= gameDirectoriesListModel.getSize()) { // If last item was removed
                    gameDirectoriesList.setSelectedIndex(gameDirectoriesListModel.getSize() - 1);
                } else { // Otherwise, select the item at the same index (which is now the next item)
                    gameDirectoriesList.setSelectedIndex(selectedIndex);
                }
            }
             // Ensure remove button is disabled if list becomes empty or no selection
            removeButton.setEnabled(gameDirectoriesList.getSelectedIndex() != -1);
        }
    }

    private void saveConfiguredDirectories() {
        List<String> dirs = new ArrayList<>();
        for (int i = 0; i < gameDirectoriesListModel.getSize(); i++) {
            dirs.add(gameDirectoriesListModel.getElementAt(i));
        }
        try {
            GameDirectoryConfig.saveDirectories(dirs);
            // System.out.println("Game directories saved to config."); // Optional: keep for console logging
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving game directories: " + e.getMessage(),
                    "Configuration Error",
                    JOptionPane.ERROR_MESSAGE);
            System.err.println("Error saving game directories: " + e.getMessage()); // Keep console log for dev
        }
    }

    private void loadConfiguredDirectories() {
        gameDirectoriesListModel.clear();
        try {
            List<String> dirs = GameDirectoryConfig.loadDirectories();
            for (String dir : dirs) {
                if (!gameDirectoriesListModel.contains(dir)) {
                     gameDirectoriesListModel.addElement(dir);
                }
            }
            // System.out.println("Game directories loaded from config: " + dirs.size() + " entries.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading game directories: " + e.getMessage(),
                    "Configuration Error",
                    JOptionPane.ERROR_MESSAGE);
            System.err.println("Error loading game directories: " + e.getMessage());
        }
    }

    // Method to add tabs from outside if needed, though for now it's internal
    public void addSettingTab(String title, Component component) {
        tabbedPane.addTab(title, component);
    }
}
