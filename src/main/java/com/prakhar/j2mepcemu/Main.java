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
                    String removedGameName = listModel.getElementAt(selectedIndex); // Get name for feedback
                    gameFiles.remove(selectedIndex);
                    listModel.remove(selectedIndex);
                    // Update dragDropLabel with feedback
                    dragDropLabel.setText("'" + removedGameName + "' removed from list. May reappear on refresh.");
                } else {
                    System.err.println("Error: Index mismatch when trying to remove game from list.");
                    dragDropLabel.setText("Error removing game. Index mismatch."); // Optional: feedback for error
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
        File gamesDir = new File("games");
        if (!gamesDir.exists()) {
            if (!gamesDir.mkdirs()) {
                feedbackLabel.setText("Error: Cannot create 'games' directory.");
                System.err.println("Error: Cannot create 'games' directory.");
                return;
            }
        }

        int filesCopiedCount = 0;

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                File destFile = new File(gamesDir, file.getName());
                try {
                    Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    filesCopiedCount++;
                    // The direct adding to listModel and gameFiles here is now redundant
                    // as refreshGameList() will rescan everything.
                    // We just need to make sure the file is copied.
                } catch (IOException ex) {
                    System.err.println("Failed to copy file: " + file.getName() + " - " + ex.getMessage());
                    feedbackLabel.setText("Error copying " + file.getName());
                }
            }
        }

        if (filesCopiedCount > 0) {
            feedbackLabel.setText("Copied " + filesCopiedCount + " JAR file(s) to 'games' directory.");
            refreshGameList(); // Refresh the list to show newly dropped games
        } else {
            feedbackLabel.setText("No new JAR files were processed. Drag JAR files here.");
            // Optionally call refreshGameList() even if no files copied, if other state might need update
            // but typically only needed if changes occurred.
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

    private static void scanDirectoryForGamesInternal(File directory, Set<String> scannedGamePaths) {
        if (!directory.exists() || !directory.isDirectory()) {
            // System.err.println("Cannot scan directory: " + directory.getAbsolutePath() + " - does not exist or not a directory.");
            return;
        }

        File[] files = directory.listFiles((d, name) -> name.toLowerCase().endsWith(".jar"));
        if (files != null) {
            for (File file : files) {
                if (scannedGamePaths.contains(file.getAbsolutePath())) {
                    continue; // Already processed this file, skip
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
                scannedGamePaths.add(file.getAbsolutePath()); // Mark as processed
            }
        }

        File[] subDirs = directory.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                scanDirectoryForGamesInternal(subDir, scannedGamePaths); // Recursive call
            }
        }
    }

    private static void loadGamesFromConfiguredDirectories() {
        listModel.clear();
        gameFiles.clear();
        Set<String> scannedGamePaths = new HashSet<>(); // To prevent duplicates if "games" is also a configured path

        // 1. Always scan the local "games" directory
        File localGamesDir = new File("games");
        if (!localGamesDir.exists()) {
            localGamesDir.mkdirs(); // Ensure it exists for drag & drop consistency
        }
        // System.out.println("Scanning default 'games' directory: " + localGamesDir.getAbsolutePath());
        scanDirectoryForGamesInternal(localGamesDir, scannedGamePaths);

        // 2. Scan all user-configured directories
        List<String> configuredPaths = new ArrayList<>(); // Initialize to empty list
        try {
            configuredPaths = GameDirectoryConfig.loadDirectories();
        } catch (java.io.IOException e) {
            System.err.println("Error loading game directory configurations: " + e.getMessage());
            // Optionally, provide feedback to the user via the UI, though dragDropLabel is mostly for game status
            // dragDropLabel.setText("Error loading directory config: " + e.getMessage());
            // For now, logging to System.err is consistent with other parts of the initial load sequence.
        }

        // System.out.println("Scanning " + configuredPaths.size() + " configured directories.");
        for (String path : configuredPaths) {
            File dir = new File(path);
            // System.out.println("Scanning configured directory: " + dir.getAbsolutePath());
            scanDirectoryForGamesInternal(dir, scannedGamePaths);
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