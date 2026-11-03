package kutu.kutuclient.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * Classic keystrokes display: W / A S D keys, LMB and RMB boxes and a
 * space bar, each lighting up (in the module's text color) while pressed.
 * Key labels follow the user's actual key bindings.
 */
public class KeystrokesModule extends Module {
    private static final int CELL = 18;
    private static final int GAP = 2;
    private static final int WIDTH = CELL * 3 + GAP * 2;          // 58
    private static final int SPACE_H = 12;
    private static final int HEIGHT = CELL * 3 + GAP * 3 + SPACE_H; // 78

    private static final int KEY_BG = 0xB0101418;
    private static final int KEY_TEXT = 0xFFF2F5F7;

    public KeystrokesModule() {
        super("keystrokes", "Keystrokes");
    }

    @Override
    public boolean supportsBackground() {
        return false; // the key boxes are the background
    }

    @Override
    public int width(Minecraft mc) {
        return WIDTH;
    }

    @Override
    public int height(Minecraft mc) {
        return HEIGHT;
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{
                Math.max(0, mc.getWindow().getGuiScaledWidth() - WIDTH - 4),
                Math.max(0, mc.getWindow().getGuiScaledHeight() - HEIGHT - 4)};
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        Font font = mc.font;
        int accent = textColor();

        // Row 1: W (centered)
        drawKey(g, font, x + CELL + GAP, y, CELL, CELL,
                mc.options.keyUp.getTranslatedKeyMessage().getString(),
                mc.options.keyUp.isDown(), accent);
        // Row 2: A S D
        int row2 = y + CELL + GAP;
        drawKey(g, font, x, row2, CELL, CELL,
                mc.options.keyLeft.getTranslatedKeyMessage().getString(),
                mc.options.keyLeft.isDown(), accent);
        drawKey(g, font, x + CELL + GAP, row2, CELL, CELL,
                mc.options.keyDown.getTranslatedKeyMessage().getString(),
                mc.options.keyDown.isDown(), accent);
        drawKey(g, font, x + (CELL + GAP) * 2, row2, CELL, CELL,
                mc.options.keyRight.getTranslatedKeyMessage().getString(),
                mc.options.keyRight.isDown(), accent);
        // Row 3: LMB / RMB
        int row3 = row2 + CELL + GAP;
        int mouseW = (WIDTH - GAP) / 2;
        drawKey(g, font, x, row3, mouseW, CELL, "LMB", mc.mouseHandler.isLeftPressed(), accent);
        drawKey(g, font, x + mouseW + GAP, row3, mouseW, CELL, "RMB", mc.mouseHandler.isRightPressed(), accent);
        // Row 4: Space
        int row4 = row3 + CELL + GAP;
        drawKey(g, font, x, row4, WIDTH, SPACE_H,
                mc.options.keyJump.getTranslatedKeyMessage().getString(),
                mc.options.keyJump.isDown(), accent);
    }

    private void drawKey(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h,
                         String label, boolean pressed, int accent) {
        g.fill(x, y, x + w, y + h, pressed ? accent : KEY_BG);
        int labelColor = pressed ? 0xFF0B0E11 : KEY_TEXT;
        String text = label == null ? "" : label;
        if (text.length() > 3 && w <= CELL) text = text.substring(0, 3);
        int tw = font.width(text);
        int tx = x + (w - tw) / 2;
        int ty = y + (h - 9) / 2 + 1;
        g.text(font, text, tx, ty, labelColor);
    }

    @Override
    public List<Opt> options() {
        return List.of();
    }
}
