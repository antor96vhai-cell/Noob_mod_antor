# Noob_mod_antor — menu & HUD design notes

> Renamed from **Noob_launchar** to **Noob_mod_antor** on 2026-11-03.

Original design (no third-party client assets, branding or layouts are
copied — only the generic feature ideas: module list, toggles, HUD editor).

## Palette (ARGB)

| Role | Value | Use |
|------|-------|-----|
| Panel background | `0xE60C1015` | Menu/settings panels (dark blue-grey, ~90% opaque) |
| Panel border | `0xFF2B3442` | 1px panel and chip outlines |
| Accent | `0xFF2DD4BF` | Title text, ON pills, HUD-edit outlines, title rule |
| Text | `0xFFF2F5F7` | Primary labels |
| Text dim | `0xFF9AA7B4` | Subtitles, hints, disabled module names |
| Row hover | `0x33FFFFFF` | Module row hover wash |
| Pill off | `0xFF3A4353` | Toggle track when off |
| Chip / button bg | `0xFF1B222C` | Value chips, footer buttons |
| Button hover | `0xFF27313F` | Footer button hover |
| HUD element bg | translucent dark (`0x90101418` in 26.2; alpha ~0.55 black in 1.8.9) | Behind HUD text elements when the module's background setting is on |
| Keystrokes box | dark fill; pressed = module color with dark text | Key boxes |

## Layout

- **Main menu:** centered panel, 320px wide, 16px from top/bottom of the
  screen. Header: accent title "Noob_mod_antor", dim subtitle, 1px accent
  rule. Module rows 24px high: name left, ">" settings affordance, ON/OFF
  pill (30×14, right). List scrolls (mouse wheel) inside a scissored/clipped
  region. Footer: "Edit HUD" and "Done" buttons (120×22).
- **Settings page:** centered panel 300px wide, height fits its rows.
  "< Back" top-left, module name as title. Rows 24px: Enabled pill; Text
  color (value chip cycles the presets); Background pill (when the module
  supports it); one row per module option (chip cycles values); HUD
  position Reset row for HUD modules. Footer "Back" button.
- **Edit HUD:** screen dimmed ~40%; instruction lines at top; each enabled
  HUD element drawn live with a 1px accent outline and its name above it;
  drag with the mouse; "Reset All" and "Done" buttons bottom-center.
  Positions clamp to the screen and save on mouse release.
- **HUD text elements:** 4px horizontal / 3px vertical padding, 10px line
  step, module text color (default white).

## Text color presets (cycle order)

White `FFFFFF` → Teal `2DD4BF` → Yellow `FFFF55` → Aqua `55FFFF` →
Green `55FF55` → Pink `FF55FF` → Orange `FFAA00` → Red `FF5555` →
Gray `AAAAAA` → back to White.
