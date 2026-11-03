package kutu.kutuclient.gui;

import kutu.kutuclient.KutuConfig;
import kutu.kutuclient.module.Module;
import kutu.kutuclient.module.Modules;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * "Edit HUD" mode: every enabled HUD module is drawn at its live position
 * with an outline and name tag; drag elements with the mouse to move them.
 * Positions are saved to the config when a drag ends.
 */
public class HudEditScreen extends Screen {
    private final List<Ui.Zone> zones = new ArrayList<>();
    private Module dragging = null;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudEditScreen() {
        super(Component.literal("Edit HUD"));
    }

    private List<Module> visibleModules() {
        List<Module> list = new ArrayList<>();
        for (Module m : Modules.all()) {
            if (m.enabled() && m.hasHud()) list.add(m);
        }
        return list;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        zones.clear();

        graphics.fill(0, 0, this.width, this.height, 0x66000000);
        graphics.centeredText(this.font, "Edit HUD - drag elements to move them", this.width / 2, 8, Ui.TEXT);
        graphics.centeredText(this.font, "Positions save automatically", this.width / 2, 20, Ui.TEXT_DIM);

        if (this.minecraft != null && this.minecraft.player != null) {
            for (Module module : visibleModules()) {
                int[] pos = module.pos(this.minecraft);
                int w = Math.max(6, module.width(this.minecraft));
                int h = Math.max(6, module.height(this.minecraft));
                module.render(graphics, this.minecraft, pos[0], pos[1]);
                boolean active = module == dragging;
                graphics.outline(pos[0] - 1, pos[1] - 1, w + 2, h + 2, active ? 0xFFFFFFFF : Ui.ACCENT);
                graphics.text(this.font, module.name, pos[0], Math.max(0, pos[1] - 11), Ui.TEXT_DIM);
            }
        }

        int btnY = this.height - 30;
        int resetX = this.width / 2 - 104;
        int doneX = this.width / 2 + 4;
        boolean resetHover = mouseX >= resetX && mouseX < resetX + 100 && mouseY >= btnY && mouseY < btnY + 22;
        boolean doneHover = mouseX >= doneX && mouseX < doneX + 100 && mouseY >= btnY && mouseY < btnY + 22;
        Ui.button(graphics, this.font, resetX, btnY, 100, 22, "Reset All", resetHover);
        Ui.button(graphics, this.font, doneX, btnY, 100, 22, "Done", doneHover);
        zones.add(new Ui.Zone(resetX, btnY, 100, 22, () -> {
            for (Module m : Modules.all()) {
                m.cfg().x = -1;
                m.cfg().y = -1;
            }
            KutuConfig.save();
        }));
        zones.add(new Ui.Zone(doneX, btnY, 100, 22, this::onClose));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            if (Ui.click(zones, event.x(), event.y())) return true;
            if (this.minecraft != null) {
                List<Module> modules = visibleModules();
                // Topmost last: iterate in reverse draw order.
                for (int i = modules.size() - 1; i >= 0; i--) {
                    Module module = modules.get(i);
                    int[] pos = module.pos(this.minecraft);
                    int w = Math.max(6, module.width(this.minecraft));
                    int h = Math.max(6, module.height(this.minecraft));
                    if (event.x() >= pos[0] && event.x() < pos[0] + w
                            && event.y() >= pos[1] && event.y() < pos[1] + h) {
                        dragging = module;
                        dragOffsetX = (int) event.x() - pos[0];
                        dragOffsetY = (int) event.y() - pos[1];
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null && this.minecraft != null) {
            int w = Math.max(6, dragging.width(this.minecraft));
            int h = Math.max(6, dragging.height(this.minecraft));
            int nx = (int) event.x() - dragOffsetX;
            int ny = (int) event.y() - dragOffsetY;
            nx = Math.max(0, Math.min(nx, this.width - w));
            ny = Math.max(0, Math.min(ny, this.height - h));
            dragging.cfg().x = nx;
            dragging.cfg().y = ny;
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging != null) {
            dragging = null;
            KutuConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        KutuConfig.save();
        this.minecraft.gui.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
