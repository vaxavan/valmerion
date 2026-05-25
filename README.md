# Valmerion

A LibGDX side-scrolling RPG prototype. Features a main menu, a 4-slide
cutscene and a full combat tutorial set in a medieval academy.

## Project structure

```
valmerion/
├── core/                   ← game logic (screens, entities, ui, assets)
│   └── src/main/java/com/valmerion/
│       ├── ValmerionGame.java          entry point
│       ├── assets/AssetLoader.java     centralised asset registry
│       ├── screens/
│       │   ├── BaseScreen.java
│       │   ├── MenuScreen.java
│       │   ├── CutsceneScreen.java
│       │   ├── AcademyScreen.java      tutorial gameplay
│       │   ├── SettingsScreen.java
│       │   └── CongratulatoryScreen.java
│       ├── entities/
│       │   ├── Entity.java
│       │   ├── AnimationSet.java
│       │   ├── Player.java
│       │   └── Goblin.java
│       └── ui/
│           ├── MenuButton.java
│           ├── HintOverlay.java
│           └── HealthBar.java
├── lwjgl3/                 ← desktop launcher
└── assets/                 ← game assets (place your files here)
```

## Running

```bash
./gradlew lwjgl3:run
```

Requires **Java 17+**. On Windows use `gradlew.bat`.

## Asset placement guide

Place your image and audio files exactly at the paths listed below
(relative to the `assets/` directory).

### Menu

| File | Description |
|------|-------------|
| `textures/menu/menu_bg.png` | Main menu background — dark ruins with golden light |
| `textures/menu/btn_new_game.png` | "NEW GAME" gold runic button (normal) |
| `textures/menu/btn_new_game_hover.png` | "NEW GAME" hover state (can be same file) |
| `textures/menu/btn_settings.png` | Settings button — wooden board + gear |
| `textures/menu/btn_exit.png` | Exit button — stone "Continue" plate |

### Academy (tutorial gameplay)

| File | Description |
|------|-------------|
| `textures/academy/academy_bg.png` | Training hall — arches, banners, dummies, weapons |

### Cutscene slides (shown in order before the academy)

| File | Description |
|------|-------------|
| `textures/cutscene/slide_01.png` | Dark forest with purple flowers & broken wagon |
| `textures/cutscene/slide_02.png` | Medieval city at night (the kingdom) |
| `textures/cutscene/slide_03.png` | Cave/dungeon with skeletons and chains |
| `textures/cutscene/slide_04.png` | Wizard's study / library |

### Characters (sprite atlases)

When you have sprite sheets, pack them with
[libGDX Texture Packer](https://libgdx.com/wiki/tools/texture-packer)
and place the output here.

| Atlas file | Region prefix | Animations expected |
|------------|---------------|---------------------|
| `textures/characters/player/player.atlas` | `player_` | `player_idle`, `player_walk`, `player_jump`, `player_fall`, `player_attack`, `player_hit`, `player_dead` |
| `textures/characters/goblin/goblin.atlas` | `goblin_` | `goblin_idle`, `goblin_walk`, `goblin_attack`, `goblin_hit`, `goblin_dead` |

### Audio *(optional — game runs without audio)*

| File | Description |
|------|-------------|
| `audio/music/menu.ogg` | Menu background music |
| `audio/music/academy.ogg` | Academy background music |
| `audio/sfx/btn_click.ogg` | Button click sound |
| `audio/sfx/jump.ogg` | Player jump |
| `audio/sfx/attack.ogg` | Player attack |
| `audio/sfx/hit.ogg` | Enemy hit |

### Fonts *(optional — falls back to libGDX default)*

| File | Used for |
|------|----------|
| `fonts/main.ttf` | UI text, hints, subtitles (24 px) |
| `fonts/title.ttf` | Large headings (52 px) |

## Controls

| Key | Action |
|-----|--------|
| A / ← | Move left |
| D / → | Move right |
| SPACE | Jump (unlocked mid-tutorial) |
| F / Z | Attack (unlocked mid-tutorial) |
| ESC | Back / skip cutscene |
| SPACE / ENTER | Skip cutscene |

## Tutorial flow

```
Menu → [New Game] → Cutscene (4 slides) → Academy tutorial
  1. Walk (A/D) for 80+ pixels
  2. Jump (SPACE) — unlocked
  3. Attack (F) — unlocked
  4. Defeat static goblin
  5. Defeat aggressive goblin
  6. Congratulations screen → Menu
```
