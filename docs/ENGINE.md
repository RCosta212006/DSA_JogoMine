# Engine Overview and Student Guide

This repository is a small, opinionated “engine shell” built on top of jMonkeyEngine (jME3) that your game-projects will sit on. It’s designed for an object-oriented programming course where students implement the game layer using simple, engine-neutral classes, while the engine layer handles rendering, physics, input, and scene lifecycle.

The goal: let you practice core OO design (class hierarchy, composition, behaviors, error-handling) without getting blocked by rendering or physics APIs.

---

## Big Picture

- Student layer
  - You write engine-neutral classes under `jogo.gameobject.*`.
  - Base class: `GameObject` (stores just a name and a position: `jogo.framework.math.Vec3`).
  - Hierarchy provided to extend:
    - `GameObject`
      - `character.Character` (example: `Player`, `NPC`)
      - `terrain.Terrain` (example: `Tree`, `Rock`) with half-extents metadata
      - `item.Item` (example: `Key`, `Pickaxe`)
  - These classes contain only state and logic. No engine imports (no jME types).

- Engine layer (do not modify unless instructed)
  - AppStates
    - `InputAppState` — centralizes input mappings and exposes input state.
    - `WorldAppState` — builds the world (simple voxel world), manages lights, handles voxel edits.
    - `PlayerAppState` — first-person character controller (physics + camera) and a head-following light.
    - `RenderAppState` — renders student `GameObject`s using simple placeholder meshes, keeps them in sync; cleans up visuals when objects are removed from the registry.
    - `InteractionAppState` — raycasts from the camera and routes interactions to student items; also locates voxel blocks for a student exercise.
    - `HudAppState` — minimal GUI overlay (crosshair).
  - Utilities
    - `GameRegistry` — engine-side registry of student objects to render/interact with.
    - `RenderIndex` — mapping from jME `Spatial` back to `GameObject` for picking/hit-tests.
    - `voxel.*` — voxel world representation, block palette, and mesh generation (engine-internal).
      - Voxel types are class-based (`VoxelBlockType`); the default palette auto-registers `AirBlockType` (id 0, not solid) and `StoneBlockType` (id 1, solid). Register more types via `voxelWorld.getPalette().register(new YourBlockType())`.
    - `util.ProcTextures` — procedural textures for engine visuals.

- Boundary
  - The engine layer uses jME3 classes. The student layer does not.
  - Engine AppStates adapt your data to visuals/physics/interaction.

---

## Controls (runtime)

- Mouse capture: Tab (cursor hidden when captured)
- Move: W/A/S/D
- Sprint: Left Shift
- Jump: Space (requires being on the ground)
- Interact: E (raycasts from camera; calls `Item.onInteract()`; if no item is hit, it finds a voxel block and leaves a TODO for students)
- Break voxel: Left Mouse Button
- Respawn: R (warp to safe spawn)
- Render toggles: L (toggles Shading/Lit ↔ Unshaded, Wireframe On/Off, Culling On/Off)
  - Defaults at startup: Shading=On, Wireframe=Off, Culling=On
- ESC: Quit (default jME behavior)

---

## Engine Architecture

- AppStates orchestrate the engine: initialize → update → cleanup.
- Physics uses `BetterCharacterControl` from Bullet/Minie (gravity, walk, jump).
- Voxel world
  - A compact 16×16×16 grid provides a simple surface.
  - The initial scene includes exactly one Stone block under the player so the project starts bare‑bones.
  - Voxel types come from a palette of `VoxelBlockType` instances (default: Air, Stone). Instructors/students can add types (e.g., Dirt, Wood) by registering them in the palette before mesh building.
- Rendering and interaction for your objects are provided by two layers:
  - `RenderAppState` creates placeholder geometries (cylinders for characters, boxes for terrain/items), registers them in `RenderIndex`, syncs positions from your `GameObject.position`, and removes visuals for objects dropped from the registry.
  - `InteractionAppState` listens for Interact (E), first tries to resolve a rendered `GameObject` (e.g., `Item`) under the crosshair; if none is hit, it finds a voxel block and prints a TODO—this is the extension point where students can implement voxel interactions (toggle/place/inspect).

Notes:
- The placeholder meshes/colors are defaults; later you can extend `RenderAppState` with adapters (e.g., custom models per class/interface).

---

## Student Layer: How To Extend

- Create new game objects under `jogo.gameobject.*` with no engine imports.
  - Examples:
    - `class Slime extends Character { ... }`
    - `class Tree extends Terrain { ... }`
    - `class Key extends Item { @Override public void onInteract() { /* add logic */ } }`

- Store just data and game logic
  - Examples: health, inventory, states (idle/attacking), cooldown timers.
  - Use `getPosition()/setPosition()` to manage logical positions.

- Interaction
  - Implement `Item.onInteract()` for items you want the E key to affect.
  - For voxel interaction, open `InteractionAppState` and implement the TODO where the engine reports the targeted voxel coordinates.

- Rendering
  - The engine renders a generic shape for each type (box/cylinder). You don’t add any jME code.
  - To change visuals in the future, add an adapter in `RenderAppState` (engine side).

- Voxel types (advanced)
  - Add a new block type by subclassing `VoxelBlockType` (e.g., `DirtBlockType`) and registering it: `byte DIRT_ID = voxelWorld.getPalette().register(new DirtBlockType());`
  - Assign cells in the world to your new type’s ID before rebuilding meshes/physics.

---

## Registering Objects for Rendering/Interaction

Engine wiring is done in `Test.simpleInitApp()`. To add objects you want to see and interact with (non-voxel props):

```java
// Create the shared engine registry
GameRegistry registry = new GameRegistry();
RenderIndex renderIndex = new RenderIndex();
stateManager.attach(new RenderAppState(rootNode, assetManager, registry, renderIndex));
stateManager.attach(new InteractionAppState(rootNode, cam, input, renderIndex, world));

// Example: add your own engine-neutral objects
// MyProp prop = new MyProp(/* sizes, etc. */);
// prop.setPosition(…);
// registry.add(prop);

// MyItem item = new MyItem();
// item.setPosition(…);
// registry.add(item);
```

These examples are illustrative—the default scene starts bare‑bones with only the voxel world. When you add your own objects to the registry, `RenderAppState` will draw them and `InteractionAppState` will allow interacting with items via E.

---

## Running and Building

Windows (cmd):

```bat
gradlew.bat run
```

Build distributable packages (without tests):

```bat
gradlew.bat build -x jogo
```

> Dependencies (jME3, Minie, Lemur) are managed by Gradle; nothing else required.

---

## FAQ / Common Pitfalls

- I can’t move the camera
  - Press Tab to capture the mouse.
- I can’t interact with items
  - Look directly at the item (crosshair over it) and press E while the mouse is captured.
- I want to interact with voxels
  - Implement the TODO in `InteractionAppState` where it prints the voxel coordinates.
- I added an object but don’t see it
  - Ensure you added it to the `GameRegistry`; the renderer only draws registered objects. To remove it at runtime, call `registry.remove(obj)`—its visual will be detached automatically.
- Everything is dark
  - Press L to toggle to Unshaded + Wireframe + Culling Off, or rely on the player’s local light.

---

## Contributing Engine Improvements

If you modify the engine, prefer new AppStates or utilities instead of changing student-facing classes. Keep student classes engine-neutral.

Recommended areas for improvement:
- Chunked voxel meshing and physics for performance.
- Type‑driven visuals: per‑type materials/factories for `VoxelBlockType`.
- Removal/cleanup flows in `RenderAppState` are implemented; extend them if you add pooling or instancing.
- A richer adapter layer for visuals (models, textures, animations).
- Interaction prompts and UI feedback.
- Shadow mapping and skybox.

Keep PRs small and documented.
