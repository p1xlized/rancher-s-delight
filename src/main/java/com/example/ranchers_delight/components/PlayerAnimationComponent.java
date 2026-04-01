package com.example.ranchers_delight.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

public class PlayerAnimationComponent extends Component {

    private enum MotionState {
        IDLE,
        WALK
    }

    private enum Facing {
        FORWARD,
        BACK,
        LEFT,
        RIGHT
    }

    private static final double IDLE_FRAME_TIME = 0.2;
    private static final double WALK_FRAME_TIME = 0.12;

    private final ImageView imageView = new ImageView();
    private final Map<Facing, Image[]> idleFrames = new EnumMap<>(Facing.class);
    private final Map<Facing, Image[]> walkFrames = new EnumMap<>(Facing.class);

    private MotionState state = MotionState.IDLE;
    private Facing facing = Facing.FORWARD;
    private int frameIndex = 0;
    private double frameTimer = 0.0;
    private Point2D lastPosition = Point2D.ZERO;

    @Override
    public void onAdded() {
        loadFrames();

        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        imageView.setPreserveRatio(false);
        setImageFromCurrentState();

        entity.getViewComponent().addChild(imageView);
        lastPosition = new Point2D(entity.getX(), entity.getY());
    }

    @Override
    public void onUpdate(double tpf) {
        Point2D current = new Point2D(entity.getX(), entity.getY());
        double dx = current.getX() - lastPosition.getX();
        double dy = current.getY() - lastPosition.getY();
        lastPosition = current;

        MotionState newState = (Math.abs(dx) > 0.01 || Math.abs(dy) > 0.01) ? MotionState.WALK : MotionState.IDLE;
        Facing newFacing = resolveFacing(dx, dy, facing);

        if (newState != state || newFacing != facing) {
            state = newState;
            facing = newFacing;
            frameIndex = 0;
            frameTimer = 0.0;
            setImageFromCurrentState();
        }

        Image[] frames = getFrames(state, facing);
        if (frames.length <= 1) {
            return;
        }

        frameTimer += tpf;
        double frameDuration = state == MotionState.WALK ? WALK_FRAME_TIME : IDLE_FRAME_TIME;
        if (frameTimer >= frameDuration) {
            frameTimer -= frameDuration;
            frameIndex = (frameIndex + 1) % frames.length;
            imageView.setImage(frames[frameIndex]);
        }
    }

    private void setImageFromCurrentState() {
        Image[] frames = getFrames(state, facing);
        if (frames.length > 0) {
            imageView.setImage(frames[Math.min(frameIndex, frames.length - 1)]);
        }
    }

    private Image[] getFrames(MotionState motion, Facing direction) {
        return motion == MotionState.WALK ? walkFrames.get(direction) : idleFrames.get(direction);
    }

    private Facing resolveFacing(double dx, double dy, Facing fallback) {
        if (Math.abs(dx) >= Math.abs(dy) && Math.abs(dx) > 0.01) {
            return dx > 0 ? Facing.RIGHT : Facing.LEFT;
        }

        if (Math.abs(dy) > 0.01) {
            return dy > 0 ? Facing.FORWARD : Facing.BACK;
        }

        return fallback;
    }

    private void loadFrames() {
        idleFrames.put(Facing.FORWARD, loadSeries("/com/example/ranchers_delight/player/idle/idle_froward", 4));
        idleFrames.put(Facing.BACK, loadSeries("/com/example/ranchers_delight/player/idle/idle_back", 4));
        idleFrames.put(Facing.LEFT, loadSeries("/com/example/ranchers_delight/player/idle/idle_left", 4));
        idleFrames.put(Facing.RIGHT, loadSeries("/com/example/ranchers_delight/player/idle/idle_right", 4));

        walkFrames.put(Facing.FORWARD, loadSeries("/com/example/ranchers_delight/player/walk/walk_forward", 6));
        walkFrames.put(Facing.BACK, loadSeries("/com/example/ranchers_delight/player/walk/walk_backwards", 6));
        walkFrames.put(Facing.LEFT, loadSeries("/com/example/ranchers_delight/player/walk/walk_left", 6));
        walkFrames.put(Facing.RIGHT, loadSeries("/com/example/ranchers_delight/player/walk/walk_right", 6));

        // Ensure all directions have at least one frame.
        for (Facing facing : Facing.values()) {
            if (idleFrames.get(facing).length == 0) {
                idleFrames.put(facing, new Image[] { createFallbackImage() });
            }
            if (walkFrames.get(facing).length == 0) {
                walkFrames.put(facing, idleFrames.get(facing));
            }
        }
    }

    private Image[] loadSeries(String prefix, int count) {
        Image[] frames = new Image[count];
        int loaded = 0;

        for (int i = 1; i <= count; i++) {
            Image image = loadImage(prefix + i + ".png");
            if (image != null) {
                frames[loaded++] = image;
            }
        }

        Image[] result = new Image[loaded];
        System.arraycopy(frames, 0, result, 0, loaded);
        return result;
    }

    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        return stream == null ? null : new Image(stream);
    }

    private Image createFallbackImage() {
        InputStream fallback = getClass().getResourceAsStream("/com/example/ranchers_delight/player/idle/idle_froward1.png");
        return fallback == null ? null : new Image(fallback);
    }
}

