# Noob_launchar — canonical module list

Every version of the client (1.8.9 Forge, 26.2 Fabric, and future ports)
implements the same module set with the same ids, so one config file shape
works everywhere. The mod is a pure in-game HUD/menu mod: **no account,
login, authentication or ownership features exist in any version.**

| # | Module id | Display name | Default | HUD | Notes |
|---|-----------|--------------|---------|-----|-------|
| 1 | `fps` | FPS Counter | on | yes | Client FPS. Option: show "FPS" label (On/Off). |
| 2 | `cps` | CPS Counter | on | yes | Left \| right clicks/sec, rolling 1-second window. |
| 3 | `keystrokes` | Keystrokes | on | yes | W A S D, LMB, RMB, Space boxes; pressed keys light up in the module color. Labels follow the user's key bindings. No background setting (the boxes are the background). |
| 4 | `coords` | Coordinates | on | yes | XYZ, facing direction + axis hint, biome name. Option: show biome (On/Off). |
| 5 | `armor` | Armor Status | on | yes | Helmet, chestplate, leggings, boots, main-hand item; icon + durability. Option: Percent / Remaining / Bar only. |
| 6 | `potions` | Potion Effects | on | yes | Active effects: color dot, name, amplifier, duration (m:ss or ∞). |
| 7 | `ping` | Ping | on | yes | Multiplayer latency from the player-list entry; shows `--` in singleplayer. |
| 8 | `clock` | Clock | on | yes | Real-world time. Option: 24h / 12h format. |
| 9 | `toggle_sprint` | Toggle Sprint | **off** | no | Keeps the player sprinting while moving forward, under vanilla's own conditions (food/creative, not using an item, not sneaking). Never modifies the sprint key binding; off = pure vanilla. |
| 10 | `zoom` | Zoom | on | no | Hold the zoom key (default C): smooth FOV zoom with mouse-sensitivity scaling; the user's exact FOV/sensitivity are restored on release. Option: factor 2x / 3x / 4x. |
| 11 | `fullbright` | Fullbright | **off** | no | Maximum-brightness rendering while on; the user's exact previous brightness is restored when switched off. |

## Menu behaviour (all versions)

- **Right Shift** opens the main menu: scrollable module list, ON/OFF pill
  per module, per-module settings page (enabled, text color, background,
  module options, HUD position reset), and an **Edit HUD** mode where
  enabled HUD elements are dragged with the mouse.
- Changes save immediately to the version's config file
  (see `CONFIG_SCHEMA.md`).
- HUD elements have sensible default anchors (top-left stack for
  FPS/CPS/Coords/Ping, top-right Clock/Potions, bottom-right Keystrokes,
  beside the hotbar for Armor) until the user drags them.
