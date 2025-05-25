/* package com.prakhar.j2mepcemu;

import org.recompile.freej2me.FreeJ2ME;
import org.recompile.mobile.Mobile;

import javax.swing.JFileChooser;
import java.awt.*;
import java.io.File;

import javax.swing.JFrame;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;

import java.util.List;


public class Main {
    public static void main(String[] args) {
        System.out.println("Core integrated!");

        // Set working directory for config files
        try {
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdir();
            }
            System.setProperty("user.dir", configDir.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to set working directory: " + e.getMessage());
        }

        // Create Swing JFrame
        JFrame frame = new JFrame("My J2ME Emulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(240 * 2, 320 * 2); // Default size (240x320, scaled 2x)

        // Initialize FreeJ2ME with default args (no JAR yet)
        String[] emulatorArgs = {"", "240", "320", "2"};
        FreeJ2ME emulator;
        try {
            emulator = new FreeJ2ME(emulatorArgs);
        } catch (Exception e) {
            System.err.println("Failed to initialize emulator: " + e.getMessage());
            return;
        }

        // Customize the AWT Frame
        Frame frame;
        try {
            frame = (Frame) FreeJ2ME.class.getDeclaredField("main").get(emulator);
            frame.setTitle("My J2ME Emulator");
            frame.setVisible(true); // Ensure frame is visible
        } catch (Exception e) {
            System.err.println("Failed to access emulator frame: " + e.getMessage());
            return;
        }

        // Get LCD canvas for drag-and-drop
        Canvas lcd;
        try {
            lcd = (Canvas) FreeJ2ME.class.getDeclaredField("lcd").get(emulator);
        } catch (Exception e) {
            System.err.println("Failed to access LCD canvas: " + e.getMessage());
            return;
        }

        // Enable drag-and-drop on the LCD canvas
        new DropTarget(lcd, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                // Accept only JAR files
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (files.stream().anyMatch(file -> file.getName().toLowerCase().endsWith(".jar"))) {
                            dtde.acceptDrag(DnDConstants.ACTION_COPY);
                        } else {
                            dtde.rejectDrag();
                        }
                    } catch (Exception e) {
                        dtde.rejectDrag();
                    }
                } else {
                    dtde.rejectDrag();
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {}

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}

            @Override
            public void dragExit(DropTargetEvent dte) {}

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File jarFile = files.get(0);
                        if (jarFile.getName().toLowerCase().endsWith(".jar")) {
                            // Convert to file:/// URI
                            String jarPath = "file:///" + jarFile.getAbsolutePath().replace("\\", "/");
                            System.out.println("Dropped JAR Path: " + jarPath);

                            // Stop any running game
                            org.recompile.mobile.Mobile.getPlatform().stopApp();

                            // Load and run new JAR
                            org.recompile.mobile.Mobile.getPlatform().load(jarPath);
                            org.recompile.mobile.Mobile.getPlatform().runJar();

                            // Load and run new JAR
                            if (org.recompile.mobile.Mobile.getPlatform().load(jarPath)) {
                                org.recompile.mobile.Mobile.getPlatform().runJar();
                            } else {
                                System.err.println("Failed to load JAR: " + jarPath);
                            }
                        }
                    }
                    dtde.dropComplete(true);
                } catch (Exception e) {
                    System.err.println("Drop failed: " + e.getMessage());
                    dtde.dropComplete(false);
                }
            }
        });
    }
} */

/*        // Initialize the file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("J2ME JAR files", "jar"));
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                // Verify file exists
                if (!selectedFile.exists()) {
                    System.err.println("Selected file does not exist: " + selectedFile.getAbsolutePath());
                    return;
                }

                // Convert to file:/// URI and ensure correct slashes
                String jarPath = "file:///" + selectedFile.getAbsolutePath().replace("\\", "/");
                String[] emulatorArgs = {jarPath, "240", "320", "2"};
                System.out.println("JAR Path: " + jarPath);
                FreeJ2ME emulator = new FreeJ2ME(emulatorArgs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("No file selected.");
        }
    }
}

 */

package com.prakhar.j2mepcemu;

import org.recompile.freej2me.FreeJ2ME;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Core integrated!");

        // Set working directory for config files
        try {
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdir();
            }
            System.setProperty("user.dir", configDir.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to set working directory: " + e.getMessage());
        }

        // Create games directory
        File gamesDir = new File("games");
        if (!gamesDir.exists()) {
            gamesDir.mkdir();
        }

        // Create Swing JFrame
        JFrame frame = new JFrame("J2ME Emulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Create JList for game entries
        DefaultListModel<String> gameListModel = new DefaultListModel<>();
        JList<String> gameList = new JList<>(gameListModel);
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setFont(new Font("Arial", Font.PLAIN, 16));

        // Populate JList with existing games
        File[] gameFiles = gamesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (gameFiles != null) {
            Arrays.stream(gameFiles)
                    .map(File::getName)
                    .forEach(gameListModel::addElement);
        }

        // Add double-click listener to launch games
        gameList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && !gameList.isSelectionEmpty()) {
                    String selectedGame = gameList.getSelectedValue();
                    File jarFile = new File(gamesDir, selectedGame);
                    if (jarFile.exists()) {
                        String jarPath = "file:///" + jarFile.getAbsolutePath().replace("\\", "/");
                        String[] emulatorArgs = {jarPath, "240", "320", "2"};
                        try {
                            new FreeJ2ME(emulatorArgs);
                        } catch (Exception e) {
                            System.err.println("Failed to launch game: " + e.getMessage());
                            JOptionPane.showMessageDialog(frame, "Failed to launch " + selectedGame, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        // Wrap JList in JScrollPane
        JScrollPane scrollPane = new JScrollPane(gameList);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Create drop target panel
        JPanel dropPanel = new JPanel();
        dropPanel.setBackground(Color.LIGHT_GRAY);
        dropPanel.setLayout(new BorderLayout());
        JLabel dropLabel = new JLabel("Drag and Drop J2ME JAR Files Here", SwingConstants.CENTER);
        dropLabel.setFont(new Font("Arial", Font.BOLD, 20));
        dropPanel.add(dropLabel, BorderLayout.CENTER);
        frame.add(dropPanel, BorderLayout.NORTH);

        // Enable drag-and-drop on dropPanel
        new DropTarget(dropPanel, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (files.stream().anyMatch(file -> file.getName().toLowerCase().endsWith(".jar"))) {
                            dtde.acceptDrag(DnDConstants.ACTION_COPY);
                        } else {
                            dtde.rejectDrag();
                        }
                    } catch (Exception e) {
                        dtde.rejectDrag();
                    }
                } else {
                    dtde.rejectDrag();
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {}

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}

            @Override
            public void dragExit(DropTargetEvent dte) {}

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : files) {
                        if (file.getName().toLowerCase().endsWith(".jar")) {
                            // Copy file to games directory
                            File destFile = new File(gamesDir, file.getName());
                            Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            // Add to JList if not already present
                            if (!gameListModel.contains(file.getName())) {
                                gameListModel.addElement(file.getName());
                            }
                        }
                    }
                    dtde.dropComplete(true);
                } catch (IOException e) {
                    System.err.println("Failed to copy file: " + e.getMessage());
                    dtde.dropComplete(false);
                } catch (Exception e) {
                    System.err.println("Drop failed: " + e.getMessage());
                    dtde.dropComplete(false);
                }
            }
        });

        // Display the frame
        frame.setVisible(true);
    }
}