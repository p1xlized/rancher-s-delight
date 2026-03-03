package com.example.ranchers_delight.factories;

import com.almasb.fxgl.entity.*;
import com.example.ranchers_delight.components.GreenTileComponent; // Import your components
import com.example.ranchers_delight.components.BrownTileComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// Use the standard Java DSL import
import static com.almasb.fxgl.dsl.FXGL.*;

public class GameFactory implements EntityFactory {

    @Spawns("greenTile")
    public Entity newGreenTile(SpawnData data) {
        return entityBuilder(data)
                .with(new GreenTileComponent())
                .build();
    }

    @Spawns("brownTile")
    public Entity newBrownTile(SpawnData data) {
        return entityBuilder(data)
                .with(new BrownTileComponent())
                .build();
    }
    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        return entityBuilder(data)
                .view(new Rectangle(25, 25, Color.BLUE))
                .zIndex(10) // Ensures player is drawn on top of the tiles
                .build();
    }
}