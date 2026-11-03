package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import noobmodantor.ModuleConfig;
import noobmodantor.NoobConfig;

import java.util.Collections;
import java.util.List;

/**
 * Base class for every Noob_mod_antor module. A module owns an id, a display
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

    /** A numeric slider setting shown on the module's settings page. The value is stored in the options map as a string. */
    public static class SliderOpt {
        public final String key;
        public final String label;
        public final double min;
        public final double max;
        public final double step;
        public final double def;

        public SliderOpt(String key, String label, double min, double max, double step, double def) {
            this.key = key;
            this.label = label;
            this.min = min;
            this.max = max;
            this.step = step;
            this.def = def;
        }
    }

    /** A free-text setting shown on the module's settings page (edited inline with the keyboard). */
    public static class TextOpt {
        public final String key;
        public final String label;
        public final String def;

        public TextOpt(String key, String label, String def) {
            this.key = key;
            this.label = label;
            this.def = def;
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

    /** Whether the settings page should show a color row even for non-HUD modules (e.g. Hitbox box color). */
    public boolean usesColor() {
        return false;
    }

    /** Whether the settings page should show background rows even for non-HUD modules (e.g. Nametag plate background). */
    public boolean usesBackground() {
        return false;
    }

    // ------------------------------------------------------------------
    // Appearance system (per-module overrides over global defaults)
    //
    // Stored in the module's options map under "apx.*" keys so existing
    // configs load unchanged; anything unset falls back to the global
    // appearance defaults in NoobConfig (edited on the Appearance page).
    // ------------------------------------------------------------------

    /** Background mode for this module's HUD panel: Clear / Box / Custom. */
    public String bgMode() {
        String v = cfg().options.get("apx.bg_mode");
        if (v != null) return v;
        if (!cfg().background) return "Clear"; // legacy boolean off = clear
        String g = NoobConfig.get().globalBgMode;
        return g != null ? g : "Box";
    }

    public void setBgMode(String mode) {
        cfg().options.put("apx.bg_mode", mode);
        cfg().background = !"Clear".equals(mode);
        NoobConfig.save();
    }

    /** Background opacity in percent (0-100) for Box/Custom modes. */
    public int bgOpacityPct() {
        String v = cfg().options.get("apx.bg_opacity");
        if (v != null) {
            try {
                int p = Integer.parseInt(v.trim());
                return Math.max(0, Math.min(100, p));
            } catch (NumberFormatException ignored) {
            }
        }
        return NoobConfig.get().globalBgOpacity;
    }

    /** Background RGB for Custom mode. */
    public int bgColorRgb() {
        String v = cfg().options.get("apx.bg_color");
        if (v != null) {
            try {
                return Integer.parseInt(v.trim()) & 0xFFFFFF;
            } catch (NumberFormatException ignored) {
            }
        }
        return NoobConfig.get().globalBgColor & 0xFFFFFF;
    }

    public void setBgColorRgb(int rgb) {
        cfg().options.put("apx.bg_color", Integer.toString(rgb & 0xFFFFFF));
        NoobConfig.save();
    }

    /** Chroma (rainbow-cycling) text for this module. */
    public boolean chromaOn() {
        String v = cfg().options.get("apx.chroma");
        if (v != null) return "On".equals(v);
        return NoobConfig.get().globalChroma;
    }

    public void setChromaOn(boolean on) {
        cfg().options.put("apx.chroma", on ? "On" : "Off");
        NoobConfig.save();
    }

    /** Smooth rainbow color for a point in time (cheap: one HSB conversion). */
    public static int chromaRgb(long millis) {
        float hue = (millis % 6000L) / 6000.0f;
        return java.awt.Color.HSBtoRGB(hue, 0.65f, 1.0f) & 0xFFFFFF;
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

    /** Slider-style options exposed on the settings page. */
    public List<SliderOpt> sliders() {
        return Collections.emptyList();
    }

    /** Current slider value, snapped to its step and clamped to its range. Falls back to the declared default. */
    public double sliderVal(SliderOpt s) {
        String v = cfg().options.get(s.key);
        if (v != null) {
            try {
                return snapSlider(s, Double.parseDouble(v));
            } catch (NumberFormatException ignored) {
            }
        }
        return snapSlider(s, s.def);
    }

    public void setSlider(SliderOpt s, double value) {
        double snapped = snapSlider(s, value);
        String str = (snapped == Math.floor(snapped) && !Double.isInfinite(snapped))
                ? Integer.toString((int) Math.round(snapped))
                : Double.toString(snapped);
        cfg().options.put(s.key, str);
        NoobConfig.save();
    }

    private static double snapSlider(SliderOpt s, double value) {
        double snapped = s.min + Math.round((value - s.min) / s.step) * s.step;
        return Math.max(s.min, Math.min(s.max, snapped));
    }

    /** Text-style options exposed on the settings page. */
    public List<TextOpt> textOpts() {
        return Collections.emptyList();
    }

    public String textVal(TextOpt t) {
        String v = cfg().options.get(t.key);
        return v != null ? v : t.def;
    }

    public void setText(TextOpt t, String value) {
        cfg().options.put(t.key, value);
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
        if (chromaOn()) {
            return 0xFF000000 | chromaRgb(System.currentTimeMillis());
        }
        return 0xFF000000 | (cfg().color & 0xFFFFFF);
    }

    /** Fills a w x h rect using Gui.drawRect's (left, top, right, bottom) form. */
    protected static void fill(int x, int y, int w, int h, int color) {
        Gui.drawRect(x, y, x + w, y + h, color);
    }

    /**
     * Draws this module's background panel according to the appearance
     * system: Clear = nothing, Box = the standard dark panel, Custom =
     * the chosen background color; Box/Custom use the opacity setting.
     */
    protected void drawBackground(int x, int y, int w, int h) {
        String mode = bgMode();
        if ("Clear".equals(mode)) return;
        int alpha = (int) Math.round(bgOpacityPct() / 100.0 * 255.0);
        if (alpha <= 0) return;
        int rgb = "Custom".equals(mode) ? bgColorRgb() : (BG_COLOR & 0xFFFFFF);
        fill(x, y, w, h, (alpha << 24) | rgb);
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
