package noobmodantor.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import noobmodantor.NoobConfig;
import org.lwjgl.opengl.Display;

/**
 * Dynamic FPS (Performance page toggle): while the game window is
 * unfocused, the framerate limit is temporarily dropped to the
 * configured cap so an alt-tabbed client costs almost no GPU/CPU;
 * when focus returns, the user's own limit is restored exactly. The
 * cap is applied in memory only — it is never written to the vanilla
 * options file.
 */
public final class DynamicFps {
    private static boolean capped = false;
    private static int savedLimit = 260;

    private DynamicFps() {
    }

    public static void tick(Minecraft mc) {
        GameSettings gs = mc.gameSettings;
        if (gs == null) return;
        if (!NoobConfig.get().dynamicFps) {
            if (capped) restore(gs);
            return;
        }
        boolean focused;
        try {
            focused = Display.isActive();
        } catch (Throwable t) {
            focused = true; // never cap on a broken focus probe
        }
        if (!focused && !capped) {
            savedLimit = gs.limitFramerate;
            gs.limitFramerate = NoobConfig.get().dynamicFpsCap;
            capped = true;
        } else if (focused && capped) {
            restore(gs);
        }
    }

    private static void restore(GameSettings gs) {
        gs.limitFramerate = savedLimit;
        capped = false;
    }
}
