# Course Project Plan (5 Weeks)

This project uses a small engine shell (see `docs/ENGINE.md`) to give you a playable sandbox while you practice core OO design by building a small game. Your focus is to design and implement a clean object model (classes, hierarchies, interfaces, abstract classes, overriding), and give core gameplay pillars:

1) Terrain with multiple voxel/material types
2) World objects (items/props) with meaningful interactions
3) Crafting (recipes and inventory)
4) NPCs (friendly and enemies) with simple AI
5) Terrain generation (with structures and randomness)

Each pillar has objective, gradeable criteria below.

You may use an LLM for **narrowly scoped tasks under strict rules** (see *AI Use Policy*). You **must document** every LLM use in a separate log.

---

## Weekly Overview

- Week 1 — Foundations: Classes, Objects, Encapsulation, Project Setup
- Week 2 — Hierarchies & Voxel Types: Inheritance, Interfaces, Multiple Materials
- Week 3 — Systems: Crafting + Inventory + Basic NPCs, Terrain Generation (stub)
- Week 4 — Polymorphism, AI & Error Handling: Friendly/Enemy behaviors, Recipes & Persistence, Gen pass 2
- Week 5 — Integration & Quality: Balance, Performance
- Week 5 — Polishing, Final Demo

Each week has learning goals, activities, deliverables, and rubric emphasis. Objective criteria for pillars are spelled out below.

---

## Objective, Gradeable Criteria (Pillars)

A) Terrain / Voxel Block Types (20%)
- At least 4 voxel block types total (not including `Air` and `Stone` defaults; add ≥4 new types such as Wood/Sand/etc).
- Each type must specify its own material/visual (distinct color/texture) and solidity (isSolid).
- There should be at least one functional distinction between each terrain type (An example would be gravity, altering player speed, etc)
- Types registered via `voxelWorld.getPalette().register(new YourBlockType())` and used in world data.

B) Objects & Interactions (20%)
- At least 5 distinct GameObjects (mix of Item/Prop/Character subclasses) with clear responsibilities.
- At least 3 objects must expose interactions (E) producing visible/logged effects or state changes.
- Encapsulation: fields private/protected, clear APIs, Javadoc on public members.

C) Crafting & Inventory (20%)
- Inventory structure supporting at least 6 slots (or a reasonable capacity you justify).
- At least 4 recipes (e.g., combining two items yields another item); recipes documented.
- A simple crafting flow (key‑driven, menu, or console) that validates inputs and outputs results; error handling for invalid recipes.

D) NPCs (Friendly/Enemy) (20%)
- At least 2 friendly NPCs with one behavior (e.g., follow player, trade/log message, simple dialogue).
- At least 2 enemy NPCs with one behavior (e.g., patrol/chase/attack tick that logs or affects player state).
- NPC logic expressed via interfaces/abstract classes and overridden methods (e.g., `updateAI()` or state machine methods), showing polymorphism.

E) Terrain Generation (rudimentary) (20%)
- Automated terrain population using a simple algorithm (e.g., heightmap, noise, or layered rules) that places multiple voxel types.
- (Optional) Generation must be parameterized (constants or configuration) and deterministic for the same seed.
- Code structured for readability (helpers, small functions), with comments explaining the approach.

Note: Total adds to 100%. Instructor can adjust weights if needed but should keep objective criteria intact.

---

## Week 1 — Foundations

Learning goals
- Understand the engine boundary (engine vs. game objects) and project build/run flow.
- Define simple classes with fields/methods, constructors, and encapsulation.
- Use composition sensibly (a class as a field of another class).

Activities
- Read `docs/ENGINE.md` (Big Picture, Controls, Registering Objects, Voxel palette).
- Build and run:

  ```bat
  gradlew.bat run
  ```
  - Or in IntelliJ:
    - Open the project.
    - Configure the JDK (17+).
    - Run the `run` configuration.
<br />
<br />
- Create 2–3 concrete classes under `jogo.gameobject.*` (engine‑neutral):
  - Example: `Key`, `Door`, `Tree`, `Slime`.
  - Add fields, constructors, getters/setters, `toString()`, and (if useful) `equals()/hashCode()`.
- Register 1 object in `Test.simpleInitApp()` via `GameRegistry` (see ENGINE guide) and verify it renders.

Deliverables
- New classes with Javadoc on each public type and method.
- A brief UML sketch showing classes and their fields/relationships.
- Short note in `docs/` explaining your object choices and responsibilities.

Rubric emphasis (W1)
- Class design clarity — 40%
- Encapsulation & API quality — 40%
- Build/run hygiene — 20%

---

## Week 2 — Hierarchies & Voxel Types

Learning goals
- Apply inheritance and interfaces; prefer composition unless a clear hierarchy adds value.
- Extend voxel palette with multiple block types and materials.

Activities
- Extend the hierarchy provided (`GameObject` → `Character`, `Terrain`, `Item`) with 1–2 subclasses.
- Add ≥2 new `VoxelBlockType`s (e.g., Dirt, Wood) and register them in the palette.
- Assign these types to some world cells and rebuild voxel meshes/physics; make types visually distinct.

Deliverables
- Updated UML showing hierarchy and overridden methods.
- Code that registers and uses the new voxel types (screenshots or brief video acceptable).

Rubric emphasis (W2)
- Hierarchy soundness & naming — 40%
- Voxel types (≥2 new), materials & usage — 60%

---

## Week 3 — Systems: Crafting, Inventory, NPCs, Generation (stub)

Learning goals
- Design interfaces to express capabilities; use abstract classes when sharing state + partial behavior makes sense.
- Implement a minimal crafting/inventory system.
- Introduce basic NPCs; begin terrain generation.

Activities
- Define at least one capability interface (e.g., `Interactable`, `Craftable`, `Tickable`) and one abstract base (shared fields/partial behavior).
- Implement inventory + crafting flow with ≥2 recipes this week (target ≥4 total by Week 5).
- Add at least 1 friendly and 1 enemy NPC with a basic `updateAI()` loop (log or change state) and bind it in a tick (e.g., simple logic AppState or manual update calls for now).
- Begin terrain generation: a basic height function or perlin‑like stub that places at least 2 voxel types.

Deliverables
- UML updated to include interfaces and abstract classes.
- Demo of crafting 1–2 recipes and NPC behavior (log or visuals).

Rubric emphasis (W3)
- Interfaces/abstract classes design — 35%
- Crafting & inventory (initial) — 35%
- NPCs (initial) — 15%
- Generation (stub) — 15%

---

## Week 4 — Polymorphism, AI & Error Handling

Learning goals
- Use polymorphism over interfaces/abstract types instead of concrete ones.
- Evolve AI with simple state machines or strategies; distinguish friendly/enemy roles.
- Harden crafting with clear contracts and error handling.

Activities
- Implement a small AI state machine for enemies (e.g., Idle → Chase → Attack) and a behavior for friendlies (e.g., Follow/Interact).
- Expand crafting to ≥4 recipes; persist inventory/crafting results over a short session if possible.
- Improve terrain generation (second pass): add a rule/biome distinction or parameter toggles, still simple/fast.
- Add small tests/harnesses to validate contracts and polymorphic behavior.

Deliverables
- Demo: enemies exhibit at least two states and a transition; friendlies perform a distinct behavior.
- Tests/harnesses showing polymorphism and error cases.

Rubric emphasis (W4)
- Polymorphism & contracts — 30%
- AI behaviors & correctness — 30%
- Crafting (recipes ≥4, error handling) — 20%
- Generation (second pass) — 20%

---

## Week 5 — Integration, Quality, and Demo

Learning goals
- Integrate the pillars into a coherent loop; polish and balance.
- Refactor to improve clarity, performance, and maintainability.

Activities
- Finalize features; ensure E (interact) and movement/jump work.
- Balance materials, crafting outputs, and NPC difficulty.
- Refactor: remove duplication, tighten visibility, complete docs.
- Prepare a 3–5 minute walkthrough: show your object model, voxel types, crafting, NPCs, terrain gen, and a short in‑engine demo.

Deliverables
- Final engine‑neutral classes with complete Javadoc (class/methods).
- Updated UML (final) and a short architecture note (1–2 pages) on design decisions/trade‑offs.
- AI usage log committed to a document.

Rubric emphasis (W5)
- Integration coherence & feature completeness — 40%
- Code clarity & documentation — 40%
- Demo readiness & professionalism — 20%

---

## Submission Checklist

- [ ] Code builds and runs.
- [ ] Voxel types: ≥6 total (Air, Stone + ≥4 new) with distinct visuals, functionality and solidity
- [ ] Objects: ≥5 distinct, ≥3 interactable (E)
- [ ] Crafting & inventory: ≥4 recipes, error handling for invalid inputs
- [ ] NPCs: ≥2 friendly, ≥2 enemies; basic behaviors implemented
- [ ] Terrain generation: deterministic, uses ≥4 voxel types, parameters documented
- [ ] Student classes: engine‑neutral, documented
- [ ] UML diagrams (initial → final) in `docs/`
- [ ] Tests/harness for polymorphism/contracts (Week 4)
- [ ] Final write‑up (Week 5)
- [ ] AI usage log with prompts, outputs, reflection, and commit references

---

## AI Use Policy (Read Carefully)

What’s **allowed** (with documentation):
- Boilerplate generation: getters/setters, equals/hashCode/toString, simple DTOs.
- Clarifying questions about language or library syntax.
- Brainstorming edge cases or jogo scenarios.
- Producing first‑draft comments/Javadoc that you then review and edit.
- Specific algorithms to save work.

What’s **not allowed**:
- “Catch‑all” prompts (e.g., *"Build my whole project/game"*).
- Blindly pasting/generated code without review or understanding.
- Replacing your design effort (e.g., asking an LLM to pick your entire hierarchy/contracts).
- Using LLMs for code you cannot later explain or defend.

Documentation requirements (mandatory)
- Keep a running log in a document with entries:
  - Context (what you were working on and why you needed help)
  - Prompt (exact text)
  - Output (trimmed for relevance)
  - Decision (what you used, what you changed, what you rejected)
  - Reflection (1–3 sentences on what you learned and risks mitigated)
- If you use line-completion, be ready to explain why and what was added, and perhaps document everything.

Quality bar for AI‑assisted content
- You own the code and its correctness. Review, jogo, and rewrite as needed.
- AI output must match your chosen design, naming, and conventions.
- If the instructor asks, you must be able to explain any AI‑assisted code in detail.

Academic integrity
- Failure to document AI use may be treated as an integrity violation.
- Massive AI‑authored content that you cannot explain will be **disallowed and your grade will be 0**.

---

## Tips and Resources

- Prefer small, purposeful interfaces and composition over deep inheritance.
- Use clear names and keep public APIs small; hide internals.
- Sketch UML before coding; update it as designs evolve.
- Tests don’t need to be heavy—small harnesses that print and assert invariants are fine.
- Keep your weekly scope tight; ship something small but complete each week.
