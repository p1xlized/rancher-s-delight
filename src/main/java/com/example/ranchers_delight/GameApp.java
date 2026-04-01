package com.example.ranchers_delight;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.example.ranchers_delight.components.Item;
import com.example.ranchers_delight.entities.Player;
import com.example.ranchers_delight.entities.PotatoCrop;
import com.example.ranchers_delight.factories.GameFactory;
import com.example.ranchers_delight.utils.EntityType;
import com.example.ranchers_delight.utils.Rarity;
import com.example.ranchers_delight.world.MapGenerator;
import com.example.ranchers_delight.ui.MainMenu;
import com.example.ranchers_delight.ui.InventoryUI;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {
    private String currentSaveFile = "world_01.map";

    private Entity player;
    private Player playerModel;
    private Entity tileHighlighter;
    private InventoryUI inventoryUI;
    private int selectedSlot = 0;
    private String[][] currentMapData;
    private Canvas terrainCanvas;
    private GraphicsContext terrainGraphics;

    private Rectangle frameTop;
    private Rectangle frameBottom;
    private Rectangle frameLeft;
    private Rectangle frameRight;
    private Rectangle cornerTopLeft;
    private Rectangle cornerTopRight;
    private Rectangle cornerBottomLeft;
    private Rectangle cornerBottomRight;

    private final Map<String, PotatoCrop> plantedPotatoes = new HashMap<>();
    private final Map<String, Entity> plantedPotatoViews = new HashMap<>();
    private final Map<String, ImageView> plantedPotatoImageViews = new HashMap<>();
    private final Map<String, Image> cropTextureCache = new HashMap<>();
    private double growthTickAccumulator = 0.0;
    private double autosaveAccumulator = 0.0;
    private boolean shutdownHookRegistered = false;

    private final int TILE_SIZE = 40;
    private final int GRID_SIZE = 100;
    private static final double FRAME_THICKNESS = 30;
    private static final double CORNER_SIZE = 45;
    private static final double CROP_GROWTH_INTERVAL_SECONDS = 30.0;
    private static final double AUTOSAVE_INTERVAL_SECONDS = 10.0;
    private static final double CAMERA_ZOOM = 1.35;

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
        onBtnDown(MouseButton.PRIMARY, this::tryUseSelectedItemAtCursor);

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

    private void tryUseSelectedItemAtCursor() {
        if (currentMapData == null || terrainGraphics == null) {
            return;
        }

        if (isHoeSelected()) {
            tryPrepareTileAtCursor();
            return;
        }

        if (isSeedSelected()) {
            tryPlantSeedAtCursor();
        }
    }

    private void tryPrepareTileAtCursor() {
        int[] tilePos = getCursorTile();
        if (tilePos == null) {
            return;
        }

        int tileX = tilePos[0];
        int tileY = tilePos[1];

        if (!"brownTile".equals(currentMapData[tileX][tileY])) {
            return;
        }

        currentMapData[tileX][tileY] = "preparedTile";
        drawSingleTile(tileX, tileY);
    }

    private void tryPlantSeedAtCursor() {
        if (playerModel == null) {
            return;
        }

        int[] tilePos = getCursorTile();
        if (tilePos == null) {
            return;
        }

        int tileX = tilePos[0];
        int tileY = tilePos[1];

        if (!"preparedTile".equals(currentMapData[tileX][tileY])) {
            return;
        }

        boolean consumed = playerModel.consumeItemInSlot(selectedSlot);
        if (!consumed) {
            return;
        }

        currentMapData[tileX][tileY] = "plantedTile";
        drawSingleTile(tileX, tileY);
        spawnPotatoCrop(tileX, tileY);
        if (inventoryUI != null) {
            inventoryUI.refresh();
        }
    }

    private int[] getCursorTile() {
        if (currentMapData == null) {
            return null;
        }

        int tileX = (int) (getInput().getMouseXWorld() / TILE_SIZE);
        int tileY = (int) (getInput().getMouseYWorld() / TILE_SIZE);

        if (tileX < 0 || tileY < 0 || tileX >= currentMapData.length || tileY >= currentMapData[0].length) {
            return null;
        }

        return new int[] { tileX, tileY };
    }

    private boolean isHoeSelected() {
        if (playerModel == null) {
            return false;
        }

        var selectedItem = playerModel.getItemInSlot(selectedSlot);
        return selectedItem != null && "hoe".equalsIgnoreCase(selectedItem.getName());
    }

    private boolean isSeedSelected() {
        if (playerModel == null) {
            return false;
        }

        var selectedItem = playerModel.getItemInSlot(selectedSlot);
        if (selectedItem == null) {
            return false;
        }

        String name = selectedItem.getName();
        return "seed".equalsIgnoreCase(name) || "seeds".equalsIgnoreCase(name);
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameFactory());

        if (!shutdownHookRegistered) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::saveProgressNow));
            shutdownHookRegistered = true;
        }

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
        clearPlantedCropViews();
        plantedPotatoes.clear();
        growthTickAccumulator = 0.0;
        autosaveAccumulator = 0.0;

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
        int mapWidth = mapData.length;
        int mapHeight = mapData[0].length;
        int worldPixelWidth = mapWidth * TILE_SIZE;
        int worldPixelHeight = mapHeight * TILE_SIZE;

        terrainCanvas = new Canvas(worldPixelWidth, worldPixelHeight);
        terrainGraphics = terrainCanvas.getGraphicsContext2D();
        redrawTerrain();

        // Attach map to world layer
        entityBuilder()
                .view(terrainCanvas)
                .zIndex(-1)
                .buildAndAttach();

        // 4. ENTITY SPAWNING: Populate the world based on mapData
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                if (mapData[x][y].equals("treeTile")) {
                    spawn("tree", x * TILE_SIZE, y * TILE_SIZE);
                } else if (mapData[x][y].equals("plantedTile")) {
                    spawnPotatoCrop(x, y);
                }
            }
        }

        // 5. PLAYER & RANCH ASSETS: Center of the map
        int startX = (GRID_SIZE * TILE_SIZE) / 2;
        int startY = (GRID_SIZE * TILE_SIZE) / 2;

        player = spawn("player", startX, startY);
        player.setZIndex(100);
        playerModel = player.getObject("playerModel");
        loadPlayerProgress(generator, startX, startY);
        if (inventoryUI != null) {
            inventoryUI.setPlayer(playerModel);
            selectSlot(selectedSlot);
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
        getGameScene().getViewport().setBounds(0, 0, worldPixelWidth, worldPixelHeight);
        getGameScene().getViewport().setZoom(CAMERA_ZOOM);
        getGameScene().getViewport().setLazy(true);
    }
    @Override
    protected void initUI() {
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

        int slots = playerModel != null ? playerModel.getInventorySize() : 10;
        inventoryUI = new InventoryUI(slots);
        if (playerModel != null) {
            inventoryUI.setPlayer(playerModel);
        }
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

        // Top frame
        frameTop.setWidth(width);
        frameTop.setHeight(FRAME_THICKNESS);
        frameTop.setTranslateX(0);
        frameTop.setTranslateY(0);

        // Bottom frame
        frameBottom.setWidth(width);
        frameBottom.setHeight(FRAME_THICKNESS);
        frameBottom.setTranslateX(0);
        frameBottom.setTranslateY(height - FRAME_THICKNESS);

        // Left frame
        frameLeft.setWidth(FRAME_THICKNESS);
        frameLeft.setHeight(height);
        frameLeft.setTranslateX(0);
        frameLeft.setTranslateY(0);

        // Right frame
        frameRight.setWidth(FRAME_THICKNESS);
        frameRight.setHeight(height);
        frameRight.setTranslateX(width - FRAME_THICKNESS);
        frameRight.setTranslateY(0);

        // Corner trims
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
        updateCropGrowth(tpf);
        updateAutosave(tpf);

        if (tileHighlighter == null) return;
        double mouseX = getInput().getMouseXWorld();
        double mouseY = getInput().getMouseYWorld();
        int gridX = (int) (mouseX / TILE_SIZE) * TILE_SIZE;
        int gridY = (int) (mouseY / TILE_SIZE) * TILE_SIZE;

        int worldPixelWidth = currentMapData == null ? GRID_SIZE * TILE_SIZE : currentMapData.length * TILE_SIZE;
        int worldPixelHeight = currentMapData == null ? GRID_SIZE * TILE_SIZE : currentMapData[0].length * TILE_SIZE;

        if (gridX >= 0 && gridX < worldPixelWidth && gridY >= 0 && gridY < worldPixelHeight) {
            tileHighlighter.setVisible(true);
            tileHighlighter.setPosition(gridX, gridY);
        } else {
            tileHighlighter.setVisible(false);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


    private void updateCropGrowth(double tpf) {
        if (plantedPotatoes.isEmpty()) {
            return;
        }

        growthTickAccumulator += tpf;
        if (growthTickAccumulator < CROP_GROWTH_INTERVAL_SECONDS) {
            return;
        }

        int growthTicks = (int) (growthTickAccumulator / CROP_GROWTH_INTERVAL_SECONDS);
        growthTickAccumulator -= growthTicks * CROP_GROWTH_INTERVAL_SECONDS;

        for (int i = 0; i < growthTicks; i++) {
            advancePotatoGrowthTick();
        }
    }

    private void advancePotatoGrowthTick() {
        for (Map.Entry<String, PotatoCrop> entry : plantedPotatoes.entrySet()) {
            PotatoCrop crop = entry.getValue();
            if (crop.advanceGrowthTick()) {
                int[] pos = tileFromKey(entry.getKey());
                refreshPotatoView(pos[0], pos[1], crop);
            }
        }
    }

    private void spawnPotatoCrop(int tileX, int tileY) {
        String key = tileKey(tileX, tileY);
        PotatoCrop crop = new PotatoCrop();
        plantedPotatoes.put(key, crop);
        refreshPotatoView(tileX, tileY, crop);
    }

    private void refreshPotatoView(int tileX, int tileY, PotatoCrop crop) {
        String key = tileKey(tileX, tileY);
        Image image = getCachedCropTexture(crop.getTexturePath());
        if (image == null) {
            return;
        }

        ImageView existingView = plantedPotatoImageViews.get(key);
        if (existingView != null) {
            existingView.setImage(image);
            return;
        }

        ImageView newView = new ImageView(image);
        newView.setFitWidth(TILE_SIZE);
        newView.setFitHeight(TILE_SIZE);
        newView.setPreserveRatio(false);

        Entity entity = entityBuilder()
                .at(tileX * TILE_SIZE, tileY * TILE_SIZE)
                .view(newView)
                .zIndex(12)
                .buildAndAttach();

        plantedPotatoImageViews.put(key, newView);
        plantedPotatoViews.put(key, entity);
    }

    private Image getCachedCropTexture(String texturePath) {
        if (cropTextureCache.containsKey(texturePath)) {
            return cropTextureCache.get(texturePath);
        }

        InputStream stream = getClass().getResourceAsStream(texturePath);
        Image image = stream == null ? null : new Image(stream);
        cropTextureCache.put(texturePath, image);
        return image;
    }

    private void clearPlantedCropViews() {
        for (Entity cropView : plantedPotatoViews.values()) {
            cropView.removeFromWorld();
        }
        plantedPotatoViews.clear();
        plantedPotatoImageViews.clear();
    }

    private String tileKey(int x, int y) {
        return x + ":" + y;
    }

    private int[] tileFromKey(String key) {
        String[] parts = key.split(":", 2);
        return new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) };
    }

    private void redrawTerrain() {
        if (terrainGraphics == null || currentMapData == null) {
            return;
        }

        for (int y = 0; y < currentMapData[0].length; y++) {
            for (int x = 0; x < currentMapData.length; x++) {
                drawSingleTile(x, y);
            }
        }
    }

    private void drawSingleTile(int tileX, int tileY) {
        if (terrainGraphics == null || currentMapData == null) {
            return;
        }

        terrainGraphics.setFill(colorForTileType(currentMapData[tileX][tileY]));
        terrainGraphics.fillRect(tileX * TILE_SIZE, tileY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    private Color colorForTileType(String type) {
        return switch (type) {
            case "waterTile" -> Color.DEEPSKYBLUE;
            case "brownTile" -> Color.SADDLEBROWN;
            case "preparedTile" -> Color.PERU;
            case "plantedTile" -> Color.YELLOWGREEN;
            default -> Color.FORESTGREEN;
        };
    }

    private void updateAutosave(double tpf) {
        autosaveAccumulator += tpf;
        if (autosaveAccumulator < AUTOSAVE_INTERVAL_SECONDS) {
            return;
        }

        autosaveAccumulator = 0.0;
        saveProgressNow();
    }

    private void saveProgressNow() {
        if (currentMapData == null) {
            return;
        }

        MapGenerator generator = new MapGenerator(GRID_SIZE, GRID_SIZE);
        generator.saveMap(currentMapData, currentSaveFile);

        if (player == null || playerModel == null) {
            return;
        }

        Map<String, String> progress = new LinkedHashMap<>();
        progress.put("x", String.valueOf(player.getX()));
        progress.put("y", String.valueOf(player.getY()));
        progress.put("level", String.valueOf(playerModel.getLevel()));
        progress.put("selectedSlot", String.valueOf(selectedSlot));

        for (int i = 0; i < playerModel.getInventorySize(); i++) {
            Item item = playerModel.getItemInSlot(i);
            String value = item == null
                    ? ""
                    : item.getName() + "|" + item.getRarity().name() + "|" + item.getQuantity();
            progress.put("inv." + i, value);
        }

        generator.savePlayerProgress(currentSaveFile, progress);
    }

    private void loadPlayerProgress(MapGenerator generator, int defaultX, int defaultY) {
        if (player == null || playerModel == null) {
            return;
        }

        Map<String, String> progress = generator.loadPlayerProgress(currentSaveFile);
        if (progress.isEmpty()) {
            player.setPosition(defaultX, defaultY);
            selectedSlot = 0;
            return;
        }

        player.setPosition(
                parseDouble(progress.get("x"), defaultX),
                parseDouble(progress.get("y"), defaultY)
        );

        playerModel.setLevel(parseInt(progress.get("level"), playerModel.getLevel()));
        selectedSlot = clampSlot(parseInt(progress.get("selectedSlot"), 0));

        playerModel.clearInventory();
        for (int i = 0; i < playerModel.getInventorySize(); i++) {
            String raw = progress.get("inv." + i);
            if (raw == null || raw.isBlank()) {
                continue;
            }

            String[] parts = raw.split("\\|", 3);
            if (parts.length < 3) {
                continue;
            }

            try {
                Rarity rarity = Rarity.valueOf(parts[1]);
                int qty = Math.max(1, Integer.parseInt(parts[2]));
                playerModel.setItemInSlot(i, new Item(parts[0], rarity, qty));
            } catch (Exception ignored) {
                // Keep save loading resilient to partial/corrupt lines.
            }
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private double parseDouble(String value, double fallback) {
        try {
            return value == null ? fallback : Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private int clampSlot(int slot) {
        int max = playerModel == null ? 9 : playerModel.getInventorySize() - 1;
        return Math.max(0, Math.min(slot, max));
    }
}