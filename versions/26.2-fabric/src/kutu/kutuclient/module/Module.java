package kutu.kutuclient.module;

import kutu.kutuclient.KutuConfig;
import kutu.kutuclient.ModuleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * Base class for every KUTU Client module. A module owns an id, a display
 * name, persisted settings (via {@link ModuleConfig}) and, for HUD modules,
 * a lightweight render method. Non-HUD modules (behaviour toggles such as
 * Toggle Sprint) return {@code hasHud() == false}.
 */
public abstract class Module {

    /** A single cycle-style setting shown on the module's settings page. */
    public record Opt(String key, String label, List<String> values) {
        public Opt(String key, String label, String... values) {
            this(key, label, List.of(values));
        }
    }

    public final String id;
    public final String name;

    protected Module(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ModuleConfig cfg() {
        return KutuConfig.module(id);
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
        return List.of();
    }

    /** Current value of an option, falling back to its first declared value. */
    public String opt(String key) {
        String v = cfg().options.get(key);
        if (v != null) return v;
        for (Opt o : options()) {
            if (o.key().equals(key) && !o.values().isEmpty()) return o.values().get(0);
        }
        return "";
    }

    public void cycleOpt(Opt o) {
        List<String> values = o.values();
        if (values.isEmpty()) return;
        int idx = values.indexOf(opt(o.key()));
        cfg().options.put(o.key(), values.get((idx + 1) % values.size()));
        KutuConfig.save();
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
    public abstract void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y);

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

    /** Draws the translucent background panel for a w x h element if the setting is on. */
    protected void drawBackground(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (cfg().background) {
            g.fill(x, y, x + w, y + h, BG_COLOR);
        }
    }

    protected static int textWidth(Minecraft mc, Font font, List<String> lines) {
        int w = 0;
        for (String s : lines) w = Math.max(w, font.width(s));
        return w;
    }

    /** Standard single/multi-line text element size. */
    protected static int linesWidth(List<String> lines, Font font) {
        return textWidth(null, font, lines) + PAD_X * 2;
    }

    protected static int linesHeight(int lineCount) {
        return PAD_Y * 2 + (lineCount - 1) * LINE_STEP + 9;
    }

    /** Renders plain text lines with the module's color/background at (x, y). */
    protected void renderLines(GuiGraphicsExtractor g, Minecraft mc, int x, int y, List<String> lines) {
        Font font = mc.font;
        int w = linesWidth(lines, font);
        int h = linesHeight(Math.max(1, lines.size()));
        drawBackground(g, x, y, w, h);
        int color = textColor();
        for (int i = 0; i < lines.size(); i++) {
            g.text(font, lines.get(i), x + PAD_X, y + PAD_Y + i * LINE_STEP, color);
        }
    }

    /** Capitalizes the first letter of a lowercase id/path fragment. */
    protected static String pretty(String s) {
        if (s == null || s.isEmpty()) return s;
        String spaced = s.replace('_', ' ');
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
