package com.example.ranchers_delight.ui;

import com.almasb.fxgl.scene.SubScene;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static com.almasb.fxgl.dsl.FXGL.*;

public class MainMenu extends SubScene {

    private static class SaveSlotRow {
        final int slotNum;
        final String fileName;
        final Text slotText;
        final Button actionButton;

        SaveSlotRow(int slotNum, String fileName, Text slotText, Button actionButton) {
            this.slotNum = slotNum;
            this.fileName = fileName;
            this.slotText = slotText;
            this.actionButton = actionButton;
        }
    }

    private final List<SaveSlotRow> saveSlotRows = new ArrayList<>();

    // We change Runnable to Consumer<String> so we can pass the chosen save file name back to GameApp
    public MainMenu(Consumer<String> onFileSelected) {
        this(onFileSelected, false);
    }

    public MainMenu(Consumer<String> onFileSelected, boolean allowBackToGame) {
        var bg = new Rectangle(getAppWidth(), getAppHeight(), Color.rgb(0, 0, 0, 0.8));

        var title = new Text("RANCHER'S DELIGHT");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");

        VBox saveSlotsContainer = new VBox(15);
        saveSlotsContainer.setAlignment(Pos.CENTER);

        // Create 3 Save Slots
        for (int i = 1; i <= 3; i++) {
            var row = createSaveSlot(i, onFileSelected);
            saveSlotRows.add(row);
            saveSlotsContainer.getChildren().add(new HBox(20, row.slotText, row.actionButton));
        }

        refreshSaveSlots(onFileSelected);

        Button btnDeleteSaves = new Button("DELETE SAVES");
        btnDeleteSaves.setStyle("-fx-font-size: 16px; -fx-background-color: #773333; -fx-text-fill: white;");
        btnDeleteSaves.setOnAction(_ -> {
            deleteAllSaves();
            refreshSaveSlots(onFileSelected);
        });

        VBox actionButtons = new VBox(12);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.getChildren().add(btnDeleteSaves);

        if (allowBackToGame) {
            Button btnBackToGame = new Button("BACK TO GAME");
            btnBackToGame.setStyle("-fx-font-size: 16px; -fx-background-color: #224422; -fx-text-fill: white;");
            btnBackToGame.setOnAction(_ -> getSceneService().popSubScene());
            actionButtons.getChildren().add(btnBackToGame);
        }

        var btnExit = new Button("EXIT TO ARCH");
        btnExit.setStyle("-fx-font-size: 18px; -fx-background-color: #442222; -fx-text-fill: white;");
        btnExit.setOnAction(_ -> getGameController().exit());

        var mainLayout = new VBox(30, title, saveSlotsContainer, actionButtons, btnExit);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPrefSize(getAppWidth(), getAppHeight());

        getContentRoot().getChildren().addAll(bg, mainLayout);
    }

    private SaveSlotRow createSaveSlot(int slotNum, Consumer<String> onFileSelected) {
        String fileName = "world_0" + slotNum + ".map";
        var slotText = new Text();

        Button btnLoad = new Button();
        btnLoad.setOnAction(_ -> onFileSelected.accept(fileName));

        return new SaveSlotRow(slotNum, fileName, slotText, btnLoad);
    }

    private void refreshSaveSlots(Consumer<String> onFileSelected) {
        for (var row : saveSlotRows) {
            boolean exists = Files.exists(Paths.get("saves", row.fileName));
            row.slotText.setText("SLOT " + row.slotNum + (exists ? " [SAVED]" : " [EMPTY]"));
            row.slotText.setFill(exists ? Color.LIGHTGREEN : Color.GRAY);
            row.actionButton.setText(exists ? "LOAD" : "NEW");
            row.actionButton.setOnAction(_ -> onFileSelected.accept(row.fileName));
        }
    }

    private void deleteAllSaves() {
        for (int i = 1; i <= 3; i++) {
            String fileName = "world_0" + i + ".map";
            try {
                Files.deleteIfExists(Paths.get("saves", fileName));
            } catch (IOException ex) {
                System.out.println("Failed to delete save: " + fileName);
            }
        }
    }
}