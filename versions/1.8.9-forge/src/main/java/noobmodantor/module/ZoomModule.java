package noobmodantor.module;

import net.minecraft.client.Minecraft;
import noobmodantor.NoobConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Hold-to-zoom. Smoothly drives the vanilla FOV setting
 * ({@code gameSettings.fovSetting}, a public float in 1.8.9) towards
 * {@code baseFov / factor} while the zoom key is held, scaling mouse
 * sensitivity by the same factor, and restores the player's exact FOV and
 * sensitivity on release. The captured base values are also persisted in
 * the config so an unclean shutdown mid-zoom is repaired on next launch.
 */
public class ZoomModule extends Module {
    private static float baseFov = -1;
    private static float smoothedFov = -1;
    private static float baseSensitivity = -1;
    /** Set every client tick from the zoom key binding. */
    public static boolean keyHeld = false;

    /** True while Zoom is actively overriding the FOV (a base FOV is captured). Used by the FOV module to stay out of the way. */
    public static boolean isZooming() {
        return baseFov >= 0;
    }

    public ZoomModule() {
        super("zoom", "Zoom");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("factor", "Zoom factor", Arrays.asList("2x", "3x", "4x")));
    }

    private float factor() {
        String f = opt("factor");
        if ("2x".equals(f)) return 2.0f;
        if ("4x".equals(f)) return 4.0f;
        return 3.0f;
    }

    public static void tick(Minecraft mc) {
        ZoomModule self = (ZoomModule) Modules.get("zoom");
        if (self == null || mc.gameSettings == null) return;
        boolean active = self.enabled() && keyHeld && mc.thePlayer != null;

        if (active) {
            if (baseFov < 0) {
                baseFov = mc.gameSettings.fovSetting;
                smoothedFov = baseFov;
                baseSensitivity = mc.gameSettings.mouseSensitivity;
                NoobConfig cfg = NoobConfig.get();
                cfg.baseFov = (double) baseFov;
                cfg.fovOverrideActive = true;
                NoobConfig.save();
            }
            float factor = self.factor();
            float target = Math.max(10.0f, baseFov / factor);
            smoothedFov += (target - smoothedFov) * 0.4f;
            if (Math.abs(target - smoothedFov) < 0.25f) smoothedFov = target;
            mc.gameSettings.fovSetting = smoothedFov;
            float zoomSens = Math.max(0.0f, Math.min(1.0f, baseSensitivity / factor));
            mc.gameSettings.mouseSensitivity = zoomSens;
        } else if (baseFov >= 0) {
            restore(mc);
        }
    }

    private static void restore(Minecraft mc) {
        if (baseFov >= 0) mc.gameSettings.fovSetting = baseFov;
        if (baseSensitivity >= 0) mc.gameSettings.mouseSensitivity = baseSensitivity;
        baseFov = -1;
        smoothedFov = -1;
        baseSensitivity = -1;
        NoobConfig cfg = NoobConfig.get();
        if (cfg.fovOverrideActive) {
            cfg.fovOverrideActive = false;
            NoobConfig.save();
        }
    }

    /** If a previous session died mid-zoom, put the user's FOV back. Called once on first tick. */
    public static void cleanupOnStart(Minecraft mc) {
        NoobConfig cfg = NoobConfig.get();
        if (cfg.fovOverrideActive && cfg.baseFov != null && mc.gameSettings != null) {
            mc.gameSettings.fovSetting = cfg.baseFov.floatValue();
        }
        cfg.fovOverrideActive = false;
        cfg.baseFov = null;
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
