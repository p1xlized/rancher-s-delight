package com.example.ranchers_delight.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public abstract class TileComponent extends Component {
    protected boolean plantable;

    public TileComponent(boolean plantable) {
        this.plantable = plantable;
    }

    public boolean isPlantable() {
        return plantable;
    }
}