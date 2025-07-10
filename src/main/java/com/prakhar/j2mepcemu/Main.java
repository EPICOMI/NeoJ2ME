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
import com.prakhar.j2mepcemu.IgnoredGamesConfig;
import com.prakhar.j2mepcemu.PermanentlyRemovedGamesConfig; // New
import java.io.IOException; // For handling exceptions from IgnoredGamesConfig
import org.recompile.freej2me.FreeJ2ME;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
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

        // JMenuItem removeItem = new JMenuItem("Remove"); // OLD Line
        JMenuItem hideItem = new JMenuItem("Hide"); // NEW Line - Renamed

        // The ActionListener for 'hideItem' (previously 'removeItem') should be the one that
        // adds the game to IgnoredGamesConfig and removes it from the list.
        // Ensure its logic is as implemented in the previous plan for persistent hiding.
        hideItem.addActionListener(e -> {
            int selectedIndex = gameList.getSelectedIndex();
            if (selectedIndex != -1) {
                if (selectedIndex < gameFiles.size() && selectedIndex < listModel.getSize()) {
                    File gameFileToHide = gameFiles.get(selectedIndex); // Get the File object
                    String gamePath = gameFileToHide.getAbsolutePath();
                    String gameDisplayName = listModel.getElementAt(selectedIndex);

                    try {
                        // First, remove from permanently removed list, if it was there
                        PermanentlyRemovedGamesConfig.removePermanentlyRemovedGame(gamePath);

                        // Then, add to ignored (hidden) list
                        IgnoredGamesConfig.addIgnoredGame(gamePath);

                        // Remove from current view
                        gameFiles.remove(selectedIndex);
                        listModel.remove(selectedIndex);

                        // Update feedback message to use "hidden"
                        dragDropLabel.setText("'" + gameDisplayName + "' hidden. Manage in File > Hidden Games or drag & drop to unhide.");

                    } catch (IOException ex) {
                        System.err.println("Error trying to hide game '" + gamePath + "': " + ex.getMessage());
                        JOptionPane.showMessageDialog(frame,
                                "Error hiding game: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    System.err.println("Error: Index mismatch when trying to hide game from list.");
                    dragDropLabel.setText("Error hiding game. Index mismatch.");
                }
            }
        });
        // gameListContextMenu.add(removeItem); // OLD Line
        gameListContextMenu.add(hideItem); // NEW Line - Add the renamed item

        // New "Remove" (temporary) menu item
        JMenuItem temporaryRemoveItem = new JMenuItem("Remove");
        temporaryRemoveItem.addActionListener(e -> {
            int selectedIndex = gameList.getSelectedIndex();
            if (selectedIndex != -1) {
                if (selectedIndex < gameFiles.size() && selectedIndex < listModel.getSize()) {
                    File gameFileToRemove = gameFiles.get(selectedIndex); // Get the File object
                    String gamePath = gameFileToRemove.getAbsolutePath();
                    String gameDisplayName = listModel.getElementAt(selectedIndex);

                    try {
                        // First, remove from ignored (hidden) list, if it was there
                        IgnoredGamesConfig.removeIgnoredGame(gamePath);

                        // Then, add to permanently removed list
                        PermanentlyRemovedGamesConfig.addPermanentlyRemovedGame(gamePath);

                        // Remove from current view
                        gameFiles.remove(selectedIndex);
                        listModel.remove(selectedIndex);

                        // Update dragDropLabel feedback
                        dragDropLabel.setText("'" + gameDisplayName + "' removed. Re-import directory or drag & drop to see it again.");

                    } catch (IOException ex) {
                        System.err.println("Error trying to permanently remove game '" + gamePath + "': " + ex.getMessage());
                        JOptionPane.showMessageDialog(frame,
                                "Error removing game: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    System.err.println("Error: Index mismatch when trying to permanently remove game.");
                    dragDropLabel.setText("Error removing game. Index mismatch.");
                }
            }
        });
        gameListContextMenu.add(temporaryRemoveItem); // Add the new item

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
        File gamesDir = new File("games");
        if (!gamesDir.exists()) {
            if (!gamesDir.mkdirs()) {
                feedbackLabel.setText("Error: Cannot create 'games' directory.");
                System.err.println("Error: Cannot create 'games' directory.");
                return;
            }
        }

        int filesCopiedCount = 0;
        int filesRestoredCount = 0; // Changed from unhiddenCount for more general term

        // No need to load ignored/permanently_removed sets here anymore,
        // as the remove methods in config classes handle non-existence gracefully.

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                File destFile = new File(gamesDir, file.getName());
                String destPath = destFile.getAbsolutePath();

                boolean wasIgnored = false; // To track if it was in any list
                boolean wasPermanentlyRemoved = false;

                try {
                    // Attempt to remove from both lists.
                    // These methods load the respective lists, modify, and save if changed.
                    // They don't throw error if item not found.

                    // Check if it was ignored (for feedback purposes)
                    // We need to load the sets to check *before* removing.
                    Set<String> initialIgnored = IgnoredGamesConfig.loadIgnoredGames();
                    if (initialIgnored.contains(destPath)) {
                        wasIgnored = true;
                    }
                    IgnoredGamesConfig.removeIgnoredGame(destPath); // Remove from hidden list

                    Set<String> initialPermRemoved = PermanentlyRemovedGamesConfig.loadPermanentlyRemovedGames();
                    if (initialPermRemoved.contains(destPath)) {
                        wasPermanentlyRemoved = true;
                    }
                    PermanentlyRemovedGamesConfig.removePermanentlyRemovedGame(destPath); // Remove from permanently removed list

                    if (wasIgnored || wasPermanentlyRemoved) {
                        filesRestoredCount++;
                    }

                    Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    filesCopiedCount++;

                } catch (IOException ex) {
                    System.err.println("Error processing dropped file '" + file.getName() + "': " + ex.getMessage());
                    JOptionPane.showMessageDialog(frame,
                            "Error processing dropped file '" + file.getName() + "':\n" + ex.getMessage(),
                            "File Drop Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        if (filesCopiedCount > 0 || filesRestoredCount > 0) {
            StringBuilder feedbackMsg = new StringBuilder();
            if (filesCopiedCount > 0) { // It will always be > 0 if filesRestoredCount > 0 as well
                feedbackMsg.append("Copied/updated ").append(filesCopiedCount).append(" JAR(s). ");
            }
            if (filesRestoredCount > 0) {
                feedbackMsg.append(filesRestoredCount).append(" previously hidden/removed game(s) restored. ");
            }
            feedbackLabel.setText(feedbackMsg.toString().trim() + " List refreshed.");
            refreshGameList();
        } else {
            if (!files.isEmpty()) {
                 feedbackLabel.setText("No new JAR files processed. Drag JAR files here.");
            } else {
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

    private static void scanDirectoryForGamesInternal(File directory,
                                                      Set<String> scannedGamePaths,
                                                      Set<String> ignoredGamesSet,
                                                      Set<String> permanentlyRemovedGamesSet) { // Added permanentlyRemovedGamesSet
        if (!directory.exists() || !directory.isDirectory()) {
            // System.err.println("Cannot scan directory: " + directory.getAbsolutePath() + " - does not exist or not a directory.");
            return;
        }

        File[] files = directory.listFiles((d, name) -> name.toLowerCase().endsWith(".jar"));
        if (files != null) {
            for (File file : files) {
                String absolutePath = file.getAbsolutePath();

                // Skip if already processed OR if it's in the ignored list OR in the permanently removed list
                if (scannedGamePaths.contains(absolutePath) ||
                    IgnoredGamesConfig.isGameIgnored(absolutePath, ignoredGamesSet) ||
                    PermanentlyRemovedGamesConfig.isGamePermanentlyRemoved(absolutePath, permanentlyRemovedGamesSet)) { // New check
                    // System.out.println("Skipping (scanned, ignored, or permanently removed): " + absolutePath);
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
                // Pass all sets in recursive call
                scanDirectoryForGamesInternal(subDir, scannedGamePaths, ignoredGamesSet, permanentlyRemovedGamesSet);
            }
        }
    }

    private static void loadGamesFromConfiguredDirectories() {
        listModel.clear();
        gameFiles.clear();
        Set<String> scannedGamePaths = new HashSet<>();
        Set<String> ignoredGamesSet = new HashSet<>();
        Set<String> permanentlyRemovedGamesSet = new HashSet<>(); // New: For permanently removed games

        try {
            ignoredGamesSet = IgnoredGamesConfig.loadIgnoredGames();
        } catch (IOException e) {
            System.err.println("Error loading ignored games configuration: " + e.getMessage());
        }

        try { // New try-catch for permanently removed games
            permanentlyRemovedGamesSet = PermanentlyRemovedGamesConfig.loadPermanentlyRemovedGames();
            // System.out.println("Loaded " + permanentlyRemovedGamesSet.size() + " permanently removed game path(s).");
        } catch (IOException e) {
            System.err.println("Error loading permanently removed games configuration: " + e.getMessage());
        }

        // 1. Always scan the local "games" directory
        File localGamesDir = new File("games");
        if (!localGamesDir.exists()) {
            localGamesDir.mkdirs();
        }
        // Pass both sets to the scanning method
        scanDirectoryForGamesInternal(localGamesDir, scannedGamePaths, ignoredGamesSet, permanentlyRemovedGamesSet);

        // 2. Scan all user-configured directories
        List<String> configuredPaths = new ArrayList<>();
        try {
            configuredPaths = GameDirectoryConfig.loadDirectories();
        } catch (java.io.IOException e) {
            System.err.println("Error loading game directory configurations: " + e.getMessage());
        }

        for (String path : configuredPaths) {
            File dir = new File(path);
            // Pass both sets to the scanning method
            scanDirectoryForGamesInternal(dir, scannedGamePaths, ignoredGamesSet, permanentlyRemovedGamesSet);
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