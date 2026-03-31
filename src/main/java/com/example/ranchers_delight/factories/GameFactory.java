package com.example.ranchers_delight.factories;

import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.entity.components.CollidableComponent;

import com.example.ranchers_delight.components.GreenTileComponent;
import com.example.ranchers_delight.components.BrownTileComponent;
import com.example.ranchers_delight.components.Item;
import com.example.ranchers_delight.entities.Player;
import com.example.ranchers_delight.utils.EntityType;
import com.example.ranchers_delight.utils.Rarity;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import java.io.InputStream;
import java.util.Random;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameFactory implements EntityFactory {

    private final Random random = new Random();
    private final int TILE_SIZE = 40;

    @Spawns("greenTile")
    public Entity newGreenTile(SpawnData data) {
        Rectangle grass = new Rectangle(TILE_SIZE, TILE_SIZE, Color.LIMEGREEN);
        grass.setStroke(Color.FORESTGREEN);
        grass.setStrokeWidth(0.5);

        Entity entity = entityBuilder(data)
                .type(EntityType.TILE) // Assign type
                .view(grass)
                .with(new GreenTileComponent())
                .zIndex(0)
                .build();

        if (random.nextDouble() < 0.2) {
            // We use spawn("tree", ...) instead of spawnTree() to ensure
            // the factory @Spawns method is used correctly
            spawn("tree", data.getX(), data.getY());
        }

        return entity;
    }



    @Spawns("brownTile")
    public Entity newBrownTile(SpawnData data) {
        Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE, Color.SADDLEBROWN);

        return entityBuilder(data)
                .type(EntityType.TILE)
                .view(rect)
                .with(new BrownTileComponent())
                .zIndex(0)
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        String playerName = readStringOrDefault(data, "name", "Rancher");
        int level = readIntOrDefault(data, "level", 1);

        Player playerModel = new Player(playerName, level);
        // Starter kit so the hotbar and inventory are populated on first load.
        playerModel.addItemToInventory(new Item("Seeds", Rarity.COMMON));
        playerModel.addItemToInventory(new Item("Hoe", Rarity.UNCOMMON));

        Entity entity = entityBuilder(data)
                .type(EntityType.PLAYER)
                .viewWithBBox(new Rectangle(25, 25, Color.BLUE))
                .with(new CollidableComponent(true))
                .zIndex(10)
                .build();

        entity.setProperty("playerModel", playerModel);
        return entity;
    }

    private String readStringOrDefault(SpawnData data, String key, String defaultValue) {
        try {
            String value = data.get(key);
            return (value == null || value.isBlank()) ? defaultValue : value;
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }

    private int readIntOrDefault(SpawnData data, String key, int defaultValue) {
        try {
            Integer value = data.get(key);
            return value == null ? defaultValue : value;
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }

    @Spawns("tree")
    public Entity newTree(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.TREE) // CRITICAL: Assign the type
                .viewWithBBox(createTreeView())
                .with(new CollidableComponent(true))
                .zIndex(20)
                .build();
    }

    private Node createTreeView() {
        InputStream stream = getClass().getResourceAsStream("/com/example/ranchers_delight/objects/tree.png");

        if (stream == null) {
            return new Rectangle(40, 40, Color.DARKGREEN);
        }

        Image image = new Image(stream);
        ImageView treeView = new ImageView(image);
        treeView.setFitWidth(40);
        treeView.setFitHeight(40);
        treeView.setPreserveRatio(false);
        return treeView;
    }

    @Spawns("house")
    public Entity newHouse(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.HOUSE) // CRITICAL: Assign the type
                .viewWithBBox(createHouseView())
                .with(new CollidableComponent(true))
                .zIndex(30)
                .build();
    }

    private Node createHouseView() {
        InputStream stream = getClass().getResourceAsStream("/com/example/ranchers_delight/objects/house.png");

        if (stream == null) {
            // Fallback keeps gameplay running if the asset path is wrong at runtime.
            return new Rectangle(120, 100, Color.MAROON);
        }

        Image image = new Image(stream);
        ImageView houseView = new ImageView(image);
        houseView.setFitWidth(120);
        houseView.setFitHeight(100);
        houseView.setPreserveRatio(false);
        return houseView;
    }

    @Spawns("box")
    public Entity newBox(SpawnData data) {
        return entityBuilder(data)
                .viewWithBBox(new Rectangle(30, 30, Color.BURLYWOOD))
                .with(new CollidableComponent(true))
                .zIndex(25)
                .build();
    }
}