# Noob_mod_antor — config schema

> Renamed from **Noob_launchar** to **Noob_mod_antor** on 2026-11-03.

One JSON file per version, same shape everywhere:

- 1.8.9 Forge: `config/noob_mod_antor.json` (in the game directory).
  If that file does not exist but the pre-rename `config/nooblaunchar.json`
  does, it is loaded once as a fallback and re-saved under the new name,
  so existing settings survive the rename.
- 26.2 Fabric: `config/kutuclient.json` (legacy filename from before the
  rename; same schema)

```json
{
  "modules": {
    "<module id>": {
      "enabled": true,
      "x": -1,
      "y": -1,
      "color": 16777215,
      "background": true,
      "options": { "<option key>": "<value>" }
    }
  },
  "baseFov": null,
  "fovOverrideActive": false,
  "baseGamma": null,
  "gammaOverrideActive": false,
  "globalBgMode": "Box",
  "globalBgOpacity": 56,
  "globalBgColor": 1053720,
  "globalTextColor": 3008703,
  "globalChroma": false,
  "entityRenderDistance": 0,
  "dynamicFps": false,
  "dynamicFpsCap": 30
}
```

## Fields

- `modules` — map from module id (see `MODULES.md`) to its settings.
  Missing modules/fields fall back to defaults; unknown ids are ignored.
- Per module:
  - `enabled` — module on/off. Defaults: all on except `toggle_sprint`,
    `fullbright`, `toggle_sneak`, `time_changer`, `nick_hider`, `fov`,
    `entity_size`, `weather` and `hitbox` (off).
  - `x`, `y` — HUD position in GUI-scaled pixels. `-1` = use the module's
    default anchor. Set by the Edit HUD mode; reset returns them to `-1`.
  - `color` — text color as an RGB int (alpha forced opaque at render).
  - `background` — whether text-style HUD elements draw their translucent
    background panel.
  - `options` — module-specific option values (see `MODULES.md`):
    `fps.label` (On/Off), `coords.biome` (On/Off),
    `armor.style` (Percent/Remaining/Bar only), `clock.format` (24h/12h),
    `zoom.factor` (2x/3x/4x). Missing = first declared value.
    Added in 1.1.0 (1.8.9): `crosshair.size` / `crosshair.gap`
    (Small/Medium/Large), `crosshair.dot` (On/Off),
    `crosshair.vanilla` (Hidden/Shown), `scoreboard.display` /
    `bossbar.display` (Show/Hide), `time_changer.time`
    (Day/Noon/Sunset/Night/Midnight), `combo.best` (Off/On),
    `fov.fov` (slider value stored as a number string, "30"–"110";
    absent until the FOV module first runs, when it seeds itself from
    the player's current FOV), and `nick_hider.nick` (free text,
    default "Player"). All remain plain string values in the same
    `options` map, so older configs load unchanged.
- `baseFov` / `fovOverrideActive` — while Zoom holds an FOV override, the
  user's own FOV is stored here so it can be restored exactly — including
  after an unclean shutdown (repaired on the next launch's first tick).
- `baseGamma` / `gammaOverrideActive` — same pattern for Fullbright's
  brightness override.
- Appearance globals (added in 1.1.0 round 2; missing fields in older
  configs keep the defaults shown above, so old saves load unchanged):
  - `globalBgMode` — default HUD background mode: `Clear` / `Box` /
    `Custom`.
  - `globalBgOpacity` — default background opacity, percent 0–100.
  - `globalBgColor` — default background RGB int for Custom mode.
  - `globalTextColor` — global text/accent RGB int; the menu accent
    follows this (or chroma, below).
  - `globalChroma` — global chroma (rainbow text/accent) default.
  - Per-module appearance overrides live in the module's `options`
    map: `apx.bg_mode` (Clear/Box/Custom), `apx.bg_opacity` (percent
    as a number string), `apx.bg_color` (RGB int as a decimal string),
    `apx.chroma` (On/Off). Unset = inherit the global default. The
    legacy per-module `background` boolean is kept in sync with the
    mode (false ⇔ Clear) for older readers.
- `entityRenderDistance` — Performance page setting: skip rendering
  living entities farther than this many blocks; `0` = unlimited.
  Applies when the Smart Culling module is disabled; while Smart
  Culling is enabled, its own entity-distance slider wins.
- `dynamicFps` / `dynamicFpsCap` — Performance page Dynamic FPS:
  while the window is unfocused, the framerate limit is temporarily
  set to `dynamicFpsCap` (in memory only, never written to the
  vanilla options file) and the user's own limit is restored on
  focus.

Module options added in 1.1.0 round 3 (1.8.9): `chat_tweaks.compact`
(On/Off), `nametag.scale` (slider string "0.5"–"2.0"),
`particles.crit` (slider string "0.5"–"3"), `particles.density`
(Vanilla/Reduced/Minimal), `sound_tweaks.click` / `.levelup` /
`.explosion` / `.hurt` (volume percent slider strings "0"–"200"),
`smart_culling.entity_distance` ("0"–"256"),
`smart_culling.tile_distance` ("0"–"192"). The Behind You (F8) and
Copy Chat (F7) keys are vanilla KeyBindings like the others.

Module options added in 1.1.0 round 2 (1.8.9), same plain-string
`options` map: `coords.compass` (Off/On), `potions.style`
(Icon+Text/Text only/Icon+Duration), `potions.duration` (On/Off),
`fov.dynamic_fov` (On/Off), `fov.lock_fov` (Off/On),
`entity_size.player_scale` / `entity_size.mob_scale` (slider strings
"0.5"–"2.0"), `animation.speed` ("0.25"–"3"),
`weather.mode` (Server default/Always Clear/Always Rain),
`freecam.speed` (slider string "2"–"40"), `held_item.scale`
("0.5"–"1.5"), `name_highlight.words` (free text, comma-separated),
`name_highlight.sound` (Off/On). The Freelook and Freecam keybinds
(and the menu/zoom keys) are vanilla KeyBindings, persisted by the
game itself in `options.txt` and rebindable in Controls.

The file is loaded on startup and rewritten on every change (toggle,
option cycle, drag end, override capture/restore). A corrupt file falls
back to defaults instead of crashing the client.
