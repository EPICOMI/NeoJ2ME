package com.prakhar.j2mepcemu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HiddenGamesDialog extends JDialog {

    private DefaultListModel<String> hiddenGamesListModel;
    private JList<String> hiddenGamesList;
    private JButton unhideButton;
    private JButton closeButton;
    private JFrame parentFrame; // To call Main.refreshGameList() indirectly if needed or for modality

    public HiddenGamesDialog(JFrame parent) {
        super(parent, "Hidden Games Management", true); // Modal dialog
        this.parentFrame = parent;
        initComponents();
        loadHiddenGames();
        setSize(600, 400);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));


        hiddenGamesListModel = new DefaultListModel<>();
        hiddenGamesList = new JList<>(hiddenGamesListModel);
        hiddenGamesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Allow multi-select
        JScrollPane scrollPane = new JScrollPane(hiddenGamesList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        unhideButton = new JButton("Unhide Selected");
        closeButton = new JButton("Close");

        unhideButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                unhideSelectedGames();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        buttonPanel.add(unhideButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        hiddenGamesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                unhideButton.setEnabled(hiddenGamesList.getSelectedIndices().length > 0);
            }
        });
        unhideButton.setEnabled(false); // Initially disabled
    }

    private void loadHiddenGames() {
        hiddenGamesListModel.clear();
        try {
            Set<String> ignoredPaths = IgnoredGamesConfig.loadIgnoredGames();
            List<String> sortedPaths = new ArrayList<>(ignoredPaths);
            Collections.sort(sortedPaths); // Sort for consistent display

            for (String path : sortedPaths) {
                // Displaying just the filename and maybe parent dir for brevity
                File gameFile = new File(path);
                String displayName = gameFile.getName();
                File parentDir = gameFile.getParentFile();
                if (parentDir != null) {
                    displayName += " (" + parentDir.getName() + ")";
                }
                hiddenGamesListModel.addElement(path); // Store full path, but display could be friendlier
                                                       // For now, using full path for simplicity to map back
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading hidden games list: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.err.println("Error loading hidden games list: " + e.getMessage());
        }
        unhideButton.setEnabled(false); // Reset button state after loading
    }

    private void unhideSelectedGames() {
        List<String> selectedPaths = hiddenGamesList.getSelectedValuesList();
        if (selectedPaths.isEmpty()) {
            return;
        }

        int unhiddenCount = 0;
        boolean errorOccurred = false;

        for (String path : selectedPaths) {
            try {
                IgnoredGamesConfig.removeIgnoredGame(path); // This removes from config file
                unhiddenCount++;
            } catch (IOException e) {
                errorOccurred = true;
                System.err.println("Error unhiding game '" + path + "': " + e.getMessage());
                // Collect errors or show one by one? For now, log and continue.
            }
        }

        if (errorOccurred) {
            JOptionPane.showMessageDialog(this,
                    "An error occurred while trying to unhide some games. Please check logs.",
                    "Unhide Error",
                    JOptionPane.WARNING_MESSAGE);
        }

        if (unhiddenCount > 0) {
            // Refresh the list in this dialog
            loadHiddenGames();
            // Crucially, refresh the main game list in the Main window
            if (parentFrame instanceof Main) { // Basic check, not ideal but works for now
                 Main.refreshGameList();
            } else {
                // If parentFrame is not Main, or a more robust callback is needed:
                System.out.println("HiddenGamesDialog: Main game list refresh needed but parent not Main or no callback.");
                JOptionPane.showMessageDialog(this,
                    unhiddenCount + " game(s) unhidden. Please restart or refresh main list manually if it doesn't update.",
                    "Games Unhidden",
                    JOptionPane.INFORMATION_MESSAGE);

            }
             if (!errorOccurred) { // Only show success if no errors at all
                // This specific dialog might be too much if the list auto-refreshes successfully.
                // JOptionPane.showMessageDialog(this, unhiddenCount + " game(s) unhidden successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
         // Ensure button is disabled if list becomes empty or no selection
        unhideButton.setEnabled(hiddenGamesList.getSelectedIndices().length > 0);
    }
}
