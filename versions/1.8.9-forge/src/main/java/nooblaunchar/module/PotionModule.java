package nooblaunchar.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists the player's active potion effects with a color dot (the effect's
 * own liquid color), name, amplifier and remaining duration.
 */
public class PotionModule extends Module {

    public PotionModule() {
        super("potions", "Potion Effects");
    }

    private static class Row {
        final int color;
        final String text;

        Row(int color, String text) {
            this.color = color;
            this.text = text;
        }
    }

    private List<Row> rows(Minecraft mc) {
        List<Row> rows = new ArrayList<Row>();
        if (mc.thePlayer == null) return rows;
        for (PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
            String name = effect.getEffectName();
            int amp = effect.getAmplifier();
            if (amp > 0) name += " " + roman(amp + 1);
            int duration = effect.getDuration();
            String durationText = (duration / 20 > 60000) ? "\u221E" : formatDuration(duration);
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            int dot = potion == null ? 0xFFFFFFFF : (0xFF000000 | (potion.getLiquidColor() & 0xFFFFFF));
            rows.add(new Row(dot, name + "  " + durationText));
        }
        return rows;
    }

    private static String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        return (totalSeconds / 60) + ":" + String.format("%02d", totalSeconds % 60);
    }

    private static String roman(int n) {
        switch (n) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return Integer.toString(n);
        }
    }

    @Override
    public int width(Minecraft mc) {
        FontRenderer font = mc.fontRendererObj;
        int w = 0;
        for (Row r : rows(mc)) w = Math.max(w, font.getStringWidth(r.text));
        return PAD_X * 2 + 10 + w; // 6px dot + 4px gap
    }

    @Override
    public int height(Minecraft mc) {
        return linesHeight(Math.max(1, rows(mc).size()));
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        int w = width(mc);
        ScaledResolution res = new ScaledResolution(mc);
        return new int[]{Math.max(0, res.getScaledWidth() - w - 4), 18};
    }

    @Override
    public void render(Minecraft mc, int x, int y) {
        List<Row> rows = rows(mc);
        if (rows.isEmpty()) return;
        int w = width(mc);
        int h = linesHeight(rows.size());
        drawBackground(x, y, w, h);
        int color = textColor();
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            int rowY = y + PAD_Y + i * LINE_STEP;
            fill(x + PAD_X, rowY + 2, 6, 6, r.color);
            mc.fontRendererObj.drawString(r.text, x + PAD_X + 10, rowY, color);
        }
    }
}
