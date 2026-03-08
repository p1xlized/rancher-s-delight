package com.example.ranchers_delight.entities;

import com.example.ranchers_delight.utils.GrowthStage;

/*
* Crop is an abstract class, that the specific types of crops (like potatoes, cucomber, etc.) will inherit from
* It has growth stage from [-10, 10]
* -10 - planted
* 0 - grown
* 10 - ready for harvest
* isAlive - boolean representing the stage of the crop
* */
public abstract class Crop {
    private GrowthStage stage;
    private boolean isAlive;

    private int cycles;
    public Crop() {
        this.stage = GrowthStage.PLANTED; // Starts at -10
        this.isAlive = true;
        this.cycles = 0;
    }

    // Common logic for all crops
    public void grow() {
        if (!isAlive) {
            System.out.println("This crop is dead and cannot grow.");
            return;
        }

        // Logic to move to the next enum ordinal
        GrowthStage[] stages = GrowthStage.values();
        int currentIndex = stage.ordinal();

        if (currentIndex < stages.length - 1) {
            this.stage = stages[currentIndex + 1];
        }
    }

    public void die() {
        this.isAlive = false;
        System.out.println("The crop has withered.");
    }

    // Getters
    public GrowthStage getStage() { return stage; }
    public boolean isAlive() { return isAlive; }

    public int getCycle() { return cycles; }
    // Abstract method: How each crop reacts to the environment
    public abstract void processDay();
}