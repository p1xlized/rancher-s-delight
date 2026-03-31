package com.example.ranchers_delight;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.example.ranchers_delight.entities.Player;
import com.example.ranchers_delight.factories.GameFactory;
import com.example.ranchers_delight.utils.EntityType;
import com.example.ranchers_delight.world.MapGenerator;
import com.example.ranchers_delight.ui.MainMenu;
import com.example.ranchers_delight.ui.InventoryUI;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {
    private String currentSaveFile = "world_01.map";

    private Entity player;
    private Player playerModel;
    private Entity tileHighlighter;
    private InventoryUI inventoryUI;
    private int selectedSlot = 0;
    private String[][] currentMapData;
    private Rectangle[][] tileViews;

    private Rectangle frameTop;
    private Rectangle frameBottom;
    private Rectangle frameLeft;
    private Rectangle frameRight;
    private Rectangle cornerTopLeft;
    private Rectangle cornerTopRight;
    private Rectangle cornerBottomLeft;
    private Rectangle cornerBottomRight;

    private final int TILE_SIZE = 40;
    private final int GRID_SIZE = 100;
    private static final double FRAME_THICKNESS = 30;
    private static final double CORNER_SIZE = 45;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1200);
        settings.setHeight(1000);
        settings.setTitle("Rancher's Delight");
        settings.setVersion("1.7");
        settings.setManualResizeEnabled(true);
        settings.setPreserveResizeRatio(true);
    }

    @Override
    protected void initInput() {
        // Use the movePlayer helper to ensure collisions are checked
        onKey(KeyCode.W, () -> movePlayer(0, -10));
        onKey(KeyCode.S, () -> movePlayer(0, 10));
        onKey(KeyCode.A, () -> movePlayer(-10, 0));
        onKey(KeyCode.D, () -> movePlayer(10, 0));
        onKeyDown(KeyCode.E, () -> {
            if (inventoryUI != null) {
                inventoryUI.toggleOverlay();
            }
        });
        onBtnDown(MouseButton.PRIMARY, this::tryPrepareTileAtCursor);

        // Reuse MainMenu as a Pause Menu
        onKeyDown(KeyCode.ESCAPE, () -> {
            // Because MainMenu expects a Consumer<String>, we provide one that
            // just closes the menu (since we don't need to pick a new file while paused)
            var pauseMenu = new MainMenu(selectedFile -> getSceneService().popSubScene(), true);
            getSceneService().pushSubScene(pauseMenu);
        });

        for (int i = 1; i <= 9; i++) {
            final int index = i - 1;
            onKeyDown(KeyCode.valueOf("DIGIT" + i), () -> selectSlot(index));
        }
        onKeyDown(KeyCode.DIGIT0, () -> selectSlot(9));
    }

    private void selectSlot(int index) {
        if (inventoryUI == null || index < 0 || index >= inventoryUI.getNumSlots()) {
            return;
        }

        selectedSlot = index;
        inventoryUI.updateSelector(selectedSlot);
    }

    private void movePlayer(double dx, double dy) {
        if (player == null) return;

        double nextX = player.getX() + dx;
        double nextY = player.getY() + dy;

        if (isWaterTileAt(nextX, nextY)) {
            return;
        }

        player.translateX(dx);
        player.translateY(dy);

        boolean isColliding = getGameWorld().getEntitiesByType(
                EntityType.TREE, EntityType.HOUSE, EntityType.BOX
        ).stream().anyMatch(obstacle -> player.isColliding(obstacle));

        if (isColliding) {
            player.translateX(-dx);
            player.translateY(-dy);
        }
    }

    private boolean isWaterTileAt(double entityX, double entityY) {
        if (currentMapData == null) {
            return false;
        }

        // Sample roughly from the player's center so shoreline checks feel natural.
        double centerX = entityX + 12.5;
        double centerY = entityY + 12.5;

        int tileX = (int) (centerX / TILE_SIZE);
        int tileY = (int) (centerY / TILE_SIZE);

        if (tileX < 0 || tileY < 0 || tileX >= currentMapData.length || tileY >= currentMapData[0].length) {
            return true;
        }

        return "waterTile".equals(currentMapData[tileX][tileY]);
    }

    private void tryPrepareTileAtCursor() {
        if (!isHoeSelected() || currentMapData == null || tileViews == null) {
            return;
        }

        int tileX = (int) (getInput().getMouseXWorld() / TILE_SIZE);
        int tileY = (int) (getInput().getMouseYWorld() / TILE_SIZE);

        if (tileX < 0 || tileY < 0 || tileX >= currentMapData.length || tileY >= currentMapData[0].length) {
            return;
        }

        if (!"brownTile".equals(currentMapData[tileX][tileY])) {
            return;
        }

        currentMapData[tileX][tileY] = "preparedTile";
        tileViews[tileX][tileY].setFill(Color.PERU);
    }

    private boolean isHoeSelected() {
        if (playerModel == null) {
            return false;
        }

        var selectedItem = playerModel.getItemInSlot(selectedSlot);
        return selectedItem != null && "hoe".equalsIgnoreCase(selectedItem.getName());
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameFactory());

        // 1. MODULAR UI TRIGGER: Open the MainMenu with Save Slot Logic
        // We use runOnce to ensure the SubScene is pushed on the JavaFX Thread
        runOnce(() -> {
            var menu = new MainMenu(selectedFile -> {
                // When a slot is clicked, store the filename and close the menu
                this.currentSaveFile = selectedFile;
                getSceneService().popSubScene();

                // Start the actual loading sequence
                finalizeWorldInitialization();
            });
            getSceneService().pushSubScene(menu);
         
        }, Duration.seconds(0.05));
    }

    /**
     * This helper method contains the logic previously in initGame.
     * It is called only AFTER the user selects a save slot.
     */
    private void finalizeWorldInitialization() {
        // 2. PERSISTENCE: Load from the 'saves/' folder or generate fresh
        var generator = new MapGenerator(GRID_SIZE, GRID_SIZE);

        // Attempt to load the selected file (e.g., world_01.map)
        String[][] mapData = generator.loadMap(currentSaveFile);

        // If slot was [EMPTY], generate a new noisy island and save it
        if (mapData == null) {
            mapData = generator.generate();
            generator.saveMap(mapData, currentSaveFile);
        }

        currentMapData = mapData;

        // 3. OPTIMIZED RENDERING: Bake the tilemap into a single Pane
        var bgPane = new Pane();
        tileViews = new Rectangle[GRID_SIZE][GRID_SIZE];
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                var tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                String type = mapData[x][y];

                // Map styling logic
                if (type.equals("waterTile")) {
                    tile.setFill(Color.DEEPSKYBLUE);
                } else if (type.equals("brownTile")) {
                    tile.setFill(Color.SADDLEBROWN);
                } else if (type.equals("preparedTile")) {
                    tile.setFill(Color.PERU);
                } else {
                    tile.setFill(Color.FORESTGREEN); // Base for greenTile and treeTile
                }

                tile.setTranslateX(x * TILE_SIZE);
                tile.setTranslateY(y * TILE_SIZE);
                bgPane.getChildren().add(tile);
                tileViews[x][y] = tile;
            }
        }

        // Attach map to world layer
        entityBuilder()
                .view(bgPane)
                .zIndex(-1)
                .buildAndAttach();

        // 4. ENTITY SPAWNING: Populate the world based on mapData
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (mapData[x][y].equals("treeTile")) {
                    spawn("tree", x * TILE_SIZE, y * TILE_SIZE);
                }
            }
        }

        // 5. PLAYER & RANCH ASSETS: Center of the map
        int startX = (GRID_SIZE * TILE_SIZE) / 2;
        int startY = (GRID_SIZE * TILE_SIZE) / 2;

        player = spawn("player", startX, startY);
        player.setZIndex(100);
        playerModel = player.getObject("playerModel");
        if (inventoryUI != null) {
            inventoryUI.setPlayer(playerModel);
            selectSlot(0);
        }

        spawn("house", startX + 80, startY);
        spawn("box", startX - 80, startY + 40);

        // 6. UI HIGHLIGHTER: Grid cursor
        tileHighlighter = entityBuilder()
                .view(new Rectangle(TILE_SIZE, TILE_SIZE, Color.rgb(255, 255, 255, 0.3)))
                .at(0, 0)
                .zIndex(10)
                .buildAndAttach();

        // 7. VIEWPORT: Bind camera and fix Niri letterboxing
        getGameScene().getRoot().setStyle("-fx-background-color: #00BFFF;");
        getGameScene().getViewport().bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);
        getGameScene().getViewport().setBounds(0, 0, GRID_SIZE * TILE_SIZE, GRID_SIZE * TILE_SIZE);
    }
    @Override
    protected void initUI() {
        int slots = playerModel != null ? playerModel.getInventorySize() : 10;
        inventoryUI = new InventoryUI(slots);
        if (playerModel != null) {
            inventoryUI.setPlayer(playerModel);
        }

        var frameColor = Color.rgb(45, 30, 20);
        frameTop = new Rectangle(0, 0, frameColor);
        frameBottom = new Rectangle(0, 0, frameColor);
        frameLeft = new Rectangle(0, 0, frameColor);
        frameRight = new Rectangle(0, 0, frameColor);

        addUINode(frameTop);
        addUINode(frameBottom);
        addUINode(frameLeft);
        addUINode(frameRight);

        cornerTopLeft = addCornerTrim();
        cornerTopRight = addCornerTrim();
        cornerBottomLeft = addCornerTrim();
        cornerBottomRight = addCornerTrim();

        updateFrameLayout();
    }

    private Rectangle addCornerTrim() {
        var trim = new Rectangle(CORNER_SIZE, CORNER_SIZE, Color.rgb(60, 45, 35));
        trim.setStroke(Color.GOLD);
        trim.setStrokeWidth(2);
        addUINode(trim);
        return trim;
    }

    private void updateFrameLayout() {
        if (frameTop == null) {
            return;
        }

        double width = getAppWidth();
        double height = getAppHeight();

        frameTop.setWidth(width);
        frameTop.setHeight(FRAME_THICKNESS);
        frameTop.setTranslateX(0);
        frameTop.setTranslateY(0);

        frameBottom.setWidth(width);
        frameBottom.setHeight(FRAME_THICKNESS);
        frameBottom.setTranslateX(0);
        frameBottom.setTranslateY(height - FRAME_THICKNESS);

        frameLeft.setWidth(FRAME_THICKNESS);
        frameLeft.setHeight(height);
        frameLeft.setTranslateX(0);
        frameLeft.setTranslateY(0);

        frameRight.setWidth(FRAME_THICKNESS);
        frameRight.setHeight(height);
        frameRight.setTranslateX(width - FRAME_THICKNESS);
        frameRight.setTranslateY(0);

        cornerTopLeft.setTranslateX(0);
        cornerTopLeft.setTranslateY(0);
        cornerTopRight.setTranslateX(width - CORNER_SIZE);
        cornerTopRight.setTranslateY(0);
        cornerBottomLeft.setTranslateX(0);
        cornerBottomLeft.setTranslateY(height - CORNER_SIZE);
        cornerBottomRight.setTranslateX(width - CORNER_SIZE);
        cornerBottomRight.setTranslateY(height - CORNER_SIZE);
    }

    @Override
    protected void initPhysics() {
        // Your existing collision handlers
    }

    @Override
    protected void onUpdate(double tpf) {
        updateFrameLayout();

        if (tileHighlighter == null) return;
        double mouseX = getInput().getMouseXWorld();
        double mouseY = getInput().getMouseYWorld();
        int gridX = (int) (mouseX / TILE_SIZE) * TILE_SIZE;
        int gridY = (int) (mouseY / TILE_SIZE) * TILE_SIZE;

        if (gridX >= 0 && gridX < GRID_SIZE * TILE_SIZE && gridY >= 0 && gridY < GRID_SIZE * TILE_SIZE) {
            tileHighlighter.setVisible(true);
            tileHighlighter.setPosition(gridX, gridY);
        } else {
            tileHighlighter.setVisible(false);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}