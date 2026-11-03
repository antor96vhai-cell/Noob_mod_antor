package kutu.kutuclient.gui;

import kutu.kutuclient.KutuConfig;
import kutu.kutuclient.ModuleConfig;
import kutu.kutuclient.module.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-module settings page: enabled toggle, text color (cycle), background
 * toggle, the module's own cycle options, and a HUD position reset.
 */
public class ModuleSettingsScreen extends Screen {
    private static final int PANEL_W = 300;
    private static final int ROW_H = 24;

    private final Screen parent;
    private final Module module;
    private final List<Ui.Zone> zones = new ArrayList<>();

    public ModuleSettingsScreen(Screen parent, Module module) {
        super(Component.literal(module.name));
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        zones.clear();
        ModuleConfig cfg = module.cfg();

        int rowCount = 1 // enabled
                + (module.hasHud() ? 1 : 0) // color
                + (module.hasHud() && module.supportsBackground() ? 1 : 0) // background
                + module.options().size()
                + (module.hasHud() ? 1 : 0); // reset position
        int panelH = 46 + rowCount * ROW_H + 36;
        int px = (this.width - PANEL_W) / 2;
        int py = Math.max(10, (this.height - panelH) / 2);

        Ui.panel(graphics, px, py, PANEL_W, panelH);
        Ui.button(graphics, this.font, px + 10, py + 8, 52, 18, "< Back", false);
        zones.add(new Ui.Zone(px + 10, py + 8, 52, 18, this::onClose));
        graphics.centeredText(this.font, module.name, px + PANEL_W / 2 + 26, py + 13, Ui.ACCENT);
        Ui.accentLine(graphics, px + 10, py + 34, PANEL_W - 20);

        int y = py + 42;

        // Enabled
        y = row(graphics, px, y, "Enabled");
        Ui.pill(graphics, px + PANEL_W - 44, y + 1, cfg.enabled);
        zones.add(new Ui.Zone(px + PANEL_W - 44, y, 30, ROW_H - 4, () -> {
            cfg.enabled = !cfg.enabled;
            KutuConfig.save();
        }));
        y += ROW_H;

        if (module.hasHud()) {
            // Text color
            y = row(graphics, px, y, "Text color");
            int chipW = 92;
            int chipX = px + PANEL_W - 12 - chipW;
            Ui.chip(graphics, this.font, chipX, y, chipW, Ui.colorName(cfg.color), 0xFF000000 | (cfg.color & 0xFFFFFF));
            zones.add(new Ui.Zone(chipX, y - 2, chipW, 20, () -> {
                cfg.color = Ui.nextColor(cfg.color);
                KutuConfig.save();
            }));
            y += ROW_H;

            // Background
            if (module.supportsBackground()) {
                y = row(graphics, px, y, "Background");
                Ui.pill(graphics, px + PANEL_W - 44, y + 1, cfg.background);
                zones.add(new Ui.Zone(px + PANEL_W - 44, y, 30, ROW_H - 4, () -> {
                    cfg.background = !cfg.background;
                    KutuConfig.save();
                }));
                y += ROW_H;
            }
        }

        // Module-specific options
        for (Module.Opt opt : module.options()) {
            y = row(graphics, px, y, opt.label());
            int chipW = 92;
            int chipX = px + PANEL_W - 12 - chipW;
            Ui.chip(graphics, this.font, chipX, y, chipW, module.opt(opt.key()), Ui.TEXT);
            zones.add(new Ui.Zone(chipX, y - 2, chipW, 20, () -> module.cycleOpt(opt)));
            y += ROW_H;
        }

        if (module.hasHud()) {
            // Reset position
            y = row(graphics, px, y, "HUD position");
            int chipW = 92;
            int chipX = px + PANEL_W - 12 - chipW;
            Ui.chip(graphics, this.font, chipX, y, chipW, (cfg.x >= 0 ? "Reset" : "Default"), Ui.TEXT_DIM);
            zones.add(new Ui.Zone(chipX, y - 2, chipW, 20, () -> {
                cfg.x = -1;
                cfg.y = -1;
                KutuConfig.save();
            }));
            y += ROW_H;
        }

        int btnY = py + panelH - 30;
        Ui.button(graphics, this.font, px + PANEL_W / 2 - 50, btnY, 100, 20, "Back", false);
        zones.add(new Ui.Zone(px + PANEL_W / 2 - 50, btnY, 100, 20, this::onClose));
    }

    /** Draws a settings row label; returns the row's content top y. */
    private int row(GuiGraphicsExtractor graphics, int px, int y, String label) {
        graphics.text(this.font, label, px + 14, y + 4, Ui.TEXT);
        return y + 2;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && Ui.click(zones, event.x(), event.y())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
