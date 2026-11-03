# Noob_launchar — config schema

One JSON file per version, same shape everywhere:

- 1.8.9 Forge: `config/nooblaunchar.json` (in the game directory)
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
  "gammaOverrideActive": false
}
```

## Fields

- `modules` — map from module id (see `MODULES.md`) to its settings.
  Missing modules/fields fall back to defaults; unknown ids are ignored.
- Per module:
  - `enabled` — module on/off. Defaults: all on except `toggle_sprint`
    and `fullbright` (off).
  - `x`, `y` — HUD position in GUI-scaled pixels. `-1` = use the module's
    default anchor. Set by the Edit HUD mode; reset returns them to `-1`.
  - `color` — text color as an RGB int (alpha forced opaque at render).
  - `background` — whether text-style HUD elements draw their translucent
    background panel.
  - `options` — module-specific option values (see `MODULES.md`):
    `fps.label` (On/Off), `coords.biome` (On/Off),
    `armor.style` (Percent/Remaining/Bar only), `clock.format` (24h/12h),
    `zoom.factor` (2x/3x/4x). Missing = first declared value.
- `baseFov` / `fovOverrideActive` — while Zoom holds an FOV override, the
  user's own FOV is stored here so it can be restored exactly — including
  after an unclean shutdown (repaired on the next launch's first tick).
- `baseGamma` / `gammaOverrideActive` — same pattern for Fullbright's
  brightness override.

The file is loaded on startup and rewritten on every change (toggle,
option cycle, drag end, override capture/restore). A corrupt file falls
back to defaults instead of crashing the client.
