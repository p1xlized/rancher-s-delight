package com.example.ranchers_delight.entities;

import com.example.ranchers_delight.utils.GrowthStage;

public class PotatoCrop extends Crop {
    private static final int MAX_VISUAL_STAGE = 7;
    private int visualStage = 1;

    public int getVisualStage() {
        return visualStage;
    }

    public String getTexturePath() {
        return "/com/example/ranchers_delight/potato/potato_" + visualStage + ".png";
    }

    public boolean advanceGrowthTick() {
        if (!isAlive()) {
            return false;
        }

        boolean changed = false;

        if (visualStage < MAX_VISUAL_STAGE) {
            visualStage++;
            changed = true;
        }

        if (getStage() != GrowthStage.HARVEST_READY) {
            changed = advanceGrowthStage() || changed;
        }

        incrementCycle();
        return changed;
    }

    @Override
    public void processDay() {
        advanceGrowthTick();
    }
}

