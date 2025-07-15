package com.prakhar.j2mepcemu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

public class SavesManager {

    private static final String SAVES_DIR = "freej2me_system";

    public static File getSavesDirectory() {
        return new File(SAVES_DIR);
    }

    public static void exportSaveChanges(File source, File destination) throws IOException {
        if (!source.exists()) {
            throw new IOException("Source folder does not exist: " + source.getAbsolutePath());
        }
        if (destination.exists()) {
            throw new IOException("Destination file already exists: " + destination.getAbsolutePath());
        }
        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void importSaveChanges(File source, File destination) throws IOException {
        if (!source.exists()) {
            throw new IOException("Source file does not exist: " + source.getAbsolutePath());
        }
        if (destination.exists()) {
            // Clear existing directory before importing
            Files.walk(destination.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
