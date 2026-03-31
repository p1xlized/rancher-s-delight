package com.example.ranchers_delight.world;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Random;

public class MapGenerator {
    private final int width;
    private final int height;
    private final Random random = new Random();
    private final String SAVE_DIR = "saves"; // Folder name
    private static final double TREE_CHANCE = 0.30;

    public MapGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        ensureSaveDirectoryExists();
    }

    private void ensureSaveDirectoryExists() {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
        } catch (IOException e) {
            System.err.println("Could not create save directory: " + e.getMessage());
        }
    }

    public String[][] generate() {
        String[][] grid = new String[width][height];
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double maxDist = Math.min(width, height) / 2.5;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double dx = x - centerX;
                double dy = y - centerY;
                double dist = Math.sqrt(dx * dx + dy * dy);

                double noise = Math.sin(x * 0.15) * 4.0 + Math.cos(y * 0.15) * 4.0;
                double noisyDist = dist + noise;

                if (noisyDist > maxDist + 5) {
                    grid[x][y] = "waterTile";
                } else if (noisyDist < maxDist * 0.35) {
                    grid[x][y] = "brownTile";
                } else {
                    // Green biome: place a tree randomly, otherwise keep grass.
                    grid[x][y] = random.nextDouble() < TREE_CHANCE ? "treeTile" : "greenTile";
                }
            }
        }
        return grid;
    }

    public void saveMap(String[][] grid, String fileName) {
        Path path = Paths.get(SAVE_DIR, fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(width + "," + height + "\n");
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    writer.write(grid[x][y] + (x == width - 1 ? "" : ","));
                }
                writer.write("\n");
            }
            System.out.println("Map saved to: " + path.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[][] loadMap(String fileName) {
        Path path = Paths.get(SAVE_DIR, fileName);
        if (!Files.exists(path)) {
            System.out.println("No save found at " + fileName + ". Generating new map...");
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(path);
            String[] dims = lines.get(0).split(",");
            int w = Integer.parseInt(dims[0]);
            int h = Integer.parseInt(dims[1]);
            String[][] grid = new String[w][h];

            for (int i = 1; i < lines.size(); i++) {
                String[] row = lines.get(i).split(",");
                for (int j = 0; j < row.length; j++) {
                    grid[j][i - 1] = row[j];
                }
            }
            System.out.println("Map loaded successfully from: " + path.toAbsolutePath());
            return grid;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}