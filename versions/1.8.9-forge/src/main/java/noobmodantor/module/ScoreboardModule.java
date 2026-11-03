package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.GuiIngameForge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Show/hide the vanilla scoreboard sidebar. Forge 1.8.9 has no overlay
 * event for the sidebar (that element type only exists in later versions);
 * it is gated by the public static flag {@code GuiIngameForge
 * .renderObjective} instead. The flag is only written when the desired
 * state actually changes, and is restored to true when the module stops
 * hiding, so other mods' use of the flag is not fought every tick.
 */
public class ScoreboardModule extends Module {
    private static boolean appliedHide = false;

    public ScoreboardModule() {
        super("scoreboard", "Scoreboard");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("display", "Sidebar", Arrays.asList("Show", "Hide")));
    }

    public static void apply(Minecraft mc) {
        Module self = Modules.get("scoreboard");
        boolean hide = self != null && self.enabled() && "Hide".equals(self.opt("display"));
        if (hide && !appliedHide) {
            GuiIngameForge.renderObjective = false;
            appliedHide = true;
        } else if (!hide && appliedHide) {
            GuiIngameForge.renderObjective = true;
            appliedHide = false;
        }
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
