package com.example.ranchers_delight.components;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BrownTileComponent extends TileComponent {
    public BrownTileComponent() {
        super(true);
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(new Rectangle(40, 40, Color.SADDLEBROWN));
    }
}