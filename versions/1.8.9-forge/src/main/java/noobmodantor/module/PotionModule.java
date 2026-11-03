package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lists the local player's own active potion effects. (In 1.8.9 the
 * client only ever knows the local player's effects — there is no
 * other source — so the display is own-effects-only by construction.)
 *
 * <p>Display options: Style — "Icon+Text" (real inventory effect
 * icons), "Text only" (classic color dot), or "Icon+Duration"; and a
 * Duration on/off switch for the text styles. Icons are drawn from the
 * vanilla inventory texture at the exact UVs vanilla's own
 * InventoryEffectRenderer uses (verified from its bytecode:
 * u = (index % 8) * 18, v = 198 + (index / 8) * 18, 18x18), scaled to
 * 9px to fit the HUD row height.
 */
public class PotionModule extends Module {

    private static final ResourceLocation INVENTORY_TEXTURE =
            new ResourceLocation("textures/gui/container/inventory.png");
    private static final Gui GUI = new Gui();

    public PotionModule() {
        super("potions", "Potion Effects");
    }

    @Override
    public List<Opt> options() {
        return Arrays.asList(
                new Opt("style", "Style", Arrays.asList("Icon+Text", "Text only", "Icon+Duration")),
                new Opt("duration", "Duration", Arrays.asList("On", "Off")));
    }

    private boolean iconStyle() {
        return !"Text only".equals(opt("style"));
    }

    private static class Row {
        final int color;
        final int iconIndex; // -1 = no icon
        final String text;

        Row(int color, int iconIndex, String text) {
            this.color = color;
            this.iconIndex = iconIndex;
            this.text = text;
        }
    }

    private List<Row> rows(Minecraft mc) {
        List<Row> rows = new ArrayList<Row>();
        if (mc.thePlayer == null) return rows;
        boolean iconDurationOnly = "Icon+Duration".equals(opt("style"));
        boolean showDuration = iconDurationOnly || "On".equals(opt("duration"));
        for (PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
            String name = effect.getEffectName();
            int amp = effect.getAmplifier();
            if (amp > 0) name += " " + roman(amp + 1);
            int duration = effect.getDuration();
            String durationText = (duration / 20 > 60000) ? "\u221E" : formatDuration(duration);
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            int dot = potion == null ? 0xFFFFFFFF : (0xFF000000 | (potion.getLiquidColor() & 0xFFFFFF));
            int icon = (potion != null && potion.hasStatusIcon()) ? potion.getStatusIconIndex() : -1;
            String text;
            if (iconDurationOnly) {
                text = durationText;
            } else if (showDuration) {
                text = name + "  " + durationText;
            } else {
                text = name;
            }
            rows.add(new Row(dot, icon, text));
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

    private int leadWidth() {
        return iconStyle() ? 13 : 10; // 9px icon (or 6px dot) + gap
    }

    @Override
    public int width(Minecraft mc) {
        FontRenderer font = mc.fontRendererObj;
        int w = 0;
        for (Row r : rows(mc)) w = Math.max(w, font.getStringWidth(r.text));
        return PAD_X * 2 + leadWidth() + w;
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
        boolean icons = iconStyle();
        if (icons) {
            mc.getTextureManager().bindTexture(INVENTORY_TEXTURE);
        }
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            int rowY = y + PAD_Y + i * LINE_STEP;
            if (icons && r.iconIndex >= 0) {
                int u = (r.iconIndex % 8) * 18;
                int v = 198 + (r.iconIndex / 8) * 18;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + PAD_X, rowY, 0.0f);
                GlStateManager.scale(0.5f, 0.5f, 1.0f);
                GUI.drawTexturedModalRect(0, 0, u, v, 18, 18);
                GlStateManager.popMatrix();
            } else {
                fill(x + PAD_X, rowY + 2, 6, 6, r.color);
            }
            mc.fontRendererObj.drawString(r.text, x + PAD_X + leadWidth(), rowY, color);
        }
    }
}
