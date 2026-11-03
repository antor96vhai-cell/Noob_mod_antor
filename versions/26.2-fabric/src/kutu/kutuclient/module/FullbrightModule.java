package kutu.kutuclient.module;

import kutu.kutuclient.KutuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Fullbright: while enabled, drives the vanilla gamma (brightness) option
 * to its maximum of 1.0; on disable, restores the player's exact previous
 * gamma. The 26.2 lightmap extractor computes lightmap brightness directly
 * from {@code options.gamma()} (verified in the jar bytecode), so max gamma
 * gives the brightest rendering vanilla supports.
 *
 * <p>A true lightmap-level override would need a mixin into
 * LightmapRenderStateExtractor; the Sponge Mixin annotation API is not
 * present in this build's offline classpath, so gamma is used instead —
 * see BUILD_NOTES.md.</p>
 */
public class FullbrightModule extends Module {
    private static double baseGamma = -1;

    public FullbrightModule() {
        super("fullbright", "Fullbright");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    public static void tick(Minecraft mc) {
        Module self = Modules.get("fullbright");
        if (self == null || mc.options == null) return;
        OptionInstance<Double> gamma = mc.options.gamma();
        if (self.enabled()) {
            if (baseGamma < 0) {
                baseGamma = gamma.get();
                KutuConfig cfg = KutuConfig.get();
                if (!cfg.gammaOverrideActive) {
                    cfg.baseGamma = baseGamma;
                    cfg.gammaOverrideActive = true;
                    KutuConfig.save();
                }
            }
            if (Double.compare(gamma.get(), 1.0) != 0) gamma.set(1.0);
        } else if (baseGamma >= 0) {
            restore(mc);
        }
    }

    private static void restore(Minecraft mc) {
        if (baseGamma >= 0) mc.options.gamma().set(Math.max(0.0, Math.min(1.0, baseGamma)));
        baseGamma = -1;
        KutuConfig cfg = KutuConfig.get();
        if (cfg.gammaOverrideActive) {
            cfg.gammaOverrideActive = false;
            cfg.baseGamma = null;
            KutuConfig.save();
        }
    }

    /** If a previous session died with fullbright on, put the user's gamma back. Called once on first tick. */
    public static void cleanupOnStart(Minecraft mc) {
        KutuConfig cfg = KutuConfig.get();
        if (cfg.gammaOverrideActive && cfg.baseGamma != null && mc.options != null) {
            double base = Math.max(0.0, Math.min(1.0, cfg.baseGamma));
            if (Double.compare(mc.options.gamma().get(), base) != 0) mc.options.gamma().set(base);
        }
        cfg.gammaOverrideActive = false;
        cfg.baseGamma = null;
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
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
    }
}
