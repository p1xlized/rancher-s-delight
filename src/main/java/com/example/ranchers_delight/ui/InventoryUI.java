package com.example.ranchers_delight.ui;

import com.example.ranchers_delight.components.Item;
import com.example.ranchers_delight.entities.Player;
import com.example.ranchers_delight.utils.Rarity;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

public class InventoryUI {

    private final int numSlots;
    private final double slotSize = 60;
    private final double slotMargin = 10;

    private final List<Rectangle> slots = new ArrayList<>();
    private final List<Text> hotbarItemTexts = new ArrayList<>();
    private final List<ImageView> hotbarItemIcons = new ArrayList<>();
    private final List<Text> hotbarCountTexts = new ArrayList<>();
    private final Map<String, Image> iconCache = new HashMap<>();
    private final Rectangle selector;
    private final Text playerHeader = new Text("No Player");
    private final VBox inventoryList = new VBox(8);
    private Player player;

    // Shop & Detailed Inventory Overlay
    private final Pane overlayContainer;
    private final VBox inventoryTab;
    private final VBox shopTab;

    public InventoryUI(int numSlots) {
        this.numSlots = numSlots;

        // --- 1. HUD HOTBAR (The existing slots at the bottom) ---
        double totalWidth = (numSlots * slotSize) + ((numSlots - 1) * slotMargin);
        double startX = (getAppWidth() - totalWidth) / 2;
        double startY = getAppHeight() - slotSize - 50;

        for (int i = 0; i < numSlots; i++) {
            var slot = new Rectangle(slotSize, slotSize, Color.rgb(40, 40, 40, 0.9));
            slot.setStroke(Color.LIGHTGRAY);
            slot.setStrokeWidth(2);

            double x = startX + (i * (slotSize + slotMargin));
            slot.setTranslateX(x);
            slot.setTranslateY(startY);

            var hint = new Text(String.valueOf(i + 1));
            hint.setFill(Color.WHITE);
            hint.setTranslateX(x + 5);
            hint.setTranslateY(startY + 15);

            var itemText = new Text("-");
            itemText.setFill(Color.WHITE);
            itemText.setTranslateX(x + 8);
            itemText.setTranslateY(startY + slotSize - 8);

            var icon = new ImageView();
            icon.setFitWidth(34);
            icon.setFitHeight(34);
            icon.setPreserveRatio(true);
            icon.setTranslateX(x + (slotSize - 34) / 2);
            icon.setTranslateY(startY + 16);

            var countText = new Text("");
            countText.setFill(Color.GOLD);
            countText.setTranslateX(x + slotSize - 16);
            countText.setTranslateY(startY + slotSize - 12);

            addUINode(slot);
            addUINode(hint);
            addUINode(icon);
            addUINode(countText);
            addUINode(itemText);
            slots.add(slot);
            hotbarItemTexts.add(itemText);
            hotbarItemIcons.add(icon);
            hotbarCountTexts.add(countText);
        }

        selector = new Rectangle(slotSize + 8, slotSize + 8, Color.TRANSPARENT);
        selector.setStroke(Color.GOLD);
        selector.setStrokeWidth(3);
        addUINode(selector);
        updateSelector(0);

        // --- 2. SHOP BUTTON ---
        Button btnOpenMenu = new Button("SHOP & INV");
        btnOpenMenu.setTranslateX(getAppWidth() - 150);
        btnOpenMenu.setTranslateY(getAppHeight() - 100);
        btnOpenMenu.setOnAction(e -> toggleOverlay());
        addUINode(btnOpenMenu);

        // --- 3. THE DETAILED OVERLAY (MC STYLE) ---
        overlayContainer = new Pane();
        overlayContainer.setVisible(false);

        // Background blur/dim
        Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), Color.rgb(0, 0, 0, 0.6));

        // Tab Content Areas
        inventoryTab = createInventoryTab();
        shopTab = createShopTab();
        shopTab.setVisible(false); // Start on Inventory

        // Tab Buttons
        HBox tabHeader = new HBox(10);
        tabHeader.setAlignment(Pos.CENTER);
        tabHeader.setTranslateX((getAppWidth() - 400) / 2);
        tabHeader.setTranslateY(150);

        Button btnInv = new Button("MY INVENTORY");
        btnInv.setOnAction(e -> switchTab(true));

        Button btnShop = new Button("SHOP");
        btnShop.setOnAction(e -> switchTab(false));

        Button btnClose = new Button("X");
        btnClose.setOnAction(e -> toggleOverlay());

        tabHeader.getChildren().addAll(btnInv, btnShop, btnClose);

        overlayContainer.getChildren().addAll(bg, tabHeader, inventoryTab, shopTab);
        addUINode(overlayContainer);
    }

    private VBox createInventoryTab() {
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefSize(400, 400);
        vbox.setTranslateX((getAppWidth() - 400) / 2);
        vbox.setTranslateY(200);
        vbox.setStyle("-fx-background-color: rgba(60, 60, 60, 0.9); -fx-border-color: gold;");

        playerHeader.setFill(Color.WHITESMOKE);
        inventoryList.setAlignment(Pos.TOP_LEFT);

        vbox.getChildren().add(new Text("DETAILED INVENTORY"));
        vbox.getChildren().add(playerHeader);
        vbox.getChildren().add(inventoryList);
        return vbox;
    }

    private VBox createShopTab() {
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefSize(400, 400);
        vbox.setTranslateX((getAppWidth() - 400) / 2);
        vbox.setTranslateY(200);
        vbox.setStyle("-fx-background-color: rgba(40, 45, 60, 0.9); -fx-border-color: #00BFFF;");

        vbox.getChildren().add(new Text("RANCHER'S SHOP"));

        Button btnBuySeeds = new Button("BUY SEEDS - $10");
        btnBuySeeds.setOnAction(e -> buyItem(new Item("Seeds", Rarity.COMMON, 5)));

        Button btnBuyFence = new Button("BUY FENCE - $5");
        btnBuyFence.setOnAction(e -> buyItem(new Item("Fence", Rarity.UNCOMMON)));

        vbox.getChildren().add(btnBuySeeds);
        vbox.getChildren().add(btnBuyFence);
        return vbox;
    }

    public int getNumSlots() {
        return numSlots;
    }

    public void setPlayer(Player player) {
        this.player = player;
        refresh();
    }

    public void refresh() {
        if (player == null) {
            playerHeader.setText("No Player");
            inventoryList.getChildren().setAll(new Text("Spawn player to see inventory."));
            hotbarItemTexts.forEach(text -> text.setText("-"));
            hotbarItemIcons.forEach(icon -> icon.setImage(null));
            hotbarCountTexts.forEach(text -> text.setText(""));
            return;
        }

        playerHeader.setText(player.getName() + " (Lvl " + player.getLevel() + ")");
        inventoryList.getChildren().clear();

        for (int i = 0; i < numSlots; i++) {
            Item item = player.getItemInSlot(i);
            String label = (item == null)
                    ? "[empty]"
                    : item.getName() + " x" + item.getQuantity() + " [" + item.getRarity().getLabel() + "]";

            if (i < hotbarItemTexts.size()) {
                Image iconImage = resolveItemIcon(item);
                hotbarItemIcons.get(i).setImage(iconImage);
                hotbarItemTexts.get(i).setText(iconImage == null ? shortName(item) : "");
                hotbarCountTexts.get(i).setText(item != null && item.getQuantity() > 1 ? String.valueOf(item.getQuantity()) : "");
            }

            Text row = new Text((i + 1) + ": " + label);
            row.setFill(Color.WHITE);
            inventoryList.getChildren().add(row);
        }
    }

    private void buyItem(Item item) {
        if (player == null) {
            System.out.println("No player model bound to Inventory UI");
            return;
        }

        boolean added = player.addItemToInventory(item);
        if (added) {
            refresh();
        }
    }

    private String shortName(Item item) {
        if (item == null) {
            return "-";
        }

        String name = item.getName();
        return name.length() <= 7 ? name : name.substring(0, 7);
    }

    private Image resolveItemIcon(Item item) {
        if (item == null || item.getName() == null) {
            return null;
        }

        String key = item.getName().toLowerCase();
        if (iconCache.containsKey(key)) {
            return iconCache.get(key);
        }

        String path;
        if ("hoe".equals(key)) {
            path = "/com/example/ranchers_delight/objects/hoe.png";
        } else if ("seed".equals(key) || "seeds".equals(key)) {
            path = "/com/example/ranchers_delight/objects/seeds_bundle.png";
        } else {
            iconCache.put(key, null);
            return null;
        }

        Image image = loadImage(path);
        iconCache.put(key, image);
        return image;
    }

    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            return null;
        }

        return new Image(stream);
    }

    private void switchTab(boolean showInv) {
        inventoryTab.setVisible(showInv);
        shopTab.setVisible(!showInv);
    }

    public void toggleOverlay() {
        overlayContainer.setVisible(!overlayContainer.isVisible());
    }

    public void updateSelector(int selectedSlot) {
        double totalWidth = (numSlots * slotSize) + ((numSlots - 1) * slotMargin);
        double startX = (getAppWidth() - totalWidth) / 2;
        double startY = getAppHeight() - slotSize - 54;

        selector.setTranslateX(startX + (selectedSlot * (slotSize + slotMargin)) - 4);
        selector.setTranslateY(startY);
    }
}