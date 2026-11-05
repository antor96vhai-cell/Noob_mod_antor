package noobmodantor.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * Custom-drawn button for the branded main menu: dark translucent fill,
 * panel-border outline that lights up in the live accent color on hover
 * (with a faint accent halo just outside the border), accent label on
 * hover. Plain 2D rects only — no textures, no shaders — so it stays
 * smooth on weak iGPUs. Click handling is vanilla's (GuiScreen dispatches
 * through {@code mousePressed}/{@code actionPerformed}).
 */
public class NoobButton extends GuiButton {

    public NoobButton(int id, int x, int y, int w, int h, String label) {
        super(id, x, y, w, h, label);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;
        boolean hov = mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        this.hovered = hov;

        if (hov && this.enabled) {
            // Soft accent halo one pixel outside the button.
            Ui.fill(this.xPosition - 1, this.yPosition - 1, this.width + 2, this.height + 2,
                    0x26000000 | (Ui.accent() & 0x00FFFFFF));
        }
        Ui.fill(this.xPosition, this.yPosition, this.width, this.height,
                hov ? 0xE627313F : 0xD10C1015);
        Ui.outline(this.xPosition, this.yPosition, this.width, this.height,
                this.enabled && hov ? Ui.accent() : Ui.PANEL_BORDER);
        int color = !this.enabled ? Ui.TEXT_DIM : (hov ? Ui.accent() : Ui.TEXT);
        this.drawCenteredString(mc.fontRendererObj, this.displayString,
                this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, color);
    }
}
