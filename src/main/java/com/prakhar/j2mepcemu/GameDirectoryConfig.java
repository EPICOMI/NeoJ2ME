package com.prakhar.j2mepcemu;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
// No need for Collectors here

public class GameDirectoryConfig {

    private static final String CONFIG_FILE_NAME = "game_dirs.conf";
    // Ensure CONFIG_DIR is initialized before CONFIG_FILE_PATH if it's static
    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".j2mepcemu");
    private static final Path CONFIG_FILE_PATH = CONFIG_DIR.resolve(CONFIG_FILE_NAME);

    public static void saveDirectories(List<String> directories) throws IOException { // Added throws IOException
        if (!Files.exists(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR); // This can also throw IOException
        }
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_FILE_PATH)) {
            for (String dir : directories) {
                writer.write(dir);
                writer.newLine();
            }
        }
        // Removed catch block, IOException will propagate
    }

    public static List<String> loadDirectories() throws IOException { // Added throws IOException
        List<String> directories = new ArrayList<>();
        if (Files.exists(CONFIG_FILE_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE_PATH)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        directories.add(line.trim());
                    }
                }
            }
            // Removed catch block, IOException will propagate
        }
        return directories;
    }
}
