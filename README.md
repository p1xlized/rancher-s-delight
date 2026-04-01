# Rancher's Delight

Small farming game made for fun.

You walk around, prepare ground, plant seeds, and watch crops grow.

## What I used (simple)

- Java
- JavaFX (window + UI)
- FXGL (game engine stuff)
- Maven (build tool)

## How to run the game

From project root:

```bash
./mvnw javafx:run
```

## How to build the game JAR

From project root:

```bash
./mvnw clean package -DskipTests
```

After build, share this file:

- `target/ranchers_delight-1.7-fat.jar`

Run it with:

```bash
java -jar target/ranchers_delight-1.7-fat.jar
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

- If build fails because of Java version, check your JDK version.
- This project is currently set in `pom.xml` to compile with Java `25`.
