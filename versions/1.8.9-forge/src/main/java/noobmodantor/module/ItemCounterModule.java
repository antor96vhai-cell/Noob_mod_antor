package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

/** Counts the arrows in the player's inventory (main storage + hotbar). */
public class ItemCounterModule extends TextModule {

    public ItemCounterModule() {
        super("item_counter", "Item Counter");
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        int arrows = 0;
        if (mc.thePlayer != null) {
            ItemStack[] inv = mc.thePlayer.inventory.mainInventory;
            for (ItemStack stack : inv) {
                if (stack != null && stack.getItem() == Items.arrow) {
                    arrows += stack.stackSize;
                }
            }
        }
        return Collections.singletonList("Arrows: " + arrows);
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 112};
    }
}
