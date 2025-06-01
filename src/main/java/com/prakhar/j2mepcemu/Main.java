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
import java.util.Arrays;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
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

        int filesProcessedCount = 0; // Renamed for clarity, as it counts both new and updated files
        List<String> processedFileNamesThisOperation = new ArrayList<>(); // Tracks files processed in this call

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                File destFile = new File(gamesDir, file.getName());
                try {
                    Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    // Check if the file (by name) is already in the list model
                    if (!listModel.contains(destFile.getName())) {
                        listModel.addElement(destFile.getName());
                        gameFiles.add(destFile); // Add the File object pointing to the copy in "games"
                        processedFileNamesThisOperation.add(destFile.getName());
                        filesProcessedCount++;
                    } else {
                        // File already existed in listModel, its content in "games" dir is now updated.
                        // We count it as processed (updated) if it hasn't been counted in this operation yet.
                        if (!processedFileNamesThisOperation.contains(destFile.getName())) {
                            // We need to ensure we update the File object in gameFiles if it changed,
                            // however, since listModel stores names and gameFiles stores File objects,
                            // and we retrieve by index, if a file is REPLACED, the existing File object
                            // in gameFiles still points to the correct path. So, no specific update needed here for gameFiles.
                            processedFileNamesThisOperation.add(destFile.getName());
                            filesProcessedCount++;
                        }
                    }
                } catch (IOException ex) {
                    System.err.println("Failed to copy file: " + file.getName() + " - " + ex.getMessage());
                    feedbackLabel.setText("Error copying " + file.getName());
                    // Potentially return or decide if one error stops all, for now, it continues
                }
            }
        }

        if (filesProcessedCount > 0) {
            feedbackLabel.setText("Processed " + filesProcessedCount + " JAR file(s) into 'games' directory.");
        } else {
            feedbackLabel.setText("No new or updated JAR files were processed. Drag JAR files here.");
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