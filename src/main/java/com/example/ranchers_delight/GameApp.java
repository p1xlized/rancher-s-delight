package com.example.ranchers_delight;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.example.ranchers_delight.factories.GameFactory;
import com.example.ranchers_delight.world.MapGenerator;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {

    private Entity player;

    private final int TILE_SIZE = 40; // 25 tiles * 24px = 600px total width
    private final int GRID_SIZE = 25;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1000);
        settings.setHeight(1000); // Extra height for UI space
        settings.setTitle("Rancher's Delight");
        settings.setVersion("1.0");
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.W, () -> player.translateY(-5));
        onKey(KeyCode.S, () -> player.translateY(5));
        onKey(KeyCode.A, () -> player.translateX(-5));
        onKey(KeyCode.D, () -> player.translateX(5));
    }



    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameFactory());

        MapGenerator generator = new MapGenerator(GRID_SIZE, GRID_SIZE);
        String[][] mapData = generator.generate();

        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                spawn(mapData[x][y], x * TILE_SIZE, y * TILE_SIZE);
            }
        }

        player = spawn("player", 300, 300);
    }

    @Override
    protected void initUI() {
        // Create a simple HUD at the bottom


        Text hintText = new Text("WASD to Move");
        hintText.setFill(Color.WHITE);
        hintText.setFont(Font.font(18));
        hintText.setTranslateX(20);
        hintText.setTranslateY(960);

        getGameScene().addUINodes( hintText);
    }

    public static void main(String[] args) {
        launch(args);
    }
}