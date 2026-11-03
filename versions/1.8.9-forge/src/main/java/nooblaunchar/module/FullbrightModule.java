package nooblaunchar.module;

import net.minecraft.client.Minecraft;
import nooblaunchar.NoobConfig;

/**
 * Fullbright: while enabled, drives the vanilla gamma setting
 * ({@code gameSettings.gammaSetting}) to 100 — the classic 1.8.9 fullbright
 * value; the lightmap brightness curve saturates far below that. On disable,
 * the player's exact previous gamma is restored. The captured base value is
 * persisted in the config so an unclean shutdown is repaired on next launch.
 */
public class FullbrightModule extends Module {
    private static final float FULLBRIGHT_GAMMA = 100.0f;
    private static float baseGamma = -1;

    public FullbrightModule() {
        super("fullbright", "Fullbright");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    public static void tick(Minecraft mc) {
        Module self = Modules.get("fullbright");
        if (self == null || mc.gameSettings == null) return;
        if (self.enabled()) {
            if (baseGamma < 0) {
                baseGamma = mc.gameSettings.gammaSetting;
                NoobConfig cfg = NoobConfig.get();
                if (!cfg.gammaOverrideActive) {
                    cfg.baseGamma = (double) baseGamma;
                    cfg.gammaOverrideActive = true;
                    NoobConfig.save();
                }
            }
            mc.gameSettings.gammaSetting = FULLBRIGHT_GAMMA;
        } else if (baseGamma >= 0) {
            restore(mc);
        }
    }

    private static void restore(Minecraft mc) {
        if (baseGamma >= 0) mc.gameSettings.gammaSetting = baseGamma;
        baseGamma = -1;
        NoobConfig cfg = NoobConfig.get();
        if (cfg.gammaOverrideActive) {
            cfg.gammaOverrideActive = false;
            cfg.baseGamma = null;
            NoobConfig.save();
        }
    }

    /** If a previous session died with fullbright on, put the user's gamma back. Called once on first tick. */
    public static void cleanupOnStart(Minecraft mc) {
        NoobConfig cfg = NoobConfig.get();
        if (cfg.gammaOverrideActive && cfg.baseGamma != null && mc.gameSettings != null) {
            mc.gameSettings.gammaSetting = cfg.baseGamma.floatValue();
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
    public void render(Minecraft mc, int x, int y) {
    }
}
