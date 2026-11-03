# Noob_launchar

A Lunar-style in-game client for Minecraft — Right Shift mod menu, draggable HUD editor, and the classic PvP HUD modules — built as plain mods with our own design. No login tricks, no cracks: just drop the jar in your mods folder.

## Versions

| Minecraft | Loader | File |
|---|---|---|
| 1.8.9 | Forge 11.15.1.2318 | `versions/1.8.9-forge/Noob_launchar-1.0.0.jar` |
| 26.2 | Fabric | `versions/26.2-fabric/Noob_launchar-1.0.0_mc26.2.jar` |

More versions can be added later — each lives in its own `versions/<mc>-<loader>/` folder and follows the shared spec in `shared/` (module list, design, config schema).

## Modules

Right Shift menu with per-module settings and a HUD editor (drag elements where you want them). Settings save to `config/nooblaunchar.json`.

FPS · CPS · Keystrokes · Coordinates + direction + biome · Armor status · Potion effects · Ping · Toggle Sprint · Zoom · Clock · Fullbright

## Install

1. Install the matching loader (Forge 1.8.9 or Fabric 26.2) in your launcher.
2. Put the jar in the `mods` folder.
3. Launch the game and press **Right Shift**.

## Build

Each version folder has its own `build.sh` / build notes. See `BUILD_NOTES.md` for the full story, including the manual 1.8.9 pipeline.
