package noobmodantor.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import noobmodantor.NoobConfig;
import noobmodantor.module.Module;

import java.util.Arrays;
import java.util.List;

/**
 * Shared dark-theme drawing helpers for the Noob_mod_antor screens, plus the
 * click-zone plumbing: screens rebuild their zone list on every draw and
 * dispatch clicks through it. See shared/DESIGN.md for the palette.
 */
public final class Ui {
    public static final int PANEL_BG = 0xE60C1015;
    public static final int PANEL_BORDER = 0xFF2B3442;
    public static final int ROW_HOVER = 0x33FFFFFF;
    public static final int ACCENT = 0xFF2DD4BF;
    public static final int TEXT = 0xFFF2F5F7;
    public static final int TEXT_DIM = 0xFF9AA7B4;
    public static final int PILL_OFF = 0xFF3A4353;

    /**
     * The live menu accent: follows the global appearance choice — the
     * global text/accent color preset, or smooth chroma cycling when
     * global chroma is on. ACCENT above remains the factory default.
     */
    public static int accent() {
        NoobConfig cfg = NoobConfig.get();
        if (cfg.globalChroma) {
            return 0xFF000000 | Module.chromaRgb(System.currentTimeMillis());
        }
        return 0xFF000000 | (cfg.globalTextColor & 0xFFFFFF);
    }
    public static final int CHIP_BG = 0xFF1B222C;
    public static final int BUTTON_BG = 0xFF1B222C;
    public static final int BUTTON_HOVER = 0xFF27313F;

    /** Named text-color presets cycled on the settings page. */
    public static class ColorPreset {
        public final String name;
        public final int rgb;

        public ColorPreset(String name, int rgb) {
            this.name = name;
            this.rgb = rgb;
        }
    }

    public static final List<ColorPreset> COLORS = Arrays.asList(
            new ColorPreset("White", 0xFFFFFF),
            new ColorPreset("Teal", 0x2DD4BF),
            new ColorPreset("Yellow", 0xFFFF55),
            new ColorPreset("Aqua", 0x55FFFF),
            new ColorPreset("Green", 0x55FF55),
            new ColorPreset("Pink", 0xFF55FF),
            new ColorPreset("Orange", 0xFFAA00),
            new ColorPreset("Red", 0xFF5555),
            new ColorPreset("Gray", 0xAAAAAA)
    );

    public static String colorName(int rgb) {
        for (ColorPreset p : COLORS) if (p.rgb == (rgb & 0xFFFFFF)) return p.name;
        return "Custom";
    }

    public static int nextColor(int rgb) {
        for (int i = 0; i < COLORS.size(); i++) {
            if (COLORS.get(i).rgb == (rgb & 0xFFFFFF)) {
                return COLORS.get((i + 1) % COLORS.size()).rgb;
            }
        }
        return COLORS.get(0).rgb;
    }

    private Ui() {
    }

    public static class Zone {
        public final int x, y, w, h;
        public final Runnable action;

        public Zone(int x, int y, int w, int h, Runnable action) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.action = action;
        }

        public boolean contains(int mx, int my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    /** Runs the first zone containing the point; returns true if one matched. */
    public static boolean click(List<Zone> zones, int mx, int my) {
        for (Zone z : zones) {
            if (z.contains(mx, my)) {
                z.action.run();
                return true;
            }
        }
        return false;
    }

    public static void fill(int x, int y, int w, int h, int color) {
        Gui.drawRect(x, y, x + w, y + h, color);
    }

    public static void outline(int x, int y, int w, int h, int color) {
        fill(x, y, w, 1, color);
        fill(x, y + h - 1, w, 1, color);
        fill(x, y, 1, h, color);
        fill(x + w - 1, y, 1, h, color);
    }

    public static void panel(int x, int y, int w, int h) {
        fill(x, y, w, h, PANEL_BG);
        outline(x, y, w, h, PANEL_BORDER);
    }

    /** ON/OFF pill switch, 30 x 14. */
    public static void pill(int x, int y, boolean on) {
        fill(x, y, 30, 14, on ? accent() : PILL_OFF);
        int knobX = on ? x + 17 : x + 3;
        fill(knobX, y + 3, 10, 8, 0xFFF2F5F7);
    }

    public static void centeredText(FontRenderer font, String text, int centerX, int y, int color) {
        font.drawString(text, centerX - font.getStringWidth(text) / 2, y, color);
    }

    public static void chip(FontRenderer font, int x, int y, int w, String value, int valueColor) {
        fill(x, y, w, 16, CHIP_BG);
        outline(x, y, w, 16, PANEL_BORDER);
        centeredText(font, value, x + w / 2, y + 4, valueColor);
    }

    public static void button(FontRenderer font, int x, int y, int w, int h, String label, boolean hovered) {
        fill(x, y, w, h, hovered ? BUTTON_HOVER : BUTTON_BG);
        outline(x, y, w, h, PANEL_BORDER);
        centeredText(font, label, x + w / 2, y + (h - 9) / 2 + 1, TEXT);
    }

    public static void accentLine(int x, int y, int w) {
        fill(x, y, w, 1, accent());
    }
}
