package kutu.kutuclient.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * Shared dark-theme drawing helpers for the KUTU Client screens, plus the
 * click-zone plumbing: screens rebuild their zone list on every render and
 * dispatch clicks through it.
 */
public final class Ui {
    public static final int PANEL_BG = 0xE60C1015;
    public static final int PANEL_BORDER = 0xFF2B3442;
    public static final int ROW_HOVER = 0x33FFFFFF;
    public static final int ACCENT = 0xFF2DD4BF;
    public static final int TEXT = 0xFFF2F5F7;
    public static final int TEXT_DIM = 0xFF9AA7B4;
    public static final int PILL_OFF = 0xFF3A4353;
    public static final int CHIP_BG = 0xFF1B222C;
    public static final int BUTTON_BG = 0xFF1B222C;
    public static final int BUTTON_HOVER = 0xFF27313F;

    /** Named text-color presets cycled on the settings page. */
    public record ColorPreset(String name, int rgb) {
    }

    public static final List<ColorPreset> COLORS = List.of(
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
        for (ColorPreset p : COLORS) if (p.rgb() == (rgb & 0xFFFFFF)) return p.name();
        return "Custom";
    }

    public static int nextColor(int rgb) {
        for (int i = 0; i < COLORS.size(); i++) {
            if (COLORS.get(i).rgb() == (rgb & 0xFFFFFF)) {
                return COLORS.get((i + 1) % COLORS.size()).rgb();
            }
        }
        return COLORS.get(0).rgb();
    }

    private Ui() {
    }

    public record Zone(int x, int y, int w, int h, Runnable action) {
        public boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    /** Runs the first zone containing the point; returns true if one matched. */
    public static boolean click(List<Zone> zones, double mx, double my) {
        for (Zone z : zones) {
            if (z.contains(mx, my)) {
                z.action().run();
                return true;
            }
        }
        return false;
    }

    public static boolean hover(List<Zone> zones, double mx, double my) {
        for (Zone z : zones) if (z.contains(mx, my)) return true;
        return false;
    }

    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, PANEL_BG);
        g.outline(x, y, w, h, PANEL_BORDER);
    }

    /** ON/OFF pill switch, 30 x 14. */
    public static void pill(GuiGraphicsExtractor g, int x, int y, boolean on) {
        g.fill(x, y, x + 30, y + 14, on ? ACCENT : PILL_OFF);
        int knobX = on ? x + 17 : x + 3;
        g.fill(knobX, y + 3, knobX + 10, y + 11, 0xFFF2F5F7);
    }

    public static void chip(GuiGraphicsExtractor g, Font font, int x, int y, int w, String value, int valueColor) {
        g.fill(x, y, x + w, y + 16, CHIP_BG);
        g.outline(x, y, w, 16, PANEL_BORDER);
        g.centeredText(font, value, x + w / 2, y + 4, valueColor);
    }

    public static void button(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h,
                              String label, boolean hovered) {
        g.fill(x, y, x + w, y + h, hovered ? BUTTON_HOVER : BUTTON_BG);
        g.outline(x, y, w, h, PANEL_BORDER);
        g.centeredText(font, label, x + w / 2, y + (h - 9) / 2 + 1, TEXT);
    }

    public static void accentLine(GuiGraphicsExtractor g, int x, int y, int w) {
        g.fill(x, y, x + w, y + 1, ACCENT);
    }
}
