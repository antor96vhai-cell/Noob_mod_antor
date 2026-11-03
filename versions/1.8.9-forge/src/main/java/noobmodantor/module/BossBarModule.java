package noobmodantor.module;

import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Show/hide the vanilla boss health bar. When set to Hide, ClientEvents
 * cancels the BOSSHEALTH RenderGameOverlayEvent.Pre, which is the gate
 * GuiIngameForge checks before drawing the bar.
 */
public class BossBarModule extends Module {

    public BossBarModule() {
        super("bossbar", "Boss Bar");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("display", "Boss bar", Arrays.asList("Show", "Hide")));
    }

    /** True when the boss bar should be suppressed (module on + "Hide"). */
    public static boolean shouldHide() {
        Module self = Modules.get("bossbar");
        return self != null && self.enabled() && "Hide".equals(self.opt("display"));
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
