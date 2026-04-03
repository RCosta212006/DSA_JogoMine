#  Iscte_Craft  

## voxel-based sandbox game developed as a project to explore and apply Object-Oriented Programming (OOP) principles in Java. 

**Note on Project Scope:** The underlying 3D rendering engine and the base empty world/character were provided to us as a starting foundation. **Everything else**—from the user interface, block interactions, inventory, crafting, enemy AI, to the high-score system—was built entirely by us from scratch.

## Features

Starting with a completely empty world and a basic character controller, we engineered the following systems:

### 1. Advanced Block & Voxel System
We populated the world with various interactable block types, each with unique properties utilizing OOP interfaces (e.g., `Unbreakable`, `ReducedMoveSpeed`):
* **Standard Blocks:** Dirt, Grass, Stone, Wood, Leaves.
* **Ores:** Coal, Iron, Gold, and Diamond.
* **Special/Hazard Blocks:** * **Bedrock & Barriers:** Indestructible boundaries (`Unbreakable`).
  * **Magma:** Deals damage upon contact.
  * **Quicksand:** Slows down the player's movement (`ReducedMoveSpeed`).

### 2. User Interface (UI) & HUD
We built a comprehensive, state-driven UI system (via AppStates):
* **Main Menu:** Navigable starting screen.
* **HUD:** Real-time health container (hearts), score tracking, and an active Hotbar.
* **Inventory & Crafting Screens:** Interactive menus to manage collected items and craft new ones.
* **Game Over Screen:** Triggers upon player death, displaying final stats.

### 3. Inventory & Crafting System
* **Item Management:** Picking up dropped blocks/items and storing them in a dynamic inventory.
* **Crafting Logic:** A complete `CraftingManager` that checks `CraftingRecipe` requirements. Players can craft Wood Planks, Sticks, and various tiers of Pickaxes (Wood, Stone, Iron, Diamond).
* **Tool Tiers:** Different pickaxes have different mining speeds and capabilities based on inheritance structures.

### 4. Entity & AI System (NPCs & Mobs)
We populated the empty world with a rich ecosystem of characters using deep class inheritance (`Character` -> `NPC` / `Enemy` / `Player`):
* **Hostile Mobs:** Zombies and Spiders that track and attack the player.
* **Friendly/Neutral NPCs:** Villagers and Ocelots.
* **Advanced Behaviors:** Implemented traits like `Follower` mechanics and `AbleToTeleport` (e.g., for certain mobs/pets).

### 5. Progression & Persistence
* **Score System:** Players earn points for mining rare ores and surviving.
* **High Scores:** A `HighScoreManager` that reads/writes to `highscores.txt`, saving top `ScoreEntry` records between game sessions.

## Object-Oriented Design 

This project was heavily driven by OOP best practices:
* **Inheritance:** Extensively used in our Entity system (e.g., `Zombie` extends `Enemy` which extends `Character`) and Item system (`WoodPickaxe` extends `Pickaxe` extends `ToolItem`).
* **Interfaces:** Used to attach specific behaviors to blocks and entities without duplicating code (e.g., `AbleToTeleport`, `Unbreakable`).
* **Encapsulation:** Safe data handling across the `Inventory`, `HotBar`, and `Health` systems.
* **Modularity (AppStates):** Separated game logic into distinct managers (`InteractionAppState`, `NPCAppState`, `WorldAppState`) making the codebase highly scalable and clean.

## How to Run

This project uses Gradle for dependency management and building. 

**Prerequisites:** * Java Development Kit (JDK) installed (Java 11 or higher recommended).

**Running the game via terminal/command prompt:**
1. Clone the repository.
2. Navigate to the root directory.
3. Run the following command:

**On Windows:**
```
gradlew.bat run
```

**On Mac/Linux:**
```
./gradlew run
```

Credits
Engine & Base Renderer: Provided as project boilerplate.

Gameplay, UI, AI, and Logic: Rodrigo Costa & Afonso Aguilar.
