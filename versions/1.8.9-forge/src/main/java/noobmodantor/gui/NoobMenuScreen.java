package noobmodantor.gui;

import net.minecraft.client.gui.GuiScreen;
import noobmodantor.NoobConfig;
import noobmodantor.module.Module;
import noobmodantor.module.Modules;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

/**
 * Main menu (Right Shift): a scrollable list of all modules. Each row has
 * a settings affordance (click the name) and an ON/OFF pill. Footer buttons
 * open the HUD editor or close the menu.
 */
public class NoobMenuScreen extends GuiScreen {
    private static final int PANEL_W = 320;
    private static final int ROW_H = 24;
    private static final int HEADER_H = 68;
    private static final int FOOTER_H = 34;

    private final List<Ui.Zone> zones = new ArrayList<Ui.Zone>();
    private int scroll = 0;
    /** Live name filter for the module list (SmartSearch-lite). */
    private String search = "";

    private int panelX() {
        return (this.width - PANEL_W) / 2;
    }

    private int panelY() {
        return 16;
    }

    private int panelH() {
        return Math.max(160, this.height - 32);
    }

    private int listTop() {
        return panelY() + HEADER_H;
    }

    private int listBottom() {
        return panelY() + panelH() - FOOTER_H;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        zones.clear();
        int px = panelX();
        int py = panelY();
        int ph = panelH();

        Ui.panel(px, py, PANEL_W, ph);
        Ui.centeredText(this.fontRendererObj, "Noob_mod_antor", px + PANEL_W / 2, py + 8, Ui.accent());
        Ui.centeredText(this.fontRendererObj, "Click a module for its settings", px + PANEL_W / 2, py + 21, Ui.TEXT_DIM);
        // Search box: type to filter the module list by name.
        int searchX = px + 10;
        int searchY = py + 33;
        int searchW = PANEL_W - 20;
        Ui.fill(searchX, searchY, searchW, 18, Ui.CHIP_BG);
        Ui.outline(searchX, searchY, searchW, 18, Ui.PANEL_BORDER);
        String shownSearch = search.isEmpty() ? "Search modules..." : search;
        this.fontRendererObj.drawString(shownSearch, searchX + 6, searchY + 5,
                search.isEmpty() ? Ui.TEXT_DIM : Ui.TEXT);
        if (!search.isEmpty()) {
            int caretX = searchX + 6 + this.fontRendererObj.getStringWidth(search);
            Ui.fill(caretX + 1, searchY + 4, 1, 10, Ui.accent());
        }
        Ui.accentLine(px + 10, py + HEADER_H - 6, PANEL_W - 20);

        int listTop = listTop();
        int listBottom = listBottom();
        List<Module> modules = new ArrayList<Module>();
        String needle = search.trim().toLowerCase();
        for (Module m : Modules.all()) {
            if (needle.isEmpty() || m.name.toLowerCase().contains(needle)) {
                modules.add(m);
            }
        }
        int contentH = modules.size() * ROW_H;
        int maxScroll = Math.max(0, contentH - (listBottom - listTop));
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        if (modules.isEmpty()) {
            Ui.centeredText(this.fontRendererObj, "No modules match \"" + search + "\"",
                    px + PANEL_W / 2, listTop + 12, Ui.TEXT_DIM);
        }
        for (int i = 0; i < modules.size(); i++) {
            final Module module = modules.get(i);
            int rowY = listTop + i * ROW_H - scroll;
            if (rowY + ROW_H < listTop || rowY > listBottom) continue;
            boolean hovered = mouseX >= px && mouseX < px + PANEL_W && mouseY >= rowY && mouseY < rowY + ROW_H;
            if (hovered) Ui.fill(px + 4, rowY + 2, PANEL_W - 8, ROW_H - 4, Ui.ROW_HOVER);

            this.fontRendererObj.drawString(module.name, px + 12, rowY + 8, module.enabled() ? Ui.TEXT : Ui.TEXT_DIM);
            this.fontRendererObj.drawString(">", px + PANEL_W - 66, rowY + 8, Ui.TEXT_DIM);

            final int pillX = px + PANEL_W - 44;
            final int pillY = rowY + 5;
            Ui.pill(pillX, pillY, module.enabled());

            if (rowY >= listTop - 4 && rowY + ROW_H <= listBottom + 4) {
                zones.add(new Ui.Zone(pillX, pillY, 30, 14, new Runnable() {
                    @Override
                    public void run() {
                        module.cfg().enabled = !module.cfg().enabled;
                        NoobConfig.save();
                    }
                }));
                zones.add(new Ui.Zone(px + 4, rowY, PANEL_W - 56, ROW_H, new Runnable() {
                    @Override
                    public void run() {
                        mc.displayGuiScreen(new ModuleSettingsScreen(NoobMenuScreen.this, module));
                    }
                }));
            }
        }

        int btnY = py + ph - FOOTER_H + 6;
        final int btnW = 74;
        final int editX = px + 6;
        final int appearX = px + 84;
        final int perfX = px + 162;
        final int doneX = px + 240;
        boolean editHover = mouseX >= editX && mouseX < editX + btnW && mouseY >= btnY && mouseY < btnY + 22;
        boolean appearHover = mouseX >= appearX && mouseX < appearX + btnW && mouseY >= btnY && mouseY < btnY + 22;
        boolean perfHover = mouseX >= perfX && mouseX < perfX + btnW && mouseY >= btnY && mouseY < btnY + 22;
        boolean doneHover = mouseX >= doneX && mouseX < doneX + btnW && mouseY >= btnY && mouseY < btnY + 22;
        Ui.button(this.fontRendererObj, editX, btnY, btnW, 22, "Edit HUD", editHover);
        Ui.button(this.fontRendererObj, appearX, btnY, btnW, 22, "Appearance", appearHover);
        Ui.button(this.fontRendererObj, perfX, btnY, btnW, 22, "Performance", perfHover);
        Ui.button(this.fontRendererObj, doneX, btnY, btnW, 22, "Done", doneHover);
        zones.add(new Ui.Zone(editX, btnY, btnW, 22, new Runnable() {
            @Override
            public void run() {
                mc.displayGuiScreen(new HudEditScreen());
            }
        }));
        zones.add(new Ui.Zone(appearX, btnY, btnW, 22, new Runnable() {
            @Override
            public void run() {
                mc.displayGuiScreen(new AppearanceScreen(NoobMenuScreen.this));
            }
        }));
        zones.add(new Ui.Zone(perfX, btnY, btnW, 22, new Runnable() {
            @Override
            public void run() {
                mc.displayGuiScreen(new PerformanceScreen(NoobMenuScreen.this));
            }
        }));
        zones.add(new Ui.Zone(doneX, btnY, btnW, 22, new Runnable() {
            @Override
            public void run() {
                mc.displayGuiScreen(null);
            }
        }));
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
        if (keyCode == org.lwjgl.input.Keyboard.KEY_ESCAPE) {
            super.keyTyped(typedChar, keyCode); // closes the menu
            return;
        }
        if (keyCode == org.lwjgl.input.Keyboard.KEY_BACK) {
            if (!search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                scroll = 0;
            }
            return;
        }
        if (typedChar >= 32 && typedChar != 127 && search.length() < 24) {
            search += typedChar;
            scroll = 0;
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int delta = Mouse.getEventDWheel();
        if (delta != 0) {
            scroll -= (delta > 0 ? 1 : -1) * ROW_H;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
