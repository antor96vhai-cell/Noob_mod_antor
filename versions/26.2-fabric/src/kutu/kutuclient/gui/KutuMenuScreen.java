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
 * Main menu (Right Shift): a scrollable list of all modules. Each row has
 * a settings affordance (click the name) and an ON/OFF pill. Footer buttons
 * open the HUD editor or close the menu.
 */
public class KutuMenuScreen extends Screen {
    private static final int PANEL_W = 320;
    private static final int ROW_H = 24;
    private static final int HEADER_H = 42;
    private static final int FOOTER_H = 34;

    private final List<Ui.Zone> zones = new ArrayList<>();
    private int scroll = 0;

    public KutuMenuScreen() {
        super(Component.literal("Noob_launchar"));
    }

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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        zones.clear();
        int px = panelX();
        int py = panelY();
        int ph = panelH();

        Ui.panel(graphics, px, py, PANEL_W, ph);
        graphics.centeredText(this.font, "Noob_launchar", px + PANEL_W / 2, py + 10, Ui.ACCENT);
        graphics.centeredText(this.font, "Click a module for its settings", px + PANEL_W / 2, py + 24, Ui.TEXT_DIM);
        Ui.accentLine(graphics, px + 10, py + HEADER_H - 6, PANEL_W - 20);

        int listTop = listTop();
        int listBottom = listBottom();
        int contentH = Modules.all().size() * ROW_H;
        int maxScroll = Math.max(0, contentH - (listBottom - listTop));
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        graphics.enableScissor(px + 1, listTop, px + PANEL_W - 1, listBottom);
        List<Module> modules = Modules.all();
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int rowY = listTop + i * ROW_H - scroll;
            if (rowY + ROW_H < listTop || rowY > listBottom) continue;
            boolean hovered = mouseX >= px && mouseX < px + PANEL_W && mouseY >= rowY && mouseY < rowY + ROW_H;
            if (hovered) graphics.fill(px + 4, rowY + 2, px + PANEL_W - 4, rowY + ROW_H - 2, Ui.ROW_HOVER);

            graphics.text(this.font, module.name, px + 12, rowY + 8, module.enabled() ? Ui.TEXT : Ui.TEXT_DIM);
            graphics.text(this.font, ">", px + PANEL_W - 66, rowY + 8, Ui.TEXT_DIM);

            int pillX = px + PANEL_W - 44;
            int pillY = rowY + 5;
            Ui.pill(graphics, pillX, pillY, module.enabled());

            // Click zones only for rows that are (partly) inside the list viewport.
            if (rowY >= listTop - 4 && rowY + ROW_H <= listBottom + 4) {
                zones.add(new Ui.Zone(pillX, pillY, 30, 14, () -> {
                    module.cfg().enabled = !module.cfg().enabled;
                    KutuConfig.save();
                }));
                zones.add(new Ui.Zone(px + 4, rowY, PANEL_W - 56, ROW_H, () ->
                        this.minecraft.gui.setScreen(new ModuleSettingsScreen(this, module))));
            }
        }
        graphics.disableScissor();

        // Footer
        int btnY = py + ph - FOOTER_H + 6;
        int editX = px + 26;
        int doneX = px + PANEL_W - 26 - 120;
        boolean editHover = mouseX >= editX && mouseX < editX + 120 && mouseY >= btnY && mouseY < btnY + 22;
        boolean doneHover = mouseX >= doneX && mouseX < doneX + 120 && mouseY >= btnY && mouseY < btnY + 22;
        Ui.button(graphics, this.font, editX, btnY, 120, 22, "Edit HUD", editHover);
        Ui.button(graphics, this.font, doneX, btnY, 120, 22, "Done", doneHover);
        zones.add(new Ui.Zone(editX, btnY, 120, 22, () ->
                this.minecraft.gui.setScreen(new HudEditScreen())));
        zones.add(new Ui.Zone(doneX, btnY, 120, 22, this::onClose));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && Ui.click(zones, event.x(), event.y())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll -= (int) (scrollY * ROW_H);
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
