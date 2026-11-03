package nooblaunchar.gui;

import net.minecraft.client.gui.GuiScreen;
import nooblaunchar.NoobConfig;
import nooblaunchar.module.Module;
import nooblaunchar.module.Modules;
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
    private static final int HEADER_H = 42;
    private static final int FOOTER_H = 34;

    private final List<Ui.Zone> zones = new ArrayList<Ui.Zone>();
    private int scroll = 0;

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
        Ui.centeredText(this.fontRendererObj, "Noob_launchar", px + PANEL_W / 2, py + 10, Ui.ACCENT);
        Ui.centeredText(this.fontRendererObj, "Click a module for its settings", px + PANEL_W / 2, py + 24, Ui.TEXT_DIM);
        Ui.accentLine(px + 10, py + HEADER_H - 6, PANEL_W - 20);

        int listTop = listTop();
        int listBottom = listBottom();
        int contentH = Modules.all().size() * ROW_H;
        int maxScroll = Math.max(0, contentH - (listBottom - listTop));
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        List<Module> modules = Modules.all();
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
        final int editX = px + 26;
        final int doneX = px + PANEL_W - 26 - 120;
        boolean editHover = mouseX >= editX && mouseX < editX + 120 && mouseY >= btnY && mouseY < btnY + 22;
        boolean doneHover = mouseX >= doneX && mouseX < doneX + 120 && mouseY >= btnY && mouseY < btnY + 22;
        Ui.button(this.fontRendererObj, editX, btnY, 120, 22, "Edit HUD", editHover);
        Ui.button(this.fontRendererObj, doneX, btnY, 120, 22, "Done", doneHover);
        zones.add(new Ui.Zone(editX, btnY, 120, 22, new Runnable() {
            @Override
            public void run() {
                mc.displayGuiScreen(new HudEditScreen());
            }
        }));
        zones.add(new Ui.Zone(doneX, btnY, 120, 22, new Runnable() {
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
