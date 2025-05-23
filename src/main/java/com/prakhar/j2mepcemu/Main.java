package com.prakhar.j2mepcemu;

import org.recompile.freej2me.FreeJ2ME;

import javax.swing.JFileChooser;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.out.println("Core integrated!");

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
                // Convert to file:/// URI and replace backslashes
                String jarPath = selectedFile.toURI().toString().replace("\\", "/");
                String[] emulatorArgs = {jarPath, "240", "320", "2"};
                FreeJ2ME emulator = new FreeJ2ME(emulatorArgs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("No file selected.");
        }
    }
}