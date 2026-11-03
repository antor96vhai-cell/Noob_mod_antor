package noobmodantor.module;

import net.minecraft.client.Minecraft;
import noobmodantor.hud.CpsTracker;

import java.util.Collections;
import java.util.List;

/** Shows left | right clicks per second (see {@link CpsTracker}). */
public class CpsModule extends TextModule {

    public CpsModule() {
        super("cps", "CPS Counter");
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        return Collections.singletonList("CPS: " + CpsTracker.leftCps() + " | " + CpsTracker.rightCps());
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 18};
    }
}
