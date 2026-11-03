package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Collections;
import java.util.List;

/**
 * Held-item (viewmodel) size: scales the first-person hand/item render
 * about the item's own rest position. Implemented on Forge 1.8.9's
 * {@code RenderHandEvent}, which EntityRenderer fires after the hand
 * pass's projection/modelview matrices are set up and before
 * ItemRenderer draws; the transforms applied here live inside that
 * pass's push/pop, so nothing leaks into world rendering. The anchor
 * (0.56, -0.52, -0.72) is the exact translation vanilla's
 * transformFirstPersonItem applies first, so the item grows/shrinks in
 * place instead of drifting. Visual only; at 1.0x nothing is applied.
 */
public class HeldItemModule extends Module {

    public HeldItemModule() {
        super("held_item", "Held Item Size");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<SliderOpt> sliders() {
        return Collections.singletonList(new SliderOpt("scale", "Item scale", 0.5, 1.5, 0.05, 1.0));
    }

    public static void apply() {
        HeldItemModule self = (HeldItemModule) Modules.get("held_item");
        if (self == null || !self.enabled()) return;
        double s = self.sliderVal(self.sliders().get(0));
        if (s == 1.0) return;
        GlStateManager.translate(0.56f, -0.52f, -0.72f);
        GlStateManager.scale((float) s, (float) s, (float) s);
        GlStateManager.translate(-0.56f, 0.52f, 0.72f);
    }

    // Non-HUD module.
    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{0, 0};
    }

    @Override
    public int width(Minecraft mc) {
        return 0;
    }

    @Override
    public int height(Minecraft mc) {
        return 0;
    }

    @Override
    public void render(Minecraft mc, int x, int y) {
    }
}
