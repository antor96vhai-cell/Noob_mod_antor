# Noob_launchar — Build Notes (project root)

**Noob_launchar** is an original, lightweight in-game HUD/menu client mod —
module menu (Right Shift), per-module settings, draggable HUD editor, and
11 modules (see `shared/MODULES.md`). It is a pure client-side HUD/menu mod:
**no authentication, login, or ownership code exists in any version.**
Design language: `shared/DESIGN.md`. Config format (one schema for all
versions): `shared/CONFIG_SCHEMA.md`.

## Project structure

```
noob-launchar/
  BUILD_NOTES.md            <- this file
  shared/
    MODULES.md              canonical module list (ids, defaults, behaviour)
    DESIGN.md               palette, layout, color presets
    CONFIG_SCHEMA.md        config JSON schema shared by all versions
  versions/
    1.8.9-forge/            Minecraft 1.8.9 + Forge 11.15.1.2318, Java 8, MCP mappings
      Noob_launchar-1.0.0.jar     <- built distribution jar (SRG names)
      build.sh                    reproducible manual pipeline (see below)
      build.gradle, gradlew, ...  official MDK files (see "ForgeGradle" below)
      src/main/java/nooblaunchar/ mod sources (21 files)
      src/main/resources/mcmod.info
      manual/                     toolchain: libs/, tools/, mappings/, work/
    26.2-fabric/            Minecraft 26.2 + Fabric, Java 25, Mojang mappings
      Noob_launchar-1.0.0_mc26.2.jar  <- built jar (display name renamed)
      build.sh, src/, resources/, BUILD_NOTES.md (full per-version notes)
```

Toolchain installed in the workspace: Temurin JDK 8 at `~/workspace/jdk8`
(1.8.0_504), JDK 25 at `~/workspace/jdk25` (26.2 build), Gradle 2.7 at
`~/workspace/gradle-2.7` (unused in the end — see ForgeGradle section).

## Version status

| Version | Status | Jar | Size |
|---------|--------|-----|------|
| **1.8.9 Forge** | ✅ **Built** (manual pipeline; compiles clean, reobf verified) | `versions/1.8.9-forge/Noob_launchar-1.0.0.jar` | 47,942 bytes |
| **26.2 Fabric** | ✅ Built earlier; display name renamed to Noob_launchar and rebuilt this run | `versions/26.2-fabric/Noob_launchar-1.0.0_mc26.2.jar` | 45,243 bytes |

Adding a future version = create `versions/<mc>-<loader>/`, reuse the
`shared/` spec, mirror the module classes, add a `build.sh`.

---

## 1.8.9 Forge — how it was built

### The ForgeGradle route is blocked in this sandbox (honest blocker report)

The official route (MDK + ForgeGradle 2.1 + Gradle 2.7 + Temurin 8) was set
up and attempted first. It cannot complete **in this sandbox**, for two
independent reasons, both diagnosed to root cause:

1. **All Java TCP is blocked by the sandbox permission `other_tcp = Deny`.**
   Any socket opened by a Java process — direct, loopback, or to the egress
   proxy — is intercepted and answered with a plain-text denial:
   *"muse: Other TCP connections is turned off for this assistant. To allow
   it, ask the user to open Muse settings -> Permissions -> Direct network
   protocols and switch other_tcp from Deny to Ask."* (Verified with a raw
   `java.net.Socket` probe; Python/curl networking works normally.)
   Gradle/ForgeGradle download everything (plugin, userdev, MC jars, MCP
   data) from Java, so the stock route dies at dependency resolution with
   "Connection reset". **If `other_tcp` is switched to Ask/Allow, the MDK
   route should work** — `build.gradle` is already prepared for it:
   dead `jcenter()` replaced with `mavenCentral()`, Forge repo pointed at
   `https://maven.minecraftforge.net`, `archivesBaseName = "Noob_launchar"`,
   and mappings changed `stable_20` → `snapshot_20160204` (the `stable_20`
   zip no longer exists on the Forge maven — verified 404; the 2016-02-04
   snapshot zip is the set stable_20 was cut from and is still served).
   Two sandbox quirks found en route, for the record: the Gradle daemon
   segfaults in `libnative-platform`'s `setEnvironmentVariable` unless the
   giant `JARVIS_TRACE_CONTEXT` env var is unset (`env -u
   JARVIS_TRACE_CONTEXT`), and Gradle needs the sandbox proxy via
   `systemProp.*.proxy*` in a user-home `gradle.properties`.
2. Even with downloads done by hand, ForgeGradle itself performs further
   Java-side HTTP (version JSON, library checks), so pre-seeding alone
   doesn't rescue it here.

### The manual pipeline that replaced it (what `build.sh` runs)

Mirrors the proven 26.2 manual setup and reproduces ForgeGradle's
`setupDevWorkspace` + `reobf` steps with curl (downloads) + local-only Java
tools (no Java networking anywhere):

1. **Downloads (curl, all verified):** Forge 1.8.9 userdev from
   maven.minecraftforge.net; MCP `joined.srg` (`de.oceanlabs.mcp:mcp:1.8.9:srg`);
   MCP snapshot CSVs (`mcp_snapshot:20160204-1.8.9` — fields/methods/params);
   SpecialSource 1.8.6 (Maven Central); MC 1.8.9 client **and** server jars
   from Mojang via the piston-meta version manifest — **both sha1-verified
   against the manifest** (client `3870888a…b908` ✓).
2. **obf → SRG:** client+server jars merged into the "joined" jar, remapped
   with SpecialSource + `joined.srg`.
3. **Forge classes:** the userdev's `classes.jar` is stored with obfuscated
   class names + SRG member names, so it is remapped with `joined.srg` too
   (this step is easy to miss — without it, Forge signatures reference
   obfuscated classes like `avr`), then merged with the SRG Minecraft jar
   (zero class overlaps).
4. **SRG → MCP:** a mapping file `srg2mcp.srg` (6,523 fields + 13,623
   methods) is generated from `joined.srg` + the snapshot CSVs by
   `manual/mappings/generate_srg2mcp.py` (MCP renames members only, so SRG
   descriptors carry over unchanged); SpecialSource produces
   `manual/libs/minecraft-1.8.9-forge-mcp.jar` (16.8 MB) — the compile
   classpath, together with the 1.8.9 library jars (lwjgl 2.9.4, gson 2.2.4,
   guava 17, log4j-api, launchwrapper, asm, …).
5. **Compile:** `javac` (JDK 8) — clean, zero errors/warnings.
6. **Reobfuscate:** the mod jar is merged with the MCP classpath jar as
   hierarchy context, remapped MCP → SRG with the generated inverse mapping
   (`mcp2srg.srg`), and only the `nooblaunchar/**` classes + `mcmod.info`
   are kept — the standard SRG-named distribution form for 1.8.9 mods.
   Verified with javap: e.g. ZoomModule references `field_74334_X`, which
   `fields.csv` confirms is `fovSetting`; `@Mod(name = "Noob_launchar")`
   and `mcmod.info` are intact.

Exact build command: `cd versions/1.8.9-forge && ./build.sh`
(JDK 8 assumed at `~/workspace/jdk8`, override with `JDK8=...`.)

### APIs verified against the produced MCP jar (javap), not guessed

`Minecraft`: `thePlayer/theWorld/fontRendererObj/gameSettings/mcDataDir`
fields, `getMinecraft()`, `getDebugFPS()`, `displayGuiScreen`,
`getNetHandler()`, `getCurrentServerData()`, `getRenderItem()`,
`isSingleplayer()`, `currentScreen` field. `GameSettings`: `fovSetting`,
`gammaSetting`, `mouseSensitivity` (public floats — zoom/fullbright drive
them directly, no validation layer in 1.8.9), `keyBindForward/Left/Back/
Right/Jump/Attack/UseItem`. `KeyBinding#isKeyDown/isPressed/getKeyCode`.
`Gui.drawRect` (static, left/top/right/bottom), `GuiScreen` (`width/height`,
`fontRendererObj`, `drawScreen/mouseClicked/mouseReleased/mouseClickMove/
handleMouseInput/keyTyped/doesGuiPauseGame`). `FontRenderer#drawString/
getStringWidth`. `RenderGameOverlayEvent.Post` + `ElementType.ALL`,
`resolution` (ScaledResolution) field. `TickEvent.ClientTickEvent` +
`Phase`. `MinecraftForge.EVENT_BUS`, `EventBus#register`,
`ClientRegistry.registerKeyBinding`, `@Mod` incl. `clientSideOnly` and
`acceptedMinecraftVersions`, `FMLPreInitializationEvent
#getModConfigurationDirectory`. Player: `inventory.armorInventory`
(boots-first order — display order reversed in code), `getHeldItem()`,
`getFoodStats().getFoodLevel()`, `capabilities.isCreativeMode`,
`isUsingItem()`, `getActivePotionEffects()`, `getPosition()`,
`rotationYaw`, `setSprinting/isSprinting/isSneaking`. Potions:
`Potion.potionTypes`, `PotionEffect#getPotionID/getDuration/getAmplifier/
getEffectName`, `Potion#getLiquidColor`. Ping:
`NetHandlerPlayClient#getPlayerInfo(UUID)`,
`NetworkPlayerInfo#getResponseTime`. Biome: `World#getBiomeGenForCoords`,
`BiomeGenBase.biomeName`. Items: `ItemStack#isItemStackDamageable/
getItemDamage/getMaxDamage`, `RenderItem#renderItemIntoGUI/
renderItemOverlays`, `RenderHelper` lighting. LWJGL: `Keyboard.getKeyName/
KEY_RSHIFT/KEY_C`, `Mouse.isButtonDown/getEventDWheel`.

### 1.8.9 design notes / simplifications

- **Fullbright uses `gammaSetting = 100`** — the classic 1.8.9 approach:
  the vanilla lightmap curve saturates far below that value. The user's
  exact previous gamma is restored on disable (and after a crash, via the
  config-persisted base value + first-tick cleanup). Unlike 26.2, no range
  clamp applies in 1.8.9, so this is true fullbright.
- **Zoom drives `fovSetting` (float)** with the same capture/smooth/restore
  design as 26.2, but without the vanilla-range ceiling (target clamped
  only at 10°), plus mouse-sensitivity scaling.
- **Facing direction is computed from `rotationYaw`** (F3 convention)
  instead of a facing API, and **potion "infinite"** shows ∞ only for
  absurd durations (> ~16.7 h) since 1.8.9 has no real infinite flag.
- HUD editor is fully implemented in 1.8.9's GUI system: the overlay
  handler keeps drawing the live HUD behind the editor screen, which adds
  outlines/name tags and drag handling via
  `mouseClicked/mouseClickMove/mouseReleased`.
- The userdev's `devbinpatches` (Forge's patched Minecraft classes) are
  intentionally **not** applied to the compile jar: the mod uses only
  vanilla public APIs + Forge event classes, and at runtime the real Forge
  installation supplies the patched classes. `merged_at.cfg` access
  transformers are likewise unneeded (no non-public member is touched).

### What must be checked in a live 1.8.9 game test

- The jar loads in a real Forge 1.8.9 `mods/` folder (FML SRG remapping of
  the mod at runtime) alongside typical PvP mods (OptiFine etc.).
- Right Shift opens the menu in-world; wheel scrolling; settings Esc goes
  back to the menu (not to game).
- Item icons in Armor Status render correctly in the overlay pass
  (RenderItem state in `RenderGameOverlayEvent` — the classic place where
  1.8.9 HUD mods can hit GL state quirks), especially enchanted armor glint.
- Zoom/Fullbright: FOV and gamma restore exactly after closing the game
  mid-zoom (options.txt is written on shutdown with live field values; the
  config cleanup repairs on next launch — verify once).
- Toggle Sprint on a server with hunger near the threshold; CPS accuracy
  at low FPS (frame-polled edges can merge clicks within one frame).
- HUD Edit drag with GUI scale AUTO/large on a small window (positions are
  GUI-scaled pixels, shared schema with 26.2).

---

## 26.2 Fabric — state

Complete and previously delivered; full notes in
`versions/26.2-fabric/BUILD_NOTES.md`. Summary: 20 source files, manual
javac build against the autovault-port classpath (no Gradle/Loom), all 11
modules, Right Shift menu + settings + HUD editor, config at
`config/kutuclient.json`. Two honest limitations recorded there: no Sponge
Mixin API exists in this sandbox's classpath, so Zoom clamps to the vanilla
FOV range [30, 110] via `Options.fov()` and Fullbright uses max vanilla
gamma (1.0) rather than a true lightmap override (mixin target mapped for
a future build). This run only renamed the display name to
"Noob_launchar" (fabric.mod.json + menu title), changed the output jar
name, and rebuilt (45,243 bytes); mod id/package/config filename unchanged.

Exact build command: `cd versions/26.2-fabric && ./build.sh`
(uses `~/workspace/jdk25` and `~/workspace/autovault-port/manual/libs`).
