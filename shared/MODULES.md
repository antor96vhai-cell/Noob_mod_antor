# Noob_mod_antor — canonical module list

> Renamed from **Noob_launchar** to **Noob_mod_antor** on 2026-11-03.

Every version of the client (1.8.9 Forge, 26.2 Fabric, and future ports)
implements the same module set with the same ids, so one config file shape
works everywhere. The mod is a pure in-game HUD/menu mod: **no account,
login, authentication or ownership features exist in any version.**

Modules 1–11 are implemented in every version. Modules 12–24, the
Performance page and the built-in resource pack were added in the
**1.8.9 Forge build, version 1.1.0**; modules 25–32 plus the
appearance system in a second 1.1.0 expansion round; and modules
33–38 in a third round matching Polyfrost's 1.8.9 catalog (see the
mapping section below); and module 39 in **1.1.2**, after live play
showed long Auction House tooltips overflowing a phone screen.
All are the spec target for future ports of the other versions
(the 26.2 Fabric build still ships 1–11).

| # | Module id | Display name | Default | HUD | Notes |
|---|-----------|--------------|---------|-----|-------|
| 1 | `fps` | FPS Counter | on | yes | Client FPS. Option: show "FPS" label (On/Off). |
| 2 | `cps` | CPS Counter | on | yes | Left \| right clicks/sec, rolling 1-second window. |
| 3 | `keystrokes` | Keystrokes | on | yes | W A S D, LMB, RMB, Space boxes; pressed keys light up in the module color. Labels follow the user's key bindings. No background setting (the boxes are the background). |
| 4 | `coords` | Coordinates | on | yes | XYZ, facing direction + axis hint, biome name. Options: show biome (On/Off), compass strip (Off/On — a 15-slot N/E/S/W tape with heading marker centered on the current bearing). |
| 5 | `armor` | Armor Status | on | yes | Helmet, chestplate, leggings, boots, main-hand item; icon + durability. Option: Percent / Remaining / Bar only. |
| 6 | `potions` | Potion Effects | on | yes | The local player's own active effects (the only effects a 1.8.9 client can see). Options: Style (Icon+Text with real inventory icons / Text only with color dot / Icon+Duration), Duration (On/Off). Name, amplifier, duration (m:ss or ∞). |
| 7 | `ping` | Ping | on | yes | Multiplayer latency from the player-list entry; shows `--` in singleplayer. |
| 8 | `clock` | Clock | on | yes | Real-world time. Option: 24h / 12h format. |
| 9 | `toggle_sprint` | Toggle Sprint | **off** | no | Keeps the player sprinting while moving forward, under vanilla's own conditions (food/creative, not using an item, not sneaking). Never modifies the sprint key binding; off = pure vanilla. |
| 10 | `zoom` | Zoom | on | no | Hold the zoom key (default C): smooth FOV zoom with mouse-sensitivity scaling; the user's exact FOV/sensitivity are restored on release. Option: factor 2x / 3x / 4x. |
| 11 | `fullbright` | Fullbright | **off** | no | Maximum-brightness rendering while on; the user's exact previous brightness is restored when switched off. |
| 12 | `crosshair` | Crosshair | on | yes (always centered) | Custom crosshair replacing the vanilla one. Options: arm length (Small/Medium/Large), gap (Small/Medium/Large), center dot (On/Off), vanilla crosshair (Hidden/Shown). Color = the module color setting. It cannot be dragged in the HUD editor (always screen-center by design). |
| 13 | `fov` | FOV Changer | **off** | no | Base FOV slider (30–110), independent of Zoom: seeds itself from the player's current FOV when first used, and never touches the setting while Zoom is overriding it. FOV-effect options: Dynamic FOV (On/Off — Off removes sprint/flying/speed-potion FOV changes at the FOVUpdateEvent multiplier), Lock FOV (Off/On — pins the final rendered FOV to the base setting, ignoring all dynamic modifiers). |
| 14 | `reach` | Reach Display | on | yes | Distance in blocks (2 decimals) from the eyes to the block/entity under the crosshair; `--` when nothing is in range. |
| 15 | `combo` | Combo Counter | on | yes | Client-side combo approximation: counts attack presses that land on a crosshair entity, resets after 2 s without a hit or when the player takes damage. Option: show best combo (Off/On). Not server-verified — see BUILD_NOTES. |
| 16 | `item_counter` | Item Counter | on | yes | Number of arrows in the inventory. |
| 17 | `playtime` | Playtime | on | yes | Session timer from the first world join after launch (H:MM:SS). |
| 18 | `memory` | Memory Usage | on | yes | JVM heap: used / max MB and percent. |
| 19 | `server` | Server Address | on | yes | Current multiplayer server address, or "Singleplayer". |
| 20 | `scoreboard` | Scoreboard | on | no | Vanilla sidebar Show/Hide. (1.8.9 Forge has no overlay event for the sidebar; it drives `GuiIngameForge.renderObjective`.) |
| 21 | `bossbar` | Boss Bar | on | no | Vanilla boss health bar Show/Hide (cancels the BOSSHEALTH overlay pre-event). |
| 22 | `time_changer` | Time Changer | **off** | no | Locks the *client-visual* world time: Day / Noon / Sunset / Night / Midnight. Visual only — the server's time, mob spawning and other players are unaffected. |
| 23 | `toggle_sneak` | Toggle Sneak | **off** | no | Press sneak once to latch sneaking, press again to release; drives the sneak key binding's own state, so vanilla movement code does the work. |
| 24 | `nick_hider` | Nick Hider | **off** | no | Replaces the player's own username with a chosen nickname in chat, on this client only. Free-text option: nickname (default "Player"). |
| 25 | `entity_size` | Entity Size | **off** | no | Render-scale only: Player size and Mob size sliders (0.5–2.0) applied as a GL scale around the entity's render call. The local player is included (visible in F5). **Collision/hitbox/entity dimensions are never touched — purely how big the model is drawn.** |
| 26 | `animation` | Animation Speed | on | no | Walk-cycle speed multiplier (0.25–3.0): temporarily scales each living entity's `limbSwing` phase during its render call and restores it right after. Visual only; 1.0 = vanilla. |
| 27 | `weather` | Weather Changer | **off** | no | Client-visual weather override: Server default / Always Clear / Always Rain. Pins the local world's rain strength (thunder off) each tick; nothing is sent to the server. |
| 28 | `freelook` | Freelook | on | no | Hold the Freelook key (default Left Alt) in third person: the mouse orbits the camera while the player's facing is restored every tick and never actually changes. Release restores the normal camera instantly. |
| 29 | `freecam` | Freecam | on | no | Toggle key (default F6): the camera detaches and flies freely (mouse + WASD + Jump/Sneak for up/down, Sprint = 3× speed; Fly speed slider 2–40 blocks/s) for screenshots. The player stays standing where he was (movement input frozen, rotation restored each tick, view swapped to a dummy camera entity). Toggle key or ESC exits; opening any GUI exits cleanly too. Only sees chunks the server already sent. |
| 30 | `name_highlight` | Name Highlight | on | no | Chat messages containing the player's own username (always watched) or any word from the editable comma list get a colored background + border behind their chat line(s) while fresh. Option: sound ping (Off/On, default Off; soft XP-orb sound). |
| 31 | `held_item` | Held Item Size | on | no | First-person held-item scale slider (0.5–1.5), applied on RenderHandEvent about the item's rest position. Visual only; 1.0 = vanilla. |
| 32 | `hitbox` | Hitbox Highlighter | **off** | no | Draws an outline box around players using their real vanilla bounding box (F3+B style, players only, configurable color). Depth testing stays ON, so boxes never show through walls; the box is only read and drawn, never modified. |
| 33 | `chat_tweaks` | Chat Tweaks | on | no | Compact repeats (On/Off): back-to-back duplicate chat messages collapse into one updating "§7×N" counter line instead of spamming (first line prints normally). The Copy Chat key (default F7) copies the last chat line's plain text to the clipboard. Works alongside Nick Hider and Name Highlight. No chat tabs (would need a full chat-GUI replacement). |
| 34 | `nametag` | Nametag | on | no | Shows your OWN nametag in third person/F5 (vanilla never draws it), plus a scale slider (0.5–2.0) that also scales other players' vanilla nametags. Plate text color and background follow the module's color/appearance settings. |
| 35 | `particles` | Particles | on | no | Crit particle multiplier (0.5–3.0) for your own critical hits — adds client-side crit/magic-crit particles scaled by the multiplier (server-sent base particles can't be intercepted in 1.8.9, so below 1.0 behaves like 1.0; use density to reduce). Density option (Vanilla/Reduced/Minimal) drives the vanilla particle setting. |
| 36 | `behind_you` | Behind You | on | no | Hold the Behind You key (default F8) in third person: camera snaps 180° behind; release restores. Pure camera override — facing never changes. |
| 37 | `sound_tweaks` | Sound Tweaks | **off** | no | Volume sliders (0–200%) for UI click, level-up, explosion and player-hurt sounds, matched by name substring on Forge's PlaySoundEvent and replaced at the scaled volume. |
| 38 | `smart_culling` | Smart Culling | on | no | Entity distance slider (0 = unlimited, default 64) and tile-entity distance slider (default 48): living entities and the special renderers of common block entities (chests, signs, banners, beacons, spawners, ...) are skipped beyond those distances. Chunk/block culling is vanilla's job and is NOT replaced (see BUILD_NOTES.md). |
| 39 | `tooltip_scroll` | Tooltip Scroll | on | no | (Since 1.1.2) Item tooltips taller than the screen — whose top vanilla/Forge pushes off-screen — are captured via ItemTooltipEvent and redrawn pinned fully on-screen with a scroll offset: mouse wheel (3 lines/notch) or, on touch, dragging inside the tooltip box. Vanilla tooltips that fit are never touched. Fixes Auction House lore being cut off at the top. |

## Performance page (1.8.9, since 1.1.0)

The main menu's **Performance** button opens a page with one-click
presets — **Potato / Balanced / Quality** — that write only vanilla
`GameSettings` fields (render distance, fancy graphics, particles,
clouds, entity shadows, mipmap levels, VBO, frame-rate limit, smooth
lighting, vsync) and persist via the vanilla options file, so they are
fully reversible in Options → Video Settings. The page also shows the
current values and the recommended OptiFine 1.8.9 settings. Exact preset
values are listed in the root `BUILD_NOTES.md`. It also has an
**Entity render distance** control (Unlimited / 16–256 blocks,
persisted in the mod config): living entities farther away than the
limit are not rendered at all (their rendering is cancelled
client-side; the entities themselves are unaffected).

## Appearance system (1.8.9, since 1.1.0 round 2)

The menu's **Appearance** page edits global defaults that every HUD
module starts from; each module's settings page can override them:

- **Background mode:** Clear (fully transparent) / Box (dark
  translucent panel) / Custom color.
- **Background opacity** slider (for Box/Custom).
- **Text color:** the preset palette, plus a **Chroma** mode — a
  smooth rainbow cycle driven by system time (one HSB conversion per
  element per frame; negligible cost).
- The **menu accent** follows the same global color/chroma choice.

Per-module overrides live in the module's `options` map under `apx.*`
keys (see `CONFIG_SCHEMA.md`), so existing configs load unchanged.

## Built-in resource pack (1.8.9, since 1.1.0)

The mod ships a small FPS/PvP resource pack ("Noob_mod_antor FPS Boost":
8×8 simplified block textures, short swords, low fire, clear pumpkin
overlay, tiny particle dots — generated by `generate_pack.py`) inside the
jar and extracts it to `resourcepacks/Noob_mod_antor FPS Boost` on first
run if it is not already there. It is never auto-enabled; the user turns
it on in Options → Resource Packs.

## Menu behaviour (all versions)

- **Right Shift** opens the main menu: scrollable module list, ON/OFF pill
  per module, per-module settings page (enabled, text color, chroma,
  background mode/color/opacity, module options — cycle chips, sliders
  and inline text fields — and a HUD position reset), an **Edit HUD**
  mode where enabled HUD elements are dragged with the mouse, and
  (1.8.9) the **Performance** and **Appearance** pages.
- Changes save immediately to the version's config file
  (see `CONFIG_SCHEMA.md`).
- HUD elements have sensible default anchors (top-left stack for
  FPS/CPS/Coords/Ping and the newer info modules, top-right Clock/Potions,
  bottom-right Keystrokes, beside the hotbar for Armor) until the user
  drags them.

## Polyfrost catalog mapping (reference match, 1.1.0 round 3)

The user's reference set is Polyfrost's 1.8.9 catalog. Equivalence:

- **Already covered:** PolySprint → Toggle Sprint + Toggle Sneak;
  EvergreenHUD → the HUD module set; PolyTime → Time Changer +
  Clock + Playtime; PolyWeather → Weather Changer; PolyCrosshair →
  Crosshair; Canelex Keystrokes → Keystrokes; PolyZoom → Zoom;
  PolyFullbright → Fullbright; PolyHitbox → Hitbox Highlighter;
  OneConfig → our own menu/config/appearance system (design
  equivalent, not a module).
- **Added in round 3:** Chat Tweaks (Chatting + ChatTweaks), Nametag,
  Particles, Behind You, Sound Tweaks, Smart Culling, Dynamic FPS
  (Performance page), menu search (SmartSearch-lite), and a
  crash-guard around every module entry point (CrashPatch spirit —
  infrastructure, not a module).
- **Added later (1.1.2):** Tooltip Scroll — it sat in the skipped
  list below during round 3, then landed via the ItemTooltipEvent
  capture + DrawScreenEvent.Post redraw route after live play
  showed long Auction House tooltips overflowing a phone screen
  (see BUILD_NOTES.md).
- **Attempted, skipped with documented reasons (BUILD_NOTES.md):**
  Old Animations, Damage Tint, Color Saturation, Motion Blur
  (PolyBlur), Glint Colorizer, GUI Blur, 3D Skin
  Layers.
- **Intentionally excluded:** Poly+ (world hosting/friends service),
  OneClient modpacks (whole-client products), Hytils Reborn
  (Hypixel-server-specific), Redaction (unclear scope, overlaps),
  WaveyCapes (account cape cosmetics), Compose Multiplatform
  (a library, not a feature).

## Deliberately out of scope

- **Lunar cosmetics / emotes** — server- and account-bound; impossible and
  inappropriate for a client-side mod.
- **Entity radar / minimap** — goes beyond HUD parity (and is banned on
  many servers); not a Lunar HUD module either.
- **Chat macros / auto-text** — server-rule risk; out of scope.
- **Sodium / Sodium Extra** — do not exist for 1.8.9 (Fabric 1.16+ only).
  The performance mod for 1.8.9 is **OptiFine**, which the user installs
  separately; it is never bundled or redistributed.
