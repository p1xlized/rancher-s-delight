# Rancher's Delight

Small farming game made for fun.

You walk around, prepare ground, plant seeds, and watch crops grow.

## What I used (simple)

- Java
- JavaFX (window + UI)
- FXGL (game engine stuff)
- Maven (build tool)

## How to RUN the game JAR

```bash
java -jar target/ranchers_delight.jar
```

## Controls

- `W A S D` move
- Left mouse = use selected item
- `1..0` select hotbar slot
- `E` open inventory/shop
- `ESC` pause menu

## Save files

- Map saves are in `saves/` (example: `world_01.map`)
- Player progress is also saved (position, level, inventory)

## Notes
- This project is currently set in `pom.xml` to compile with Java `25`.
