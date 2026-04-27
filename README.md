# MazeRunner

A first-person 3D maze runner built entirely from scratch in Java — no external libraries. Uses a software raycaster for 3D projection, a procedurally generated maze, and a fixed-timestep game loop for smooth, frame-rate-independent movement.

## Features

- **Raycasting renderer** — DDA (Digital Differential Analyzer) algorithm with fisheye correction and N/S vs E/W wall shading
- **Procedural maze generation** — Randomized Depth-First Search (Recursive Backtracker) producing a unique perfect maze every run
- **Smooth movement** — fixed-timestep physics loop (60 Hz) decoupled from render rate; wall-sliding collision detection
- **Pure Java** — AWT/Swing windowing, `BufferedImage` pixel array for rendering; no LWJGL, LibGDX, or other dependencies

## Requirements

- Java 8 or later

## Build & Run

```bash
# Compile (from project root)
javac -d out -sourcepath src src/engine/Game.java

# Run
java -cp out engine.Game
```

## Controls

| Key | Action |
|-----|--------|
| W / ↑ | Move forward |
| S / ↓ | Move backward |
| A / ← | Rotate left |
| D / → | Rotate right |
| ESC | Quit |

Reach the **bottom-right exit** of the maze to win.

## Architecture

```
src/
  engine/
    Game.java          – JFrame setup and object wiring (entry point)
    GameLoop.java      – Fixed-timestep loop with accumulator pattern
    InputHandler.java  – KeyAdapter tracking held keys
  world/
    Map.java           – 2D boolean wall grid with bounds-safe queries
    MazeGenerator.java – Iterative randomized DFS maze generation
  entity/
    Player.java        – Position, direction/camera-plane vectors, movement & rotation
  graphics/
    Screen.java        – BufferedImage backed by a direct int[] pixel array
    Raycaster.java     – DDA raycasting engine, per-column wall projection
```
