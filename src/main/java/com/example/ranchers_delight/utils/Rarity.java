package com.example.ranchers_delight.utils;

public enum Rarity {
    COMMON(1, "Common"),
    UNCOMMON(2, "Uncommon"),
    RARE(3, "Rare"),
    EPIC(4, "Epic"),
    LEGENDARY(5, "Legendary");

    private final int stars;
    private final String label;

    // Constructor name must match the Enum name "Rarity"
    Rarity(int stars, String label) {
        this.stars = stars;
        this.label = label;
    }

    public int getStars() {
        return stars;
    }

    public String getLabel() {
        return label;
    }
}