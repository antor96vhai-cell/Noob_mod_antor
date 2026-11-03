package nooblaunchar.gui;

import net.minecraft.client.gui.GuiScreen;
import nooblaunchar.ModuleConfig;
import nooblaunchar.NoobConfig;
import nooblaunchar.module.Module;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-module settings page: enabled toggle, text color (cycle), background
 * toggle, the module's own cycle options, and a HUD position reset.
 * Esc returns to the parent screen instead of closing to the game.
 */
public class ModuleSettingsScreen extends GuiScreen {
    private static final int PANEL_W = 300;
    private static final int ROW_H = 24;

    private final GuiScreen parent;
    private final Module module;
    private final List<Ui.Zone> zones = new ArrayList<Ui.Zone>();

    public ModuleSettingsScreen(GuiScreen parent, Module module) {
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        zones.clear();
        final ModuleConfig cfg = module.cfg();

        int rowCount = 1 // enabled
                + (module.hasHud() ? 1 : 0) // color
                + (module.hasHud() && module.supportsBackground() ? 1 : 0) // background
                + module.options().size()
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
        Ui.centeredText(this.fontRendererObj, module.name, px + PANEL_W / 2 + 26, py + 13, Ui.ACCENT);
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

        if (module.hasHud()) {
            // Text color
            y = row(px, y, "Text color");
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

            // Background
            if (module.supportsBackground()) {
                y = row(px, y, "Background");
                Ui.pill(px + PANEL_W - 44, y + 1, cfg.background);
                zones.add(new Ui.Zone(px + PANEL_W - 44, y, 30, ROW_H - 4, new Runnable() {
                    @Override
                    public void run() {
                        cfg.background = !cfg.background;
                        NoobConfig.save();
                    }
                }));
                y += ROW_H;
            }
        }

        // Module-specific options
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

    /** Draws a settings row label; returns the row's content top y. */
    private int row(int px, int y, String label) {
        this.fontRendererObj.drawString(label, px + 14, y + 4, Ui.TEXT);
        return y + 2;
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
