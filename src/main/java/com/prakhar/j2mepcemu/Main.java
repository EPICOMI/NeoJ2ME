package com.prakhar.j2mepcemu;

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
import java.util.List;
import org.recompile.freej2me.FreeJ2ME;

public class Main {
    private static JFrame frame;
    private static JList<String> gameList;
    private static DefaultListModel<String> listModel;
    private static JLabel dragDropLabel;
    private static ArrayList<File> gameFiles;
    private static FreeJ2ME currentEmulator; // Track current instance

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
        frame.add(dragDropLabel, BorderLayout.SOUTH);

        gameFiles = new ArrayList<>();
        setupDragAndDrop();
        setupGameListListener();

        loadGamesFromDirectory("games");

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

                    File gamesDir = new File("games");
                    // Ensure "games" directory exists (loadGamesFromDirectory also does this, added for robustness here)
                    if (!gamesDir.exists()) {
                        if (!gamesDir.mkdirs()) {
                            dragDropLabel.setText("Error: Cannot create 'games' directory.");
                            System.err.println("Error: Cannot create 'games' directory.");
                            dtde.dropComplete(false);
                            return;
                        }
                    }

                    int filesAddedCount = 0;
                    List<String> addedFileNamesThisDrop = new ArrayList<>();

                    for (File file : droppedFiles) {
                        if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                            File destFile = new File(gamesDir, file.getName());
                            try {
                                // Copy the file to the "games" directory, replacing if it exists
                                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                // Add the *copied file's* name to listModel and the File object to gameFiles
                                // Only add if it's not already in the list model from this current drop session or from load.
                                if (!listModel.contains(destFile.getName())) {
                                    listModel.addElement(destFile.getName());
                                    gameFiles.add(destFile); // Add the File object pointing to the copy in "games"
                                    addedFileNamesThisDrop.add(destFile.getName());
                                    filesAddedCount++;
                                } else {
                                    // File already existed in list, its content in "games" dir is now updated.
                                    // We might need to update the File object in gameFiles if the instance matters,
                                    // but since launchGame uses getAbsolutePath(), and loadGamesFromDirectory also
                                    // loads based on path, this should be fine.
                                    // We count it as processed if it's a JAR.
                                    if (!addedFileNamesThisDrop.contains(destFile.getName())) { // Ensure it was a JAR we processed
                                        filesAddedCount++; // Count as processed (updated)
                                    }
                                }
                            } catch (IOException ex) {
                                System.err.println("Failed to copy file: " + file.getName() + " - " + ex.getMessage());
                                // Optionally, inform the user via dragDropLabel or a dialog for this specific file
                            }
                        }
                    }

                    if (filesAddedCount > 0) {
                        dragDropLabel.setText("Processed " + filesAddedCount + " JAR file(s) into 'games' directory.");
                    } else {
                        dragDropLabel.setText("No new JAR files were added. Drag JAR files here.");
                    }
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

    private static void loadGamesFromDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jar"));
        if (files != null) {
            for (File file : files) {
                listModel.addElement(file.getName());
                gameFiles.add(file);
            }
        }
    }
}