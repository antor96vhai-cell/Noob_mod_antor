package kutu.kutuclient.module;

import kutu.kutuclient.hud.CpsTracker;
import net.minecraft.client.Minecraft;

import java.util.List;

/** Shows left | right clicks per second (see {@link CpsTracker}). */
public class CpsModule extends TextModule {

    public CpsModule() {
        super("cps", "CPS Counter");
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        return List.of("CPS: " + CpsTracker.leftCps() + " | " + CpsTracker.rightCps());
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 18};
    }
}
