package nooblaunchar.gui;

import net.minecraft.client.gui.GuiScreen;
import nooblaunchar.NoobConfig;
import nooblaunchar.module.Module;
import nooblaunchar.module.Modules;

import java.util.ArrayList;
import java.util.List;

/**
 * "Edit HUD" mode: the live HUD (drawn by the overlay handler behind this
 * screen) is overlaid with outlines and name tags; drag elements with the
 * mouse to move them. Positions save to the config when a drag ends.
 */
public class HudEditScreen extends GuiScreen {
    private final List<Ui.Zone> zones = new ArrayList<Ui.Zone>();
    private Module dragging = null;
    private int dragOffsetX;
    private int dragOffsetY;

    private List<Module> visibleModules() {
        List<Module> list = new ArrayList<Module>();
        for (Module m : Modules.all()) {
            if (m.enabled() && m.hasHud()) list.add(m);
        }
        return list;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Dim (the HUD itself is drawn by the overlay handler underneath).
        Ui.fill(0, 0, this.width, this.height, 0x66000000);
        zones.clear();

        Ui.centeredText(this.fontRendererObj, "Edit HUD - drag elements to move them", this.width / 2, 8, Ui.TEXT);
        Ui.centeredText(this.fontRendererObj, "Positions save automatically", this.width / 2, 20, Ui.TEXT_DIM);

        for (Module module : visibleModules()) {
            int[] pos = module.pos(mc);
            int w = Math.max(6, module.width(mc));
            int h = Math.max(6, module.height(mc));
            Ui.outline(pos[0] - 1, pos[1] - 1, w + 2, h + 2, module == dragging ? 0xFFFFFFFF : Ui.ACCENT);
            this.fontRendererObj.drawString(module.name, pos[0], Math.max(0, pos[1] - 11), Ui.TEXT_DIM);
        }

        int btnY = this.height - 30;
        final int resetX = this.width / 2 - 104;
        final int doneX = this.width / 2 + 4;
        boolean resetHover = mouseX >= resetX && mouseX < resetX + 100 && mouseY >= btnY && mouseY < btnY + 22;
        boolean doneHover = mouseX >= doneX && mouseX < doneX + 100 && mouseY >= btnY && mouseY < btnY + 22;
        Ui.button(this.fontRendererObj, resetX, btnY, 100, 22, "Reset All", resetHover);
        Ui.button(this.fontRendererObj, doneX, btnY, 100, 22, "Done", doneHover);
        zones.add(new Ui.Zone(resetX, btnY, 100, 22, new Runnable() {
            @Override
            public void run() {
                for (Module m : Modules.all()) {
                    m.cfg().x = -1;
                    m.cfg().y = -1;
                }
                NoobConfig.save();
            }
        }));
        zones.add(new Ui.Zone(doneX, btnY, 100, 22, new Runnable() {
            @Override
            public void run() {
                mc.displayGuiScreen(null);
            }
        }));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (Ui.click(zones, mouseX, mouseY)) return;
            List<Module> modules = visibleModules();
            // Topmost last: iterate in reverse draw order.
            for (int i = modules.size() - 1; i >= 0; i--) {
                Module module = modules.get(i);
                int[] pos = module.pos(mc);
                int w = Math.max(6, module.width(mc));
                int h = Math.max(6, module.height(mc));
                if (mouseX >= pos[0] && mouseX < pos[0] + w && mouseY >= pos[1] && mouseY < pos[1] + h) {
                    dragging = module;
                    dragOffsetX = mouseX - pos[0];
                    dragOffsetY = mouseY - pos[1];
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging != null) {
            int w = Math.max(6, dragging.width(mc));
            int h = Math.max(6, dragging.height(mc));
            int nx = mouseX - dragOffsetX;
            int ny = mouseY - dragOffsetY;
            nx = Math.max(0, Math.min(nx, this.width - w));
            ny = Math.max(0, Math.min(ny, this.height - h));
            dragging.cfg().x = nx;
            dragging.cfg().y = ny;
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (dragging != null) {
            dragging = null;
            NoobConfig.save();
            return;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
