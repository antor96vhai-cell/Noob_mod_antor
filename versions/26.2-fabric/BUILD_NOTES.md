# KUTU Client — Build Notes

**Result:** `kutuclient-1.0.0_mc26.2.jar` (45,234 bytes), Minecraft 26.2, Fabric
Loader >= 0.19, Java 25, client-only, mod id `kutuclient`. Compiles cleanly
(only lint warnings are about missing JetBrains/error-prone annotation classes
inside the library jars themselves — none from this mod's sources).

## Exact build command

Use `./build.sh` (it reproduces the jar end to end). What it runs:

```bash
JDK=~/workspace/jdk25
LIBS=~/workspace/autovault-port/manual/libs
CP=$(find "$LIBS" -name '*.jar' | sort | tr '\n' ':')   # incl. manual/libs/api-nested/
rm -rf classes && mkdir -p classes
find src -name '*.java' | sort > sources.txt
~/workspace/jdk25/bin/javac -encoding UTF-8 -cp "$CP" -d classes @sources.txt
cp -r resources/. classes/
~/workspace/jdk25/bin/jar --create --file kutuclient-1.0.0_mc26.2.jar -C classes .
```

No Gradle/Loom, mirroring the proven `autovault-port` manual setup. Class files
are major version 69 (Java 25) as 26.2 requires.

## APIs verified against the actual jars (javap / unzip -l), not guessed

From `minecraft-26.2-client.jar`:

- `GuiGraphicsExtractor`: `fill`, `outline`, `text(Font, String/Component, ...)`,
  `centeredText`, `item(ItemStack, int, int)`, `itemDecorations(Font, ItemStack, int, int)`,
  `enableScissor/disableScissor`, `guiWidth()/guiHeight()`. All `blitSprite`
  overloads require a `RenderPipeline` first argument (this drove the potion
  module design — see Simplifications).
- `Minecraft`: public fields `font`, `gui`, `options`, `player`, `level`,
  `mouseHandler`, `gameRenderer`; methods `getFps()`, `getConnection()`,
  `getCurrentServer()` (null in singleplayer), `setScreenAndShow(Screen)`,
  `getWindow()`. `Gui#screen()` and `Gui#setScreen(Screen)` — the open/close
  idiom copied from the autovault sources.
- Screen input in 26.2 uses event records: `mouseClicked(MouseButtonEvent, boolean)`,
  `mouseReleased(MouseButtonEvent)`, `mouseDragged(MouseButtonEvent, double, double)`,
  `mouseScrolled(double, double, double, double)`, `keyPressed(KeyEvent)`;
  `MouseButtonEvent` is a record with `x()`, `y()`, `button()`; screens override
  `extractRenderState(GuiGraphicsExtractor, int, int, float)` (same as autovault).
- `MouseHandler#isLeftPressed()/isRightPressed()` — used for CPS edge detection
  and keystroke mouse boxes.
- `ClientPacketListener#getPlayerInfo(UUID)` and `PlayerInfo#getLatency()` — Ping.
- `MobEffectInstance#getDuration()/getAmplifier()/isInfiniteDuration()/getEffect()`
  (returns `Holder<MobEffect>`), `MobEffect#getDisplayName()/getColor()`,
  `LivingEntity#getActiveEffects()/getItemBySlot(EquipmentSlot)/getMainHandItem()`,
  `ItemStack#isDamageableItem()/getDamageValue()/getMaxDamage()` — Potions/Armor.
  Durations are formatted manually (ticks/20 → m:ss); `MobEffectUtil.formatDuration`
  exists but its unit semantics were unclear, so it was not used.
- `Level#getBiomeManager()` → `BiomeManager#getBiome(BlockPos)` →
  `Holder#unwrapKey()` → `ResourceKey#identifier()` → `Identifier#getPath()` — biome name.
- `KeyMapping#isDown()/setDown(boolean)/consumeClick()`, `Options` public
  `keyUp/keyDown/keyLeft/keyRight/keyJump` fields, `Options#fov()` →
  `OptionInstance<Integer>`, `Options#gamma()`/`sensitivity()` → `OptionInstance<Double>`,
  `OptionInstance#get()/set(T)`.
- `Entity#getDirection()/isSprinting()/setSprinting(boolean)/isShiftKeyDown()`,
  `LivingEntity#isUsingItem()`, `Player#getFoodData()/getAbilities()`,
  `FoodData#getFoodLevel()`, `Abilities#instabuild` — Toggle Sprint conditions.

From Fabric jars (`fabric-loader-0.20.1.jar`, `fabric-api-0.161.0+26.2.jar` and
its nested jars):

- `ClientModInitializer`, `ClientTickEvents.END_CLIENT_TICK`,
  `KeyMappingHelper.registerKeyMapping`, `KeyMapping.Category.MISC` — idioms
  copied from the autovault 26.2 sources.
- HUD: `net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement` with the
  single method `extractRenderState(GuiGraphicsExtractor, DeltaTracker)`,
  registered via `HudElementRegistry.addLast(Identifier, HudElement)`.

## Design decisions / simplifications

1. **No mixins — deliberately.** The task allowed mixins "only where needed"
   (zoom FOV, fullbright lightmap). Investigation showed the Sponge Mixin
   annotation API (`org.spongepowered.asm.mixin.*`) is **not present anywhere
   in this build's offline classpath**: the provided `fabric-loader-0.20.1.jar`
   contains zero `org/spongepowered` entries and no sponge/mixin jar exists
   anywhere on the machine, so `@Mixin` classes cannot be compiled here at all.
   There is therefore **no mixins json** in the jar. Both features were
   re-implemented through fully verified vanilla hooks instead:

   - **Zoom** drives `Options.fov()`. `OptionInstance.set()` bytecode was
     disassembled: it calls `ValueSet.validateValue()` and, on failure, logs
     "Illegal option value" and falls back to the option's **default** — so
     out-of-range values are unusable, and the zoom target is clamped to the
     vanilla FOV range [30, 110] (at the default FOV 70 that is ~2.3x maximum;
     users with a higher base FOV get proportionally more). Smoothing is a
     per-tick lerp (factor 0.4, ~250 ms to settle). Mouse sensitivity is
     scaled by the zoom factor while held. The user's exact base FOV and
     sensitivity are restored on release, and the base FOV is also persisted
     in `kutuclient.json` so an unclean shutdown mid-zoom is repaired on the
     next launch (startup cleanup on the first client tick).
   - **Fullbright** drives `Options.gamma()` to its maximum 1.0. The 26.2
     lightmap path was verified in bytecode: `LightmapRenderStateExtractor`
     computes `LightmapRenderState.brightness` directly from
     `options.gamma().get()`, so max gamma is the brightest rendering vanilla
     supports. It is **not** a true lightmap override (a mixin into
     `LightmapRenderStateExtractor.extract` setting `state.brightness` would
     be; that path is mapped and ready if a mixin-capable build is ever set
     up). The user's previous gamma is restored exactly on disable, with the
     same crash-safe persistence as zoom.

2. **Potion effects are text rows** (effect-colored dot + name + amplifier +
   duration), not vanilla atlas icons: every `blitSprite` overload in 26.2
   requires a `RenderPipeline`, and no suitable public pipeline constant for
   effect icons was verified in the jars. Armor items *do* use real icons via
   `GuiGraphicsExtractor.item()` + `itemDecorations()` (which also draws the
   vanilla durability bar).

3. **CPS** is measured by polling `MouseHandler.isLeftPressed()/isRightPressed()`
   once per HUD frame with rising-edge detection and a rolling 1-second
   window — no input mixins needed. Counts continue while any screen is open
   (HUD still extracts), which matches how players use CPS overlays.

4. **GUI is fully hand-rolled** (no vanilla widgets): all three screens draw
   with `fill/outline/text` and dispatch clicks through per-frame click-zone
   lists. This keeps the original dark design pixel-exact and avoids extra
   widget API surface. The design is original (dark panel, teal accent) —
   no Lunar Client assets, branding, or layouts were copied; only generic
   feature ideas (module list, toggles, HUD editor) were used.

5. **Toggle Sprint** never touches the player's sprint key binding; it only
   calls `setSprinting(true)` under the same conditions vanilla requires
   (forward held, food > 6 or creative, not using an item, not sneaking).
   Turning the module off therefore restores 100% vanilla behavior instantly.

6. Config defaults: FPS, CPS, Keystrokes, Coordinates, Armor, Potions, Ping,
   Clock and Zoom ship **enabled**; Toggle Sprint and Fullbright ship
   **disabled** (they change gameplay feel / visuals). All changeable in the
   menu; everything persists in `config/kutuclient.json`.

## What must be checked in a live game test

- Right Shift opens the menu while in a world (it is also the sneak key for
  players who bound sneak to Right Shift — vanilla behavior, same as other
  clients; rebindable in Controls).
- Menu scrolling with many rows on a small GUI scale; settings-page click
  zones (pills/chips) line up with their visuals at GUI scales 1–4.
- HUD Edit drag: modules render identically inside the editor (the HUD also
  extracts behind the editor screen); positions persist across restarts.
- Zoom feel: per-tick (20 Hz) FOV steps during the ~250 ms ease; whether the
  FOV floor of 30 is enough at Antor's base FOV; sensitivity scaling while
  zoomed; FOV restores exactly after opening inventory mid-zoom.
- Fullbright brightness vs. expectation (gamma 1.0 max — caves will be much
  brighter but not perfectly flat-lit); gamma restores after toggling and
  after a forced quit while enabled.
- Armor module default position next to the hotbar at different GUI scales;
  `itemDecorations` durability bar overlaps the percent text cleanly.
- Potion module with several simultaneous effects (width grows to the widest
  row) and with infinite-duration effects (shows ∞).
- Ping on a real server (mc.antorbio.qzz.io) vs. singleplayer ("Ping: --").
- Toggle Sprint with hunger near the 6-food threshold and while eating
  (must not force sprint).
- CPS accuracy against rapid clicking (frame-polled edges can merge clicks
  that happen within a single rendered frame at very low FPS).
- The mod loads alongside the other 43 client mods in the SKLauncher
  "the-shift" instance (esp. Sodium) without HUD layer conflicts.

---

## Rename note (2026-11-03, scope change)

The project was renamed **Noob_launchar** and restructured into
`~/workspace/noob-launchar/` with this build moved to `versions/26.2-fabric/`.
The 26.2 display name is now "Noob_launchar" (fabric.mod.json `name`, menu
title) and the jar is `Noob_launchar-1.0.0_mc26.2.jar` (45,243 bytes,
rebuilt and verified after the rename). The mod id (`kutuclient`), package
(`kutu.kutuclient`) and config filename (`config/kutuclient.json`) are
unchanged for continuity — see `shared/CONFIG_SCHEMA.md`. Everything else
in the notes above describes this build accurately as written.
