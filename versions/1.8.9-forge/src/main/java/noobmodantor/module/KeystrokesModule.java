package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Classic keystrokes display: W / A S D keys, LMB and RMB boxes and a
 * space bar, each lighting up (in the module's text color) while pressed.
 * Key labels follow the user's actual key bindings.
 */
public class KeystrokesModule extends Module {
    private static final int CELL = 18;
    private static final int GAP = 2;
    private static final int WIDTH = CELL * 3 + GAP * 2;            // 58
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
        ScaledResolution res = new ScaledResolution(mc);
        return new int[]{
                Math.max(0, res.getScaledWidth() - WIDTH - 4),
                Math.max(0, res.getScaledHeight() - HEIGHT - 4)};
    }

    private static String label(KeyBinding bind) {
        String name = Keyboard.getKeyName(bind.getKeyCode());
        return name == null ? "" : name;
    }

    @Override
    public void render(Minecraft mc, int x, int y) {
        FontRenderer font = mc.fontRendererObj;
        int accent = textColor();

        drawKey(font, x + CELL + GAP, y, CELL, CELL,
                label(mc.gameSettings.keyBindForward), mc.gameSettings.keyBindForward.isKeyDown(), accent);
        int row2 = y + CELL + GAP;
        drawKey(font, x, row2, CELL, CELL,
                label(mc.gameSettings.keyBindLeft), mc.gameSettings.keyBindLeft.isKeyDown(), accent);
        drawKey(font, x + CELL + GAP, row2, CELL, CELL,
                label(mc.gameSettings.keyBindBack), mc.gameSettings.keyBindBack.isKeyDown(), accent);
        drawKey(font, x + (CELL + GAP) * 2, row2, CELL, CELL,
                label(mc.gameSettings.keyBindRight), mc.gameSettings.keyBindRight.isKeyDown(), accent);
        int row3 = row2 + CELL + GAP;
        int mouseW = (WIDTH - GAP) / 2;
        drawKey(font, x, row3, mouseW, CELL, "LMB", Mouse.isButtonDown(0), accent);
        drawKey(font, x + mouseW + GAP, row3, mouseW, CELL, "RMB", Mouse.isButtonDown(1), accent);
        int row4 = row3 + CELL + GAP;
        drawKey(font, x, row4, WIDTH, SPACE_H,
                label(mc.gameSettings.keyBindJump), mc.gameSettings.keyBindJump.isKeyDown(), accent);
    }

    private void drawKey(FontRenderer font, int x, int y, int w, int h,
                         String label, boolean pressed, int accent) {
        fill(x, y, w, h, pressed ? accent : KEY_BG);
        int labelColor = pressed ? 0xFF0B0E11 : KEY_TEXT;
        String text = label == null ? "" : label;
        if (text.length() > 3 && w <= CELL) text = text.substring(0, 3);
        int tx = x + (w - font.getStringWidth(text)) / 2;
        int ty = y + (h - 9) / 2 + 1;
        font.drawString(text, tx, ty, labelColor);
    }
}
