package noobmodantor.gui;

import net.minecraft.client.gui.GuiScreen;
import noobmodantor.ModuleConfig;
import noobmodantor.NoobConfig;
import noobmodantor.module.Module;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-module settings page: enabled toggle, text color (cycle), background
 * toggle, the module's own cycle options, slider options (dragged with the
 * mouse), text options (edited inline with the keyboard), and a HUD
 * position reset. Esc returns to the parent screen instead of closing to
 * the game (while a text field is being edited, Esc just ends the edit).
 */
public class ModuleSettingsScreen extends GuiScreen {
    private static final int PANEL_W = 300;
    private static final int ROW_H = 24;

    private final GuiScreen parent;
    private final Module module;
    private final List<Ui.Zone> zones = new ArrayList<Ui.Zone>();
    private final List<SliderHit> sliderHits = new ArrayList<SliderHit>();

    /** Slider currently being dragged, and the track geometry captured at drag start. */
    private Module.SliderOpt dragging;
    private int dragTrackX;
    private int dragTrackW;
    /** Text option currently being edited inline, or null. */
    private Module.TextOpt editing;

    private static class SliderHit {
        final int x, y, w, h, trackX, trackW;
        final Module.SliderOpt slider;

        SliderHit(int x, int y, int w, int h, int trackX, int trackW, Module.SliderOpt slider) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.trackX = trackX;
            this.trackW = trackW;
            this.slider = slider;
        }

        boolean contains(int mx, int my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    public ModuleSettingsScreen(GuiScreen parent, Module module) {
        this.parent = parent;
        this.module = module;
    }

    /** The background-opacity slider edits the module's appearance option directly. */
    private Module.SliderOpt bgOpacitySlider() {
        return new Module.SliderOpt("apx.bg_opacity", "BG opacity", 0, 100, 5,
                NoobConfig.get().globalBgOpacity);
    }

    /** Module sliders plus the appearance opacity slider when it applies. */
    private java.util.List<Module.SliderOpt> allSliders(boolean showOpacity) {
        java.util.List<Module.SliderOpt> all = new ArrayList<Module.SliderOpt>(module.sliders());
        if (showOpacity) all.add(bgOpacitySlider());
        return all;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        zones.clear();
        sliderHits.clear();
        final ModuleConfig cfg = module.cfg();

        final boolean showColor = module.hasHud() || module.usesColor();
        final boolean showBg = module.supportsBackground() && (module.hasHud() || module.usesBackground());
        final String bgMode = showBg ? module.bgMode() : "Box";
        final boolean showOpacity = showBg && !"Clear".equals(bgMode);
        final boolean showBgColor = showBg && "Custom".equals(bgMode);
        final java.util.List<Module.SliderOpt> sliders = allSliders(showOpacity);

        int rowCount = 1 // enabled
                + (showColor ? 1 : 0) // color
                + (module.hasHud() ? 1 : 0) // chroma
                + (showBg ? 1 : 0) // background mode
                + (showBgColor ? 1 : 0) // background color
                + module.options().size()
                + sliders.size()
                + module.textOpts().size()
                + (module.hasHud() ? 1 : 0); // reset position
        int panelH = 46 + rowCount * ROW_H + 36;
        final int px = (this.width - PANEL_W) / 2;
        int py = Math.max(10, (this.height - panelH) / 2);

        Ui.panel(px, py, PANEL_W, panelH);
        Ui.button(this.fontRendererObj, px + 10, py + 8, 52, 18, "< Back", false);
        zones.add(new Ui.Zone(px + 10, py + 8, 52, 18, new Runnable() {
            @Override
            public void run() {
                mc.displayGuiScreen(parent);
            }
        }));
        Ui.centeredText(this.fontRendererObj, module.name, px + PANEL_W / 2 + 26, py + 13, Ui.accent());
        Ui.accentLine(px + 10, py + 34, PANEL_W - 20);

        int y = py + 42;

        // Enabled
        y = row(px, y, "Enabled");
        Ui.pill(px + PANEL_W - 44, y + 1, cfg.enabled);
        zones.add(new Ui.Zone(px + PANEL_W - 44, y, 30, ROW_H - 4, new Runnable() {
            @Override
            public void run() {
                cfg.enabled = !cfg.enabled;
                NoobConfig.save();
            }
        }));
        y += ROW_H;

        if (showColor) {
            // Text color (for non-HUD color users like Hitbox: box color)
            y = row(px, y, module.hasHud() ? "Text color" : "Color");
            int chipW = 92;
            final int chipX = px + PANEL_W - 12 - chipW;
            Ui.chip(this.fontRendererObj, chipX, y, chipW, Ui.colorName(cfg.color), 0xFF000000 | (cfg.color & 0xFFFFFF));
            zones.add(new Ui.Zone(chipX, y - 2, chipW, 20, new Runnable() {
                @Override
                public void run() {
                    cfg.color = Ui.nextColor(cfg.color);
                    NoobConfig.save();
                }
            }));
            y += ROW_H;
        }

        if (module.hasHud()) {
            // Chroma text (rainbow cycling overrides the static color)
            y = row(px, y, "Chroma text");
            Ui.pill(px + PANEL_W - 44, y + 1, module.chromaOn());
            zones.add(new Ui.Zone(px + PANEL_W - 44, y, 30, ROW_H - 4, new Runnable() {
                @Override
                public void run() {
                    module.setChromaOn(!module.chromaOn());
                }
            }));
            y += ROW_H;
        }

        // Background: mode (Clear / Box / Custom), color for Custom;
        // the opacity slider joins the slider rows below. Shown for HUD
        // modules and for non-HUD modules that use a background (Nametag).
        if (showBg) {
            y = row(px, y, "Background");
            int chipW = 92;
            final int chipX = px + PANEL_W - 12 - chipW;
            Ui.chip(this.fontRendererObj, chipX, y, chipW, bgMode, Ui.TEXT);
            zones.add(new Ui.Zone(chipX, y - 2, chipW, 20, new Runnable() {
                @Override
                public void run() {
                    String cur = module.bgMode();
                    String next = "Clear".equals(cur) ? "Box" : ("Box".equals(cur) ? "Custom" : "Clear");
                    module.setBgMode(next);
                }
            }));
            y += ROW_H;

            if (showBgColor) {
                y = row(px, y, "BG color");
                final int chipX2 = px + PANEL_W - 12 - chipW;
                Ui.chip(this.fontRendererObj, chipX2, y, chipW, Ui.colorName(module.bgColorRgb()),
                        0xFF000000 | module.bgColorRgb());
                zones.add(new Ui.Zone(chipX2, y - 2, chipW, 20, new Runnable() {
                    @Override
                    public void run() {
                        module.setBgColorRgb(Ui.nextColor(module.bgColorRgb()));
                    }
                }));
                y += ROW_H;
            }
        }

        // Module-specific cycle options
        for (final Module.Opt opt : module.options()) {
            y = row(px, y, opt.label);
            int chipW = 92;
            final int chipX2 = px + PANEL_W - 12 - chipW;
            Ui.chip(this.fontRendererObj, chipX2, y, chipW, module.opt(opt.key), Ui.TEXT);
            zones.add(new Ui.Zone(chipX2, y - 2, chipW, 20, new Runnable() {
                @Override
                public void run() {
                    module.cycleOpt(opt);
                }
            }));
            y += ROW_H;
        }

        // Slider options (draggable), incl. the BG opacity slider
        for (final Module.SliderOpt slider : sliders) {
            final int contentTop = row(px, y, slider.label);
            final int trackX = px + 108;
            final int trackW = PANEL_W - 108 - 56;
            final int trackY = contentTop + 5;
            double val = module.sliderVal(slider);
            double prop = (val - slider.min) / (slider.max - slider.min);
            Ui.fill(trackX, trackY, trackW, 4, Ui.CHIP_BG);
            Ui.fill(trackX, trackY, (int) Math.round(trackW * prop), 4, Ui.accent());
            int knobX = trackX + (int) Math.round(trackW * prop);
            Ui.fill(knobX - 2, contentTop + 2, 5, 10, Ui.TEXT);
            Ui.centeredText(this.fontRendererObj, formatVal(slider, val), px + PANEL_W - 32, contentTop + 2, Ui.TEXT);
            sliderHits.add(new SliderHit(trackX - 4, contentTop - 2, trackW + 8, 18, trackX, trackW, slider));
            y += ROW_H;
        }

        // Text options (click to edit inline)
        for (final Module.TextOpt textOpt : module.textOpts()) {
            final int contentTop = row(px, y, textOpt.label);
            int chipW = PANEL_W - 108 - 12;
            final int chipX = px + 108;
            boolean isEditing = editing == textOpt;
            String shown = module.textVal(textOpt) + (isEditing ? "_" : "");
            Ui.chip(this.fontRendererObj, chipX, contentTop, chipW, shown, isEditing ? Ui.accent() : Ui.TEXT);
            zones.add(new Ui.Zone(chipX, contentTop - 2, chipW, 20, new Runnable() {
                @Override
                public void run() {
                    editing = textOpt;
                }
            }));
            y += ROW_H;
        }

        if (module.hasHud()) {
            // Reset position
            y = row(px, y, "HUD position");
            int chipW = 92;
            final int chipX3 = px + PANEL_W - 12 - chipW;
            Ui.chip(this.fontRendererObj, chipX3, y, chipW, (cfg.x >= 0 ? "Reset" : "Default"), Ui.TEXT_DIM);
            zones.add(new Ui.Zone(chipX3, y - 2, chipW, 20, new Runnable() {
                @Override
                public void run() {
                    cfg.x = -1;
                    cfg.y = -1;
                    NoobConfig.save();
                }
            }));
            y += ROW_H;
        }

        int btnY = py + panelH - 30;
        Ui.button(this.fontRendererObj, px + PANEL_W / 2 - 50, btnY, 100, 20, "Back", false);
        zones.add(new Ui.Zone(px + PANEL_W / 2 - 50, btnY, 100, 20, new Runnable() {
            @Override
            public void run() {
                mc.displayGuiScreen(parent);
            }
        }));
    }

    private static String formatVal(Module.SliderOpt s, double val) {
        if (s.step >= 1.0) return Integer.toString((int) Math.round(val));
        return String.format("%.1f", val);
    }

    /** Draws a settings row label; returns the row's content top y. */
    private int row(int px, int y, String label) {
        this.fontRendererObj.drawString(label, px + 14, y + 4, Ui.TEXT);
        return y + 2;
    }

    private void applySlider(int mouseX) {
        double prop = (mouseX - dragTrackX) / (double) dragTrackW;
        if (prop < 0) prop = 0;
        if (prop > 1) prop = 1;
        module.setSlider(dragging, dragging.min + prop * (dragging.max - dragging.min));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for (SliderHit hit : sliderHits) {
                if (hit.contains(mouseX, mouseY)) {
                    dragging = hit.slider;
                    dragTrackX = hit.trackX;
                    dragTrackW = hit.trackW;
                    editing = null;
                    applySlider(mouseX);
                    return;
                }
            }
            // Clicking a text field starts editing it; clicking anything else ends the edit.
            Module.TextOpt before = editing;
            if (Ui.click(zones, mouseX, mouseY)) {
                if (editing == before) editing = null;
                return;
            }
            editing = null;
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging != null && clickedMouseButton == 0) {
            applySlider(mouseX);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = null;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (editing != null) {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER || keyCode == Keyboard.KEY_ESCAPE) {
                editing = null;
                return;
            }
            String cur = module.textVal(editing);
            if (keyCode == Keyboard.KEY_BACK) {
                if (!cur.isEmpty()) module.setText(editing, cur.substring(0, cur.length() - 1));
                return;
            }
            if (typedChar >= 32 && typedChar != 127 && cur.length() < 64) {
                module.setText(editing, cur + typedChar);
            }
            return;
        }
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
