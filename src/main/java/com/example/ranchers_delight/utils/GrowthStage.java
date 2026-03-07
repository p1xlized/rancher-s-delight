package com.example.ranchers_delight.utils;

/*
* Growth stage enum
* */
public enum GrowthStage {
    PLANTED(-10),
    SPROUT(-5),
    SEEDLING(0),
    VEGETATIVE(5),
    MATURE(8),
    HARVEST_READY(10);

    private final int progressValue;

    GrowthStage(int progressValue) {
        this.progressValue = progressValue;
    }

    public int getProgressValue() {
        return progressValue;
    }
}