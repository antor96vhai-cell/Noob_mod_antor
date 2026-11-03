package noobmodantor.module;

import net.minecraft.client.Minecraft;
import noobmodantor.hud.ComboTracker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Shows the current hit combo (and optionally the session best). Counted
 * entirely client-side by {@link ComboTracker} — an approximation, since a
 * client mod cannot see the server's hit registration.
 */
public class ComboModule extends TextModule {

    public ComboModule() {
        super("combo", "Combo Counter");
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("best", "Show best combo", Arrays.asList("Off", "On")));
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        String text = "Combo: " + ComboTracker.current();
        if ("On".equals(opt("best"))) {
            text += " (Best: " + ComboTracker.best() + ")";
        }
        return Collections.singletonList(text);
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 98};
    }
}
