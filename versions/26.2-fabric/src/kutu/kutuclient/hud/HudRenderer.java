package kutu.kutuclient.hud;

import kutu.kutuclient.module.Module;
import kutu.kutuclient.module.Modules;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Single Fabric HUD element that draws every enabled HUD module at its
 * configured position. Registered via HudElementRegistry in KutuClient.
 */
public class HudRenderer implements HudElement {

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.level == null) return;
        CpsTracker.poll(mc);
        for (Module module : Modules.all()) {
            if (!module.enabled() || !module.hasHud()) continue;
            int[] pos = module.pos(mc);
            module.render(graphics, mc, pos[0], pos[1]);
        }
    }
}
