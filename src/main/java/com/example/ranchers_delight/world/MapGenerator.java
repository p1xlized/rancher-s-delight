package com.example.ranchers_delight.world;

import java.util.Random;

public class MapGenerator {
    private final int width;
    private final int height;
    private final Random random = new Random();

    public MapGenerator(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public String[][] generate() {
        String[][] grid = new String[width][height];

        // 1. Initialize everything as Green (Grass)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[x][y] = "greenTile";
            }
        }

        // 2. Spawn Island Seeds
        // For 25x25, 5-8 islands usually create a nice "continent" look
        int numberOfIslands = 6;
        for (int i = 0; i < numberOfIslands; i++) {
            int startX = random.nextInt(width);
            int startY = random.nextInt(height);

            // Start growth with a high chance (0.8) and slow decay (0.08)
            growIsland(grid, startX, startY, 0.8);
        }

        return grid;
    }

    private void growIsland(String[][] grid, int x, int y, double chance) {
        // Stop if out of bounds, or if the random roll is higher than current chance
        if (x < 0 || x >= width || y < 0 || y >= height || random.nextDouble() > chance) {
            return;
        }

        // If this tile is already brown, we still allow a small chance to pass through
        // to help islands merge, but we reduce recursion to prevent crashes.
        if (grid[x][y].equals("brownTile") && chance < 0.6) return;

        grid[x][y] = "brownTile";

        // Spread to neighbors.
        // Lower the decay (e.g., -0.08) to make the islands LARGER.
        // Higher the decay (e.g., -0.2) to make the islands SMALLER.
        double decay = 0.04;
        growIsland(grid, x + 1, y, chance - decay);
        growIsland(grid, x - 1, y, chance - decay);
        growIsland(grid, x, y - 1, chance - decay);
    }
}