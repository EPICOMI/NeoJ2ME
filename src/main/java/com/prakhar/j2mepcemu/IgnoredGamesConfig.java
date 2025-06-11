package com.prakhar.j2mepcemu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IgnoredGamesConfig {

    private static final String CONFIG_FILE_NAME = "ignored_games.conf";
    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".j2mepcemu");
    private static final Path CONFIG_FILE_PATH = CONFIG_DIR.resolve(CONFIG_FILE_NAME);

    // Loads all ignored game paths into a Set for efficient lookup.
    public static Set<String> loadIgnoredGames() throws IOException {
        Set<String> ignoredGames = new HashSet<>();
        if (!Files.exists(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR); // Ensure directory exists
        }
        if (Files.exists(CONFIG_FILE_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE_PATH)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        ignoredGames.add(line.trim());
                    }
                }
            }
        }
        return ignoredGames;
    }

    // Saves the entire set of ignored game paths.
    // This is less efficient if only adding/removing one, but simpler to manage.
    // For high frequency, consider read-modify-write of individual lines.
    private static void saveIgnoredGames(Set<String> ignoredGames) throws IOException {
        if (!Files.exists(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_FILE_PATH)) {
            for (String gamePath : ignoredGames) {
                writer.write(gamePath);
                writer.newLine();
            }
        }
    }

    // Adds a game path to the ignored list.
    public static void addIgnoredGame(String gamePath) throws IOException {
        Set<String> ignoredGames = loadIgnoredGames();
        if (ignoredGames.add(gamePath)) { // add returns true if the element was not already present
            saveIgnoredGames(ignoredGames);
        }
    }

    // Removes a game path from the ignored list (unhides it).
    public static void removeIgnoredGame(String gamePath) throws IOException {
        Set<String> ignoredGames = loadIgnoredGames();
        if (ignoredGames.remove(gamePath)) { // remove returns true if the element was present
            saveIgnoredGames(ignoredGames);
        }
    }

    // Checks if a game is currently ignored.
    // This can be used directly if loading the set on each check is acceptable,
    // or the main app can load the set once and pass it around/query it.
    public static boolean isGameIgnored(String gamePath) throws IOException {
        Set<String> ignoredGames = loadIgnoredGames();
        return ignoredGames.contains(gamePath);
    }

    // Convenience method if the main application already has the set loaded.
    public static boolean isGameIgnored(String gamePath, Set<String> ignoredGamesSet) {
        if (ignoredGamesSet == null) return false;
        return ignoredGamesSet.contains(gamePath);
    }
}
