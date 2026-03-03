package com.example.ranchers_delight.components;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
public class GreenTileComponent extends TileComponent {
    public GreenTileComponent() {
        super(false);
    }

    @Override
    public void onAdded() {
        // Set the visual look when the component is attached to an entity
        entity.getViewComponent().addChild(new Rectangle(40, 40, Color.GREEN));
    }
}