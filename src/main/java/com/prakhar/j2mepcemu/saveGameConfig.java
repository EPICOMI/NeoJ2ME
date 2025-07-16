package com.prakhar.j2mepcemu;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.zip.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;


public class saveGameConfig {
    public void exportGameSave(JList<String> gameList, JFrame frame, DefaultListModel<String> listModel, ArrayList<File> gameFiles) {
        int selectedIndex = gameList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(frame, "No game selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File selectedGameFile = gameFiles.get(selectedIndex);
        String midletName = getMidletName(selectedGameFile);
        if (midletName == null) {
            JOptionPane.showMessageDialog(frame, "Cannot determine MIDlet-Name for " + selectedGameFile.getName(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File rmsDir = new File("rms");
        File saveDir = new File(rmsDir, midletName);
        if (!saveDir.exists() || !saveDir.isDirectory()) {
            JOptionPane.showMessageDialog(frame, "No save data found for " + midletName, "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(midletName + "_save.zip"));
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File zipFile = fileChooser.getSelectedFile();
            try {
                zipDirectory(saveDir, zipFile);
                JOptionPane.showMessageDialog(frame, "Save data exported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error exporting save data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void importGameSave(JList<String> gameList, JFrame frame, DefaultListModel<String> listModel, ArrayList<File> gameFiles) {
        int selectedIndex = gameList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(frame, "No game selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File selectedGameFile = gameFiles.get(selectedIndex);
        String midletName = getMidletName(selectedGameFile);
        if (midletName == null) {
            JOptionPane.showMessageDialog(frame, "Cannot determine MIDlet-Name for " + selectedGameFile.getName(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Zip files", "zip"));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File zipFile = fileChooser.getSelectedFile();
            File rmsDir = new File("rms");
            File saveDir = new File(rmsDir, midletName);
            if (saveDir.exists()) {
                int confirm = JOptionPane.showConfirmDialog(frame,
                        "Existing save data for " + midletName + " will be overwritten. Continue?",
                        "Confirm Import", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            try {
                unzipToDirectory(zipFile, saveDir);
                JOptionPane.showMessageDialog(frame, "Save data imported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error importing save data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getMidletName(File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
                Attributes mainAttributes = manifest.getMainAttributes();
                return mainAttributes.getValue("MIDlet-Name");
            }
        } catch (IOException e) {
            System.err.println("Error reading manifest from " + jarFile.getName() + ": " + e.getMessage());
        }
        return null;
    }

    private void zipDirectory(File directory, File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Path sourcePath = directory.toPath();
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String entryName = sourcePath.relativize(file).toString();
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(sourcePath)) {
                        String entryName = sourcePath.relativize(dir).toString() + "/";
                        zos.putNextEntry(new ZipEntry(entryName));
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private void unzipToDirectory(File zipFile, File destDir) throws IOException {
        destDir.mkdirs();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File entryFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    entryFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
}