package noobmodantor.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import noobmodantor.NoobConfig;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Performance page: one-click presets that write straight into the vanilla
 * {@link GameSettings} (render distance, fancy graphics, particles, clouds,
 * entity shadows, mipmap, VBO, frame-rate limit, smooth lighting, vsync)
 * and persist through the vanilla options file — so they are fully
 * reversible in Options -&gt; Video Settings, and nothing mod-specific is
 * stored. Also lists the recommended OptiFine 1.8.9 settings: OptiFine is
 * the performance mod for this version (Sodium does not exist for 1.8.9);
 * it is NOT bundled — the user drops its jar into the mods folder and its
 * extra Video Settings options appear automatically.
 */
public class PerformanceScreen extends GuiScreen {
    private static final int PANEL_W = 320;

    private final GuiScreen parent;
    private final List<Ui.Zone> zones = new ArrayList<Ui.Zone>();
    private String appliedMsg = "";
    private long appliedAt = 0L;

    private static final String[] PARTICLES = {"All", "Decreased", "Minimal"};
    private static final String[] CLOUDS = {"Off", "Fast", "Fancy"};
    private static final String[] SMOOTH = {"Off", "Minimum", "Maximum"};

    private static final String[] OPTIFINE_TIPS = {
            "OptiFine 1.8.9 HD is the performance mod for",
            "this version (there is no Sodium for 1.8.9).",
            "It is not bundled: drop its jar into the mods",
            "folder and its options appear in Video Settings.",
            "",
            "Recommended: Graphics Fast, Smooth Lighting OFF,",
            "Render Distance 2-6, Max Framerate Unlimited,",
            "Fast Render ON, Fast Math ON, Chunk Updates 1,",
            "Lazy Chunk Loading ON, Entity Shadows OFF,",
            "VSync OFF, and in Animations turn off water,",
            "lava and flame if you need every last frame."
    };

    public PerformanceScreen(GuiScreen parent) {
        this.parent = parent;
    }

    private void applyPreset(String name) {
        GameSettings gs = mc.gameSettings;
        if (gs == null) return;
        if ("Potato".equals(name)) {
            set(gs, 2, false, 2, 0, false, 0, 0);
        } else if ("Balanced".equals(name)) {
            set(gs, 8, true, 1, 1, true, 2, 1);
        } else { // Quality
            set(gs, 12, true, 0, 2, true, 4, 2);
        }
        gs.useVbo = true;
        gs.limitFramerate = 260; // 260 = unlimited in 1.8.9
        gs.enableVsync = false;
        gs.saveOptions();
        appliedMsg = name + " preset applied";
        appliedAt = System.currentTimeMillis();
    }

    private static void set(GameSettings gs, int renderDistance, boolean fancy, int particles,
                            int clouds, boolean shadows, int mipmap, int smoothLighting) {
        gs.renderDistanceChunks = renderDistance;
        gs.fancyGraphics = fancy;
        gs.particleSetting = particles;
        gs.clouds = clouds;
        gs.entityShadows = shadows;
        gs.mipmapLevels = mipmap;
        gs.ambientOcclusion = smoothLighting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        zones.clear();
        GameSettings gs = mc.gameSettings;

        final int px = (this.width - PANEL_W) / 2;
        int py = 16;
        int ph = Math.max(240, this.height - 32);
        Ui.panel(px, py, PANEL_W, ph);
        Ui.button(this.fontRendererObj, px + 10, py + 8, 52, 18, "< Back", false);
        zones.add(new Ui.Zone(px + 10, py + 8, 52, 18, new Runnable() {
            @Override
            public void run() {
                mc.displayGuiScreen(parent);
            }
        }));
        Ui.centeredText(this.fontRendererObj, "Performance", px + PANEL_W / 2 + 26, py + 13, Ui.accent());
        Ui.accentLine(px + 10, py + 34, PANEL_W - 20);

        int y = py + 44;
        Ui.centeredText(this.fontRendererObj, "One-click presets (vanilla Video Settings)", px + PANEL_W / 2, y, Ui.TEXT_DIM);
        y += 14;

        final String[] names = {"Potato", "Balanced", "Quality"};
        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            final int bx = px + 8 + i * 104;
            boolean hover = mouseX >= bx && mouseX < bx + 96 && mouseY >= y && mouseY < y + 22;
            Ui.button(this.fontRendererObj, bx, y, 96, 22, name, hover);
            zones.add(new Ui.Zone(bx, y, 96, 22, new Runnable() {
                @Override
                public void run() {
                    applyPreset(name);
                }
            }));
        }
        y += 28;
        if (!appliedMsg.isEmpty() && System.currentTimeMillis() - appliedAt < 3000L) {
            Ui.centeredText(this.fontRendererObj, appliedMsg, px + PANEL_W / 2, y, Ui.accent());
        }
        y += 14;

        if (gs != null) {
            String[][] rows = {
                    {"Render Distance", Integer.toString(gs.renderDistanceChunks)},
                    {"Graphics", gs.fancyGraphics ? "Fancy" : "Fast"},
                    {"Particles", PARTICLES[clamp(gs.particleSetting, 2)]},
                    {"Clouds", CLOUDS[clamp(gs.clouds, 2)]},
                    {"Entity Shadows", onOff(gs.entityShadows)},
                    {"Mipmap Levels", Integer.toString(gs.mipmapLevels)},
                    {"Use VBOs", onOff(gs.useVbo)},
                    {"Max Framerate", gs.limitFramerate >= 260 ? "Unlimited" : Integer.toString(gs.limitFramerate)},
                    {"Smooth Lighting", SMOOTH[clamp(gs.ambientOcclusion, 2)]},
                    {"VSync", onOff(gs.enableVsync)},
            };
            for (int i = 0; i < rows.length; i++) {
                int colX = px + 14 + (i % 2) * 152;
                int rowY = y + (i / 2) * 11;
                this.fontRendererObj.drawString(rows[i][0] + ": ", colX, rowY, Ui.TEXT_DIM);
                int labelW = this.fontRendererObj.getStringWidth(rows[i][0] + ": ");
                this.fontRendererObj.drawString(rows[i][1], colX + labelW, rowY, Ui.TEXT);
            }
            y += ((rows.length + 1) / 2) * 11 + 6;
        }

        // Entity render distance (mod-side, persisted in our config):
        // living entities farther than this are not rendered at all.
        final int[] DIST_STEPS = {0, 16, 24, 32, 48, 64, 96, 128, 192, 256};
        final int curDist = NoobConfig.get().entityRenderDistance;
        int curIdx = 0;
        for (int i = 0; i < DIST_STEPS.length; i++) {
            if (DIST_STEPS[i] == curDist) curIdx = i;
        }
        final int distIdx = curIdx;
        this.fontRendererObj.drawString("Entity render dist:", px + 14, y + 4, Ui.TEXT_DIM);
        String distText = curDist == 0 ? "Unlimited" : (curDist + " blocks");
        Ui.centeredText(this.fontRendererObj, distText, px + 218, y + 4, Ui.TEXT);
        Ui.button(this.fontRendererObj, px + 262, y, 20, 16, "-", false);
        Ui.button(this.fontRendererObj, px + 286, y, 20, 16, "+", false);
        zones.add(new Ui.Zone(px + 262, y, 20, 16, new Runnable() {
            @Override
            public void run() {
                int ni = Math.max(0, distIdx - 1);
                NoobConfig.get().entityRenderDistance = DIST_STEPS[ni];
                NoobConfig.save();
            }
        }));
        zones.add(new Ui.Zone(px + 286, y, 20, 16, new Runnable() {
            @Override
            public void run() {
                int ni = Math.min(DIST_STEPS.length - 1, distIdx + 1);
                NoobConfig.get().entityRenderDistance = DIST_STEPS[ni];
                NoobConfig.save();
            }
        }));
        y += 22;

        // Dynamic FPS: cap the framerate while the window is unfocused.
        final NoobConfig cfg = NoobConfig.get();
        this.fontRendererObj.drawString("Dynamic FPS:", px + 14, y + 4, Ui.TEXT_DIM);
        Ui.pill(px + 92, y + 1, cfg.dynamicFps);
        zones.add(new Ui.Zone(px + 92, y, 30, 18, new Runnable() {
            @Override
            public void run() {
                cfg.dynamicFps = !cfg.dynamicFps;
                NoobConfig.save();
            }
        }));
        if (cfg.dynamicFps) {
            this.fontRendererObj.drawString("unfocused cap:", px + 134, y + 4, Ui.TEXT_DIM);
            Ui.button(this.fontRendererObj, px + 216, y, 20, 16, "-", false);
            Ui.centeredText(this.fontRendererObj, Integer.toString(cfg.dynamicFpsCap), px + 258, y + 4, Ui.TEXT);
            Ui.button(this.fontRendererObj, px + 280, y, 20, 16, "+", false);
            final int[] CAPS = {5, 10, 15, 20, 30, 60};
            zones.add(new Ui.Zone(px + 216, y, 20, 16, new Runnable() {
                @Override
                public void run() {
                    int cur = cfg.dynamicFpsCap;
                    int next = CAPS[0];
                    for (int i = CAPS.length - 1; i >= 0; i--) {
                        if (CAPS[i] < cur) { next = CAPS[i]; break; }
                    }
                    cfg.dynamicFpsCap = next;
                    NoobConfig.save();
                }
            }));
            zones.add(new Ui.Zone(px + 280, y, 20, 16, new Runnable() {
                @Override
                public void run() {
                    int cur = cfg.dynamicFpsCap;
                    int next = CAPS[CAPS.length - 1];
                    for (int i = 0; i < CAPS.length; i++) {
                        if (CAPS[i] > cur) { next = CAPS[i]; break; }
                    }
                    cfg.dynamicFpsCap = next;
                    NoobConfig.save();
                }
            }));
        } else {
            this.fontRendererObj.drawString("caps FPS while alt-tabbed", px + 134, y + 4, Ui.TEXT_DIM);
        }
        y += 22;

        Ui.accentLine(px + 10, y, PANEL_W - 20);
        y += 8;
        this.fontRendererObj.drawString("OptiFine tips", px + 14, y, Ui.accent());
        y += 12;
        for (String line : OPTIFINE_TIPS) {
            this.fontRendererObj.drawString(line, px + 14, y, Ui.TEXT_DIM);
            y += 11;
        }
        y += 4;
        this.fontRendererObj.drawString("Note: mipmap changes fully apply after F3+T", px + 14, y, Ui.TEXT_DIM);
        y += 11;
        this.fontRendererObj.drawString("or rejoining the world.", px + 14, y, Ui.TEXT_DIM);

        int btnY = py + ph - 30;
        Ui.button(this.fontRendererObj, px + PANEL_W / 2 - 50, btnY, 100, 20, "Back", false);
        zones.add(new Ui.Zone(px + PANEL_W / 2 - 50, btnY, 100, 20, new Runnable() {
            @Override
            public void run() {
                mc.displayGuiScreen(parent);
            }
        }));
    }

    private static int clamp(int v, int max) {
        return Math.max(0, Math.min(max, v));
    }

    private static String onOff(boolean b) {
        return b ? "On" : "Off";
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && Ui.click(zones, mouseX, mouseY)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
