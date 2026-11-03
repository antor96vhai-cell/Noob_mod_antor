package nooblaunchar.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import nooblaunchar.ModuleConfig;
import nooblaunchar.NoobConfig;

import java.util.Collections;
import java.util.List;

/**
 * Base class for every Noob_launchar module. A module owns an id, a display
 * name, persisted settings (via {@link ModuleConfig}) and, for HUD modules,
 * a lightweight render method. Non-HUD modules (behaviour toggles such as
 * Toggle Sprint) return {@code hasHud() == false}.
 */
public abstract class Module {

    /** A single cycle-style setting shown on the module's settings page. */
    public static class Opt {
        public final String key;
        public final String label;
        public final List<String> values;

        public Opt(String key, String label, List<String> values) {
            this.key = key;
            this.label = label;
            this.values = values;
        }
    }

    public final String id;
    public final String name;

    protected Module(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ModuleConfig cfg() {
        return NoobConfig.module(id);
    }

    public boolean enabled() {
        return cfg().enabled;
    }

    /** Whether this module draws something on the HUD. */
    public boolean hasHud() {
        return true;
    }

    /** Whether the generic "background panel" setting applies to this module. */
    public boolean supportsBackground() {
        return true;
    }

    /** Cycle-style options exposed on the settings page. */
    public List<Opt> options() {
        return Collections.emptyList();
    }

    /** Current value of an option, falling back to its first declared value. */
    public String opt(String key) {
        String v = cfg().options.get(key);
        if (v != null) return v;
        for (Opt o : options()) {
            if (o.key.equals(key) && !o.values.isEmpty()) return o.values.get(0);
        }
        return "";
    }

    public void cycleOpt(Opt o) {
        if (o.values.isEmpty()) return;
        int idx = o.values.indexOf(opt(o.key));
        cfg().options.put(o.key, o.values.get((idx + 1) % o.values.size()));
        NoobConfig.save();
    }

    /** Effective on-screen position: stored position, or the default anchor when unset (-1). */
    public int[] pos(Minecraft mc) {
        ModuleConfig c = cfg();
        if (c.x >= 0 && c.y >= 0) return new int[]{c.x, c.y};
        return defaultPos(mc);
    }

    public abstract int[] defaultPos(Minecraft mc);

    public abstract int width(Minecraft mc);

    public abstract int height(Minecraft mc);

    /** Draws the module's HUD element with its top-left corner at (x, y). Only called when enabled. */
    public abstract void render(Minecraft mc, int x, int y);

    // ------------------------------------------------------------------
    // Shared drawing helpers
    // ------------------------------------------------------------------

    protected static final int PAD_X = 4;
    protected static final int PAD_Y = 3;
    protected static final int LINE_STEP = 10;
    protected static final int BG_COLOR = 0x90101418; // translucent dark panel

    protected int textColor() {
        return 0xFF000000 | (cfg().color & 0xFFFFFF);
    }

    /** Fills a w x h rect using Gui.drawRect's (left, top, right, bottom) form. */
    protected static void fill(int x, int y, int w, int h, int color) {
        Gui.drawRect(x, y, x + w, y + h, color);
    }

    /** Draws the translucent background panel for a w x h element if the setting is on. */
    protected void drawBackground(int x, int y, int w, int h) {
        if (cfg().background) {
            fill(x, y, w, h, BG_COLOR);
        }
    }

    protected static int linesWidth(List<String> lines, FontRenderer font) {
        int w = 0;
        for (String s : lines) w = Math.max(w, font.getStringWidth(s));
        return w + PAD_X * 2;
    }

    protected static int linesHeight(int lineCount) {
        return PAD_Y * 2 + (lineCount - 1) * LINE_STEP + 9;
    }

    /** Renders plain text lines with the module's color/background at (x, y). */
    protected void renderLines(Minecraft mc, int x, int y, List<String> lines) {
        FontRenderer font = mc.fontRendererObj;
        int w = linesWidth(lines, font);
        int h = linesHeight(Math.max(1, lines.size()));
        drawBackground(x, y, w, h);
        int color = textColor();
        for (int i = 0; i < lines.size(); i++) {
            font.drawString(lines.get(i), x + PAD_X, y + PAD_Y + i * LINE_STEP, color);
        }
    }

    /** Capitalizes the first letter of a lowercase id/path fragment. */
    protected static String pretty(String s) {
        if (s == null || s.isEmpty()) return s;
        String spaced = s.replace('_', ' ');
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
