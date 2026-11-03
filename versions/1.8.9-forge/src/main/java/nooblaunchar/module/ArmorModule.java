package nooblaunchar.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Vertical armor status: helmet, chestplate, leggings, boots and the
 * main-hand item, each drawn as an item icon (with the vanilla durability
 * bar from renderItemOverlays) plus a durability readout.
 * Note: InventoryPlayer.armorInventory is ordered boots, leggings,
 * chestplate, helmet; display order here is helmet first.
 */
public class ArmorModule extends Module {
    /** armorInventory indices in display order: helmet, chest, legs, boots. */
    private static final int[] ARMOR_ORDER = {3, 2, 1, 0};
    private static final int ROW_H = 18;
    private static final int ICON = 16;

    public ArmorModule() {
        super("armor", "Armor Status");
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("style", "Durability display",
                Arrays.asList("Percent", "Remaining", "Bar only")));
    }

    private ItemStack stack(Minecraft mc, int row) {
        if (mc.thePlayer == null) return null;
        if (row < 4) return mc.thePlayer.inventory.armorInventory[ARMOR_ORDER[row]];
        return mc.thePlayer.getHeldItem();
    }

    private String durabilityText(ItemStack stack) {
        if (stack == null || !stack.isItemStackDamageable()) return "";
        int remaining = stack.getMaxDamage() - stack.getItemDamage();
        String style = opt("style");
        if ("Remaining".equals(style)) return Integer.toString(remaining);
        if ("Bar only".equals(style)) return "";
        return Math.round(100.0f * remaining / stack.getMaxDamage()) + "%";
    }

    @Override
    public int width(Minecraft mc) {
        if ("Bar only".equals(opt("style"))) return PAD_X * 2 + ICON;
        int text = 0;
        FontRenderer font = mc.fontRendererObj;
        for (int i = 0; i < 5; i++) {
            text = Math.max(text, font.getStringWidth(durabilityText(stack(mc, i))));
        }
        return PAD_X * 2 + ICON + 3 + text;
    }

    @Override
    public int height(Minecraft mc) {
        return PAD_Y * 2 + 5 * ROW_H - 2;
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        int w = width(mc);
        int h = height(mc);
        ScaledResolution res = new ScaledResolution(mc);
        // Just right of the centered hotbar, sitting on the bottom edge.
        return new int[]{Math.min(res.getScaledWidth() - w - 2, res.getScaledWidth() / 2 + 98),
                Math.max(0, res.getScaledHeight() - h - 24)};
    }

    @Override
    public void render(Minecraft mc, int x, int y) {
        int w = width(mc);
        int h = height(mc);
        drawBackground(x, y, w, h);
        boolean barOnly = "Bar only".equals(opt("style"));
        int color = textColor();
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < 5; i++) {
            int rowY = y + PAD_Y + i * ROW_H;
            int iconX = x + PAD_X;
            int iconY = rowY + 1;
            ItemStack stack = stack(mc, i);
            if (stack == null) {
                outline(iconX, iconY, ICON, ICON, 0xFF3A4353);
            } else {
                mc.getRenderItem().renderItemIntoGUI(stack, iconX, iconY);
                mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, iconX, iconY);
            }
            if (!barOnly) {
                String text = durabilityText(stack);
                if (!text.isEmpty()) {
                    mc.fontRendererObj.drawString(text, iconX + ICON + 3, rowY + 5, color);
                }
            }
        }
        RenderHelper.disableStandardItemLighting();
    }

    private static void outline(int x, int y, int w, int h, int color) {
        fill(x, y, w, 1, color);
        fill(x, y + h - 1, w, 1, color);
        fill(x, y, 1, h, color);
        fill(x + w - 1, y, 1, h, color);
    }
}
