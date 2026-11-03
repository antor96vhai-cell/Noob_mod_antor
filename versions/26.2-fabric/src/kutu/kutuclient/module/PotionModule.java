package kutu.kutuclient.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists the player's active potion effects with a color dot (the effect's
 * own color), name, amplifier and remaining duration. Text-based (no atlas
 * icons) to stay lightweight and robust.
 */
public class PotionModule extends Module {

    public PotionModule() {
        super("potions", "Potion Effects");
    }

    private record Row(int color, String text) {
    }

    private List<Row> rows(Minecraft mc) {
        List<Row> rows = new ArrayList<>();
        if (mc.player == null) return rows;
        for (MobEffectInstance effect : mc.player.getActiveEffects()) {
            String name = effect.getEffect().value().getDisplayName().getString();
            int amp = effect.getAmplifier();
            if (amp > 0) name += " " + roman(amp + 1);
            String duration = effect.isInfiniteDuration() ? "\u221E" : formatDuration(effect.getDuration());
            rows.add(new Row(0xFF000000 | (effect.getEffect().value().getColor() & 0xFFFFFF),
                    name + "  " + duration));
        }
        return rows;
    }

    private static String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        return (totalSeconds / 60) + ":" + String.format("%02d", totalSeconds % 60);
    }

    private static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> Integer.toString(n);
        };
    }

    @Override
    public int width(Minecraft mc) {
        Font font = mc.font;
        int w = 0;
        for (Row r : rows(mc)) w = Math.max(w, font.width(r.text()));
        return PAD_X * 2 + 10 + w; // 6px dot + 4px gap
    }

    @Override
    public int height(Minecraft mc) {
        return linesHeight(Math.max(1, rows(mc).size()));
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        int w = width(mc);
        return new int[]{Math.max(0, mc.getWindow().getGuiScaledWidth() - w - 4), 18};
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        List<Row> rows = rows(mc);
        if (rows.isEmpty()) return;
        int w = width(mc);
        int h = linesHeight(rows.size());
        drawBackground(g, x, y, w, h);
        int color = textColor();
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            int rowY = y + PAD_Y + i * LINE_STEP;
            g.fill(x + PAD_X, rowY + 2, x + PAD_X + 6, rowY + 8, r.color());
            g.text(mc.font, r.text(), x + PAD_X + 10, rowY, color);
        }
    }
}
