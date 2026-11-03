package kutu.kutuclient.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Vertical armor status: helmet, chestplate, leggings, boots and the
 * main-hand item, each drawn as an item icon (with the vanilla durability
 * bar from item decorations) plus a durability readout.
 */
public class ArmorModule extends Module {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND
    };
    private static final int ROW_H = 18;
    private static final int ICON = 16;

    public ArmorModule() {
        super("armor", "Armor Status");
    }

    @Override
    public List<Opt> options() {
        return List.of(new Opt("style", "Durability display", "Percent", "Remaining", "Bar only"));
    }

    private ItemStack stack(Minecraft mc, int i) {
        if (mc.player == null) return ItemStack.EMPTY;
        return mc.player.getItemBySlot(SLOTS[i]);
    }

    private String durabilityText(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return "";
        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        return switch (opt("style")) {
            case "Remaining" -> Integer.toString(remaining);
            case "Bar only" -> "";
            default -> Math.round(100.0f * remaining / stack.getMaxDamage()) + "%";
        };
    }

    @Override
    public int width(Minecraft mc) {
        if ("Bar only".equals(opt("style"))) return PAD_X * 2 + ICON;
        int text = 0;
        Font font = mc.font;
        for (int i = 0; i < SLOTS.length; i++) {
            text = Math.max(text, font.width(durabilityText(stack(mc, i))));
        }
        return PAD_X * 2 + ICON + 3 + text;
    }

    @Override
    public int height(Minecraft mc) {
        return PAD_Y * 2 + SLOTS.length * ROW_H - 2;
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        int w = width(mc);
        int h = height(mc);
        int guiW = mc.getWindow().getGuiScaledWidth();
        int guiH = mc.getWindow().getGuiScaledHeight();
        // Just right of the centered hotbar, sitting on the bottom edge.
        return new int[]{Math.min(guiW - w - 2, guiW / 2 + 98), Math.max(0, guiH - h - 24)};
    }

    @Override
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
        int w = width(mc);
        int h = height(mc);
        drawBackground(g, x, y, w, h);
        boolean barOnly = "Bar only".equals(opt("style"));
        int color = textColor();
        for (int i = 0; i < SLOTS.length; i++) {
            int rowY = y + PAD_Y + i * ROW_H;
            int iconX = x + PAD_X;
            int iconY = rowY + 1;
            ItemStack stack = stack(mc, i);
            if (stack.isEmpty()) {
                g.outline(iconX, iconY, ICON, ICON, 0xFF3A4353);
            } else {
                g.item(stack, iconX, iconY);
                g.itemDecorations(mc.font, stack, iconX, iconY);
            }
            if (!barOnly) {
                String text = durabilityText(stack);
                if (!text.isEmpty()) {
                    g.text(mc.font, text, iconX + ICON + 3, rowY + 5, color);
                }
            }
        }
    }
}
