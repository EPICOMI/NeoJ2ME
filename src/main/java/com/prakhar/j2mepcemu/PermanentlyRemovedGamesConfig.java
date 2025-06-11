package com.prakhar.j2mepcemu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PermanentlyRemovedGamesConfig {

    private static final String CONFIG_FILE_NAME = "permanently_removed_games.conf";
    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".j2mepcemu");
    private static final Path CONFIG_FILE_PATH = CONFIG_DIR.resolve(CONFIG_FILE_NAME);

    // Loads all permanently removed game paths into a Set.
    public static Set<String> loadPermanentlyRemovedGames() throws IOException {
        Set<String> removedGames = new HashSet<>();
        if (!Files.exists(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }
        if (Files.exists(CONFIG_FILE_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE_PATH)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        removedGames.add(line.trim());
                    }
                }
            }
        }
        return removedGames;
    }

    // Saves the entire set of permanently removed game paths.
    private static void savePermanentlyRemovedGames(Set<String> removedGames) throws IOException {
        if (!Files.exists(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_FILE_PATH)) {
            for (String gamePath : removedGames) {
                writer.write(gamePath);
                writer.newLine();
            }
        }
    }

    // Adds a game path to the permanently removed list.
    public static void addPermanentlyRemovedGame(String gamePath) throws IOException {
        Set<String> removedGames = loadPermanentlyRemovedGames();
        if (removedGames.add(gamePath)) {
            savePermanentlyRemovedGames(removedGames);
        }
    }

    // Removes a game path from the permanently removed list.
    public static void removePermanentlyRemovedGame(String gamePath) throws IOException {
        Set<String> removedGames = loadPermanentlyRemovedGames();
        if (removedGames.remove(gamePath)) {
            savePermanentlyRemovedGames(removedGames);
        }
    }

    // Removes all game paths from the permanently_removed_games.conf that are under a given directory path.
    public static void removePermanentlyRemovedGamesUnderDirectory(String dirPath) throws IOException {
        Set<String> removedGames = loadPermanentlyRemovedGames();
        // Normalize dirPath to ensure it ends with a separator for correct startsWith matching of directory contents
        String normalizedDirPath = Paths.get(dirPath).normalize().toString();

        Set<String> toRemove = removedGames.stream()
                .filter(gamePath -> Paths.get(gamePath).normalize().startsWith(normalizedDirPath))
                .collect(Collectors.toSet());

        if (!toRemove.isEmpty()) {
            removedGames.removeAll(toRemove);
            savePermanentlyRemovedGames(removedGames);
            // System.out.println("Cleared " + toRemove.size() + " permanently removed entries under: " + dirPath);
        }
    }

    // Convenience method to check against an already loaded set.
    public static boolean isGamePermanentlyRemoved(String gamePath, Set<String> removedGamesSet) {
        if (removedGamesSet == null) return false;
        return removedGamesSet.contains(gamePath);
    }
}
