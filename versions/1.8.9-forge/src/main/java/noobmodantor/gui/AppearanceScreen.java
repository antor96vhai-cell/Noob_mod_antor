package noobmodantor.gui;

import net.minecraft.client.gui.GuiScreen;
import noobmodantor.NoobConfig;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Appearance page: edits the GLOBAL appearance defaults — text/accent
 * color preset (the menu accent follows it), chroma mode, HUD
 * background mode (Clear / Box / Custom), background color and
 * background opacity. Every HUD module starts from these defaults and
 * can override each of them on its own settings page.
 */
public class AppearanceScreen extends GuiScreen {
    private static final int PANEL_W = 300;
    private static final int ROW_H = 24;

    private final GuiScreen parent;
    private final List<Ui.Zone> zones = new ArrayList<Ui.Zone>();
    private boolean draggingOpacity = false;
    private int trackX;
    private int trackW;

    public AppearanceScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        zones.clear();
        final NoobConfig cfg = NoobConfig.get();

        int rows = 5; // accent color, chroma, bg mode, bg color, bg opacity
        int panelH = 46 + rows * ROW_H + 58;
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
        Ui.centeredText(this.fontRendererObj, "Appearance", px + PANEL_W / 2 + 26, py + 13, Ui.accent());
        Ui.accentLine(px + 10, py + 34, PANEL_W - 20);

        int y = py + 42;
        int chipW = 92;
        final int chipX = px + PANEL_W - 12 - chipW;

        // Accent / text color preset (menu accent follows this)
        y = row(px, y, "Accent & text color");
        Ui.chip(this.fontRendererObj, chipX, y, chipW, Ui.colorName(cfg.globalTextColor),
                0xFF000000 | (cfg.globalTextColor & 0xFFFFFF));
        zones.add(new Ui.Zone(chipX, y - 2, chipW, 20, new Runnable() {
            @Override
            public void run() {
                cfg.globalTextColor = Ui.nextColor(cfg.globalTextColor);
                NoobConfig.save();
            }
        }));
        y += ROW_H;

        // Chroma
        y = row(px, y, "Chroma (rainbow)");
        Ui.pill(px + PANEL_W - 44, y + 1, cfg.globalChroma);
        zones.add(new Ui.Zone(px + PANEL_W - 44, y, 30, ROW_H - 4, new Runnable() {
            @Override
            public void run() {
                cfg.globalChroma = !cfg.globalChroma;
                NoobConfig.save();
            }
        }));
        y += ROW_H;

        // Background mode
        y = row(px, y, "HUD background");
        Ui.chip(this.fontRendererObj, chipX, y, chipW, cfg.globalBgMode, Ui.TEXT);
        zones.add(new Ui.Zone(chipX, y - 2, chipW, 20, new Runnable() {
            @Override
            public void run() {
                String cur = cfg.globalBgMode;
                cfg.globalBgMode = "Clear".equals(cur) ? "Box" : ("Box".equals(cur) ? "Custom" : "Clear");
                NoobConfig.save();
            }
        }));
        y += ROW_H;

        // Background color (Custom mode)
        y = row(px, y, "BG color (Custom)");
        Ui.chip(this.fontRendererObj, chipX, y, chipW, Ui.colorName(cfg.globalBgColor),
                0xFF000000 | (cfg.globalBgColor & 0xFFFFFF));
        zones.add(new Ui.Zone(chipX, y - 2, chipW, 20, new Runnable() {
            @Override
            public void run() {
                cfg.globalBgColor = Ui.nextColor(cfg.globalBgColor);
                NoobConfig.save();
            }
        }));
        y += ROW_H;

        // Background opacity slider
        final int contentTop = row(px, y, "BG opacity");
        trackX = px + 108;
        trackW = PANEL_W - 108 - 56;
        int trackY = contentTop + 5;
        double prop = cfg.globalBgOpacity / 100.0;
        Ui.fill(trackX, trackY, trackW, 4, Ui.CHIP_BG);
        Ui.fill(trackX, trackY, (int) Math.round(trackW * prop), 4, Ui.accent());
        int knobX = trackX + (int) Math.round(trackW * prop);
        Ui.fill(knobX - 2, contentTop + 2, 5, 10, Ui.TEXT);
        Ui.centeredText(this.fontRendererObj, Integer.toString(cfg.globalBgOpacity), px + PANEL_W - 32, contentTop + 2, Ui.TEXT);
        y += ROW_H;

        y += 4;
        Ui.centeredText(this.fontRendererObj, "These are the defaults for every HUD module.", px + PANEL_W / 2, y, Ui.TEXT_DIM);
        y += 11;
        Ui.centeredText(this.fontRendererObj, "Each module can override them in its settings.", px + PANEL_W / 2, y, Ui.TEXT_DIM);

        int btnY = py + panelH - 30;
        Ui.button(this.fontRendererObj, px + PANEL_W / 2 - 50, btnY, 100, 20, "Back", false);
        zones.add(new Ui.Zone(px + PANEL_W / 2 - 50, btnY, 100, 20, new Runnable() {
            @Override
            public void run() {
                mc.displayGuiScreen(parent);
            }
        }));
    }

    private int row(int px, int y, String label) {
        this.fontRendererObj.drawString(label, px + 14, y + 4, Ui.TEXT);
        return y + 2;
    }

    private void applyOpacity(int mouseX) {
        double prop = (mouseX - trackX) / (double) trackW;
        if (prop < 0) prop = 0;
        if (prop > 1) prop = 1;
        int val = (int) Math.round(prop * 20.0) * 5; // snap to 5%
        NoobConfig.get().globalBgOpacity = val;
        NoobConfig.save();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            // Slider track sits on the last row; hit-test it before zones.
            int px = (this.width - PANEL_W) / 2;
            int sliderZoneY = 0; // computed from layout below
            // The opacity track's y depends on panel position; recompute:
            int rows = 5;
            int panelH = 46 + rows * ROW_H + 58;
            int py = Math.max(10, (this.height - panelH) / 2);
            sliderZoneY = py + 42 + 4 * ROW_H;
            if (mouseX >= trackX - 4 && mouseX < trackX + trackW + 4
                    && mouseY >= sliderZoneY - 2 && mouseY < sliderZoneY + 18) {
                draggingOpacity = true;
                applyOpacity(mouseX);
                return;
            }
            if (Ui.click(zones, mouseX, mouseY)) {
                return;
            }
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (draggingOpacity && clickedMouseButton == 0) {
            applyOpacity(mouseX);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        draggingOpacity = false;
        super.mouseReleased(mouseX, mouseY, state);
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
