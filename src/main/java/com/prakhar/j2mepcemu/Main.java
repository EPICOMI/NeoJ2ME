package com.prakhar.j2mepcemu;

import com.prakhar.j2mepcemu.MenuBar;
import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet; // For efficient lookup of existing game file paths
import java.util.Set; // For the HashSet
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import com.prakhar.j2mepcemu.IgnoredGamesConfig; // New
import java.io.IOException; // For handling exceptions from IgnoredGamesConfig
import org.recompile.freej2me.FreeJ2ME;

public class Main {
    private static JFrame frame;
    private static JList<String> gameList;
    private static DefaultListModel<String> listModel;
    private static JLabel dragDropLabel;
    private static ArrayList<File> gameFiles;
    private static FreeJ2ME currentEmulator; // Track current instance
    private static JPopupMenu gameListContextMenu; // New

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        frame = new JFrame("FreeJ2ME Frontend");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        gameList = new JList<>(listModel);
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(gameList);
        frame.add(scrollPane, BorderLayout.CENTER);

        dragDropLabel = new JLabel("Drag and drop JAR files here", SwingConstants.CENTER);
        dragDropLabel.setOpaque(true);
        dragDropLabel.setBackground(Color.LIGHT_GRAY);

        JButton importButton = new JButton("Import Game(s)");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setFileFilter(new FileNameExtensionFilter("Java Archives (*.jar)", "jar"));
                fileChooser.setDialogTitle("Select Game JARs to Import");
                int returnValue = fileChooser.showOpenDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();
                    if (selectedFiles != null && selectedFiles.length > 0) {
                        processAndAddGameFiles(Arrays.asList(selectedFiles), dragDropLabel);
                    }
                }
            }
        });

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(importButton, BorderLayout.WEST);
        southPanel.add(dragDropLabel, BorderLayout.CENTER);

        frame.add(southPanel, BorderLayout.SOUTH);

        gameFiles = new ArrayList<>();

        // Setup Context Menu for gameList
        gameListContextMenu = new JPopupMenu();
        JMenuItem playItem = new JMenuItem("Play");
        playItem.addActionListener(e -> {
            int selectedIndex = gameList.getSelectedIndex();
            if (selectedIndex != -1) {
                File gameFile = gameFiles.get(selectedIndex);
                launchGame(gameFile);
            }
        });
        gameListContextMenu.add(playItem);

        JMenuItem removeItem = new JMenuItem("Remove");
        removeItem.addActionListener(e -> {
            int selectedIndex = gameList.getSelectedIndex();
            if (selectedIndex != -1) {
                // Ensure indices are valid before removing
                if (selectedIndex < gameFiles.size() && selectedIndex < listModel.getSize()) {
                    File gameFileToRemove = gameFiles.get(selectedIndex); // Get the File object
                    String gamePath = gameFileToRemove.getAbsolutePath();
                    String gameDisplayName = listModel.getElementAt(selectedIndex); // For feedback

                    try {
                        IgnoredGamesConfig.addIgnoredGame(gamePath); // Add to ignored list

                        // Now remove from current view
                        gameFiles.remove(selectedIndex);
                        listModel.remove(selectedIndex);

                        dragDropLabel.setText("'" + gameDisplayName + "' hidden. Drag & drop it again to unhide.");
                        // System.out.println("Game hidden: " + gamePath);

                    } catch (IOException ex) {
                        System.err.println("Error trying to hide game '" + gamePath + "': " + ex.getMessage());
                        JOptionPane.showMessageDialog(frame, // Use 'frame' as parent
                                "Error hiding game: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        // Optionally, do not remove from list if saving ignore state failed
                        // For now, it's removed from list even if ignore save fails, but error is shown.
                    }
                } else {
                    System.err.println("Error: Index mismatch when trying to remove game from list.");
                    dragDropLabel.setText("Error removing game. Index mismatch.");
                }
            }
        });
        gameListContextMenu.add(removeItem);

        // Listener for showing the context menu
        gameList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) { // For cross-platform popup trigger
                    int row = gameList.locationToIndex(e.getPoint());
                    if (row != -1) { // If click is on an item
                        // Select the item under the mouse cursor before showing the context menu
                        // This ensures the context menu actions apply to the right-clicked item
                        if (gameList.getSelectedIndex() != row) {
                             gameList.setSelectedIndex(row);
                        }
                        gameListContextMenu.show(e.getComponent(), e.getX(), e.getY());
                    } else {
                        // Optional: If right-click is not on an item, clear selection or do nothing
                        // gameList.clearSelection();
                    }
                }
            }
        });

        setupDragAndDrop();
        setupGameListListener(); // This is the double-click listener

        // loadGamesFromDirectory("games"); // Remove this line
        loadGamesFromConfiguredDirectories(); // Add this line

        // Create and set the menu bar
        MenuBar menuBarComponent = new MenuBar(frame); // Pass the frame instance
        frame.setJMenuBar(menuBarComponent.createMenuBar());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    private static void setupDragAndDrop() {
        // The DropTarget is correctly set on 'frame' for "drop anywhere"
        new DropTarget(frame, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    Transferable transferable = dtde.getTransferable();
                    try {
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        // Check if at least one file is a JAR file for early feedback
                        if (files.stream().anyMatch(file -> file.getName().toLowerCase().endsWith(".jar"))) {
                            dtde.acceptDrag(DnDConstants.ACTION_COPY);
                        } else {
                            dtde.rejectDrag(); // Reject if no JAR files are in the drag operation
                        }
                    } catch (UnsupportedFlavorException | IOException e) {
                        // Problem getting data, usually means we can't handle it or it's not a file list
                        dtde.rejectDrag();
                    }
                } else {
                    dtde.rejectDrag(); // Data flavor not supported
                }
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked") // Standard practice for this cast
                    List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    processAndAddGameFiles(droppedFiles, dragDropLabel);
                    dtde.dropComplete(true);
                } catch (UnsupportedFlavorException | IOException e) {
                    dragDropLabel.setText("Error processing drop: " + e.getMessage());
                    System.err.println("Drop failed (flavor/IO): " + e.getMessage());
                    dtde.dropComplete(false);
                } catch (Exception e) { // Catch any other unexpected errors
                    dragDropLabel.setText("Unexpected error during drop: " + e.getMessage());
                    System.err.println("Unexpected drop error: " + e.getMessage());
                    e.printStackTrace();
                    dtde.dropComplete(false);
                }
            }
        });
    }

    private static void processAndAddGameFiles(List<File> files, JLabel feedbackLabel) {
        File gamesDir = new File("games"); // Target directory for dropped games
        if (!gamesDir.exists()) {
            if (!gamesDir.mkdirs()) {
                feedbackLabel.setText("Error: Cannot create 'games' directory.");
                System.err.println("Error: Cannot create 'games' directory.");
                return;
            }
        }

        int filesCopiedCount = 0;
        int filesUnhiddenCount = 0; // New: Track unhidden files
        Set<String> currentIgnoredGames = null; // Load once

        try {
            currentIgnoredGames = IgnoredGamesConfig.loadIgnoredGames();
        } catch (IOException e) {
            System.err.println("Error loading ignored games list: " + e.getMessage());
            feedbackLabel.setText("Error loading ignored games list. Proceeding without unhide.");
            // Initialize to empty set to avoid NullPointerExceptions later if loading fails
            currentIgnoredGames = new HashSet<>();
        }

        for (File file : files) { // Iterate through files dropped by the user
            if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                File destFile = new File(gamesDir, file.getName()); // Potential destination in "games" dir
                String destPath = destFile.getAbsolutePath();

                try {
                    // Check if this destination path is in the loaded ignored list
                    if (IgnoredGamesConfig.isGameIgnored(destPath, currentIgnoredGames)) {
                        IgnoredGamesConfig.removeIgnoredGame(destPath); // Unhide it (this will re-save the config)
                        currentIgnoredGames.remove(destPath); // Update our current set to reflect the change
                        filesUnhiddenCount++;
                        // System.out.println("Game unhidden by drag & drop: " + destPath);
                    }

                    // Copy the file
                    Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    filesCopiedCount++;

                } catch (IOException ex) {
                    System.err.println("Error processing dropped file '" + file.getName() + "': " + ex.getMessage());
                    // Show specific error related to this file to the user
                    JOptionPane.showMessageDialog(frame,
                            "Error processing dropped file '" + file.getName() + "':\n" + ex.getMessage(),
                            "File Drop Error",
                            JOptionPane.ERROR_MESSAGE);
                    // Continue to next file if one fails
                }
            }
        }

        if (filesCopiedCount > 0 || filesUnhiddenCount > 0) {
            StringBuilder feedbackMsg = new StringBuilder();
            if (filesCopiedCount > 0) {
                feedbackMsg.append("Copied ").append(filesCopiedCount).append(" JAR(s). ");
            }
            if (filesUnhiddenCount > 0) {
                feedbackMsg.append(filesUnhiddenCount).append(" previously hidden game(s) unhidden. ");
            }
            feedbackLabel.setText(feedbackMsg.toString().trim() + " List refreshed.");
            refreshGameList(); // Refresh the list to show newly dropped/unhidden games
        } else {
            // If no files were copied or unhidden, but the loop ran (e.g. non-JAR files dropped),
            // provide a generic message. If 'files' was empty, this won't be reached.
            if (!files.isEmpty()) {
                 feedbackLabel.setText("No new JAR files processed. Drag JAR files here.");
            } else {
                // This case should ideally not happen if processAndAddGameFiles is called with non-empty list.
                // For safety, ensure label is reasonable.
                dragDropLabel.setText("Drag & drop JARs or configure game directories in Settings.");
            }
        }
    }

    private static void setupGameListListener() {
        gameList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = gameList.getSelectedIndex();
                    if (index != -1) {
                        File gameFile = gameFiles.get(index);
                        launchGame(gameFile);
                    }
                }
            }
        });
    }

    private static void launchGame(File gameFile) {
        try {
            // Shut down previous emulator if active
            if (currentEmulator != null) {
                currentEmulator.shutdown();
                currentEmulator = null;
            }
            String encodedPath = URLEncoder.encode(gameFile.getAbsolutePath().replace("\\", "/"), StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
            String[] args = {"file:///" + encodedPath};
            currentEmulator = new FreeJ2ME(args);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to launch game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            if (currentEmulator != null) {
                currentEmulator.shutdown();
                currentEmulator = null;
            }
        }
    }

    // private static void loadGamesFromDirectory(String dirPath) { ... } // REMOVE or COMMENT OUT OLD METHOD

    private static void scanDirectoryForGamesInternal(File directory, Set<String> scannedGamePaths, Set<String> ignoredGamesSet) { // Added ignoredGamesSet parameter
        if (!directory.exists() || !directory.isDirectory()) {
            // System.err.println("Cannot scan directory: " + directory.getAbsolutePath() + " - does not exist or not a directory.");
            return;
        }

        File[] files = directory.listFiles((d, name) -> name.toLowerCase().endsWith(".jar"));
        if (files != null) {
            for (File file : files) {
                String absolutePath = file.getAbsolutePath(); // Get absolute path once

                // Skip if already processed OR if it's in the ignored list
                if (scannedGamePaths.contains(absolutePath) || IgnoredGamesConfig.isGameIgnored(absolutePath, ignoredGamesSet)) {
                    // System.out.println("Skipping (already scanned or ignored): " + absolutePath);
                    continue;
                }

                // Add to listModel and gameFiles
                // Suffixing with parent directory name helps distinguish if same JAR name exists in multiple places
                String gameDisplayName = file.getName();
                if (directory.getName().equals("games") && file.getParentFile().equals(new File("games").getAbsoluteFile())) {
                     // For files directly in our 'games' root, don't add "(games)" suffix
                } else {
                     gameDisplayName += " (" + directory.getName() + ")";
                }

                listModel.addElement(gameDisplayName);
                gameFiles.add(file);
                scannedGamePaths.add(absolutePath);
            }
        }

        File[] subDirs = directory.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                scanDirectoryForGamesInternal(subDir, scannedGamePaths, ignoredGamesSet); // Recursive call, pass ignoredGamesSet
            }
        }
    }

    private static void loadGamesFromConfiguredDirectories() {
        listModel.clear();
        gameFiles.clear();
        Set<String> scannedGamePaths = new HashSet<>();
        Set<String> ignoredGamesSet = new HashSet<>(); // New: For storing ignored games

        try {
            ignoredGamesSet = IgnoredGamesConfig.loadIgnoredGames();
            // System.out.println("Loaded " + ignoredGamesSet.size() + " ignored game path(s).");
        } catch (IOException e) {
            System.err.println("Error loading ignored games configuration: " + e.getMessage());
            // If loading ignored games fails, proceed without ignoring.
            // Alternatively, could show a user error, but this is less critical than game dirs.
        }

        // 1. Always scan the local "games" directory
        File localGamesDir = new File("games");
        if (!localGamesDir.exists()) {
            localGamesDir.mkdirs();
        }
        scanDirectoryForGamesInternal(localGamesDir, scannedGamePaths, ignoredGamesSet); // Pass ignoredGamesSet

        // 2. Scan all user-configured directories
        List<String> configuredPaths = new ArrayList<>();
        try {
            configuredPaths = GameDirectoryConfig.loadDirectories();
        } catch (java.io.IOException e) {
            System.err.println("Error loading game directory configurations: " + e.getMessage());
        }

        for (String path : configuredPaths) {
            File dir = new File(path);
            scanDirectoryForGamesInternal(dir, scannedGamePaths, ignoredGamesSet); // Pass ignoredGamesSet
        }

        if (listModel.isEmpty()) {
            dragDropLabel.setText("No games found. Drag & drop JARs or add directories in Options > Settings.");
        } else {
            dragDropLabel.setText("Drag & drop JARs or configure game directories in Settings.");
        }
    }

    public static void refreshGameList() {
        System.out.println("Main.refreshGameList() called."); // For logging
        loadGamesFromConfiguredDirectories();
        // Potentially update other UI elements if necessary
    }
}