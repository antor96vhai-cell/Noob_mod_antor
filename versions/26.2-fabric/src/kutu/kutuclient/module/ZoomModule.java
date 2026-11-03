package kutu.kutuclient.module;

import kutu.kutuclient.KutuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * Hold-to-zoom. Smoothly drives the vanilla FOV option towards
 * {@code baseFov / factor} while the zoom key is held and restores the
 * player's exact FOV and mouse sensitivity on release.
 *
 * <p>Implemented without a mixin: {@code OptionInstance.set} validates its
 * value (verified in the 26.2 jar bytecode — invalid values fall back to
 * the option's default), so the target FOV is clamped to the vanilla FOV
 * range [30, 110]. Mouse sensitivity is scaled by the same factor so
 * aiming stays controllable while zoomed.</p>
 */
public class ZoomModule extends Module {
    /** Vanilla FOV option range in 26.2. */
    private static final int MIN_FOV = 30;
    private static final int MAX_FOV = 110;

    private static int baseFov = -1;
    private static double smoothedFov = -1;
    private static double baseSensitivity = -1;
    /** Set every client tick by KutuClient from the zoom key binding. */
    public static boolean keyHeld = false;

    public ZoomModule() {
        super("zoom", "Zoom");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<Opt> options() {
        return List.of(new Opt("factor", "Zoom factor", "2x", "3x", "4x"));
    }

    private double factor() {
        return switch (opt("factor")) {
            case "2x" -> 2.0;
            case "4x" -> 4.0;
            default -> 3.0;
        };
    }

    public static void tick(Minecraft mc) {
        ZoomModule self = (ZoomModule) Modules.get("zoom");
        if (self == null || mc.options == null) return;
        boolean active = self.enabled() && keyHeld && mc.player != null;
        OptionInstance<Integer> fov = mc.options.fov();
        OptionInstance<Double> sensitivity = mc.options.sensitivity();

        if (active) {
            if (baseFov < 0) {
                baseFov = fov.get();
                smoothedFov = baseFov;
                baseSensitivity = sensitivity.get();
                KutuConfig cfg = KutuConfig.get();
                cfg.baseFov = baseFov;
                cfg.fovOverrideActive = true;
                KutuConfig.save();
            }
            double factor = self.factor();
            double target = Math.max(MIN_FOV, Math.min(MAX_FOV, baseFov / factor));
            smoothedFov += (target - smoothedFov) * 0.4;
            if (Math.abs(target - smoothedFov) < 0.75) smoothedFov = target;
            int newFov = (int) Math.round(smoothedFov);
            if (fov.get() != newFov) fov.set(newFov);
            double zoomSens = Math.max(0.0, Math.min(1.0, baseSensitivity / factor));
            if (Double.compare(sensitivity.get(), zoomSens) != 0) sensitivity.set(zoomSens);
        } else if (baseFov >= 0) {
            restore(mc);
        }
    }

    private static void restore(Minecraft mc) {
        if (baseFov >= 0) mc.options.fov().set(Math.max(MIN_FOV, Math.min(MAX_FOV, baseFov)));
        if (baseSensitivity >= 0) mc.options.sensitivity().set(baseSensitivity);
        baseFov = -1;
        smoothedFov = -1;
        baseSensitivity = -1;
        KutuConfig cfg = KutuConfig.get();
        if (cfg.fovOverrideActive) {
            cfg.fovOverrideActive = false;
            KutuConfig.save();
        }
    }

    /** If a previous session died mid-zoom, put the user's FOV back. Called once on first tick. */
    public static void cleanupOnStart(Minecraft mc) {
        KutuConfig cfg = KutuConfig.get();
        if (cfg.fovOverrideActive && cfg.baseFov != null && mc.options != null) {
            int base = Math.max(MIN_FOV, Math.min(MAX_FOV, cfg.baseFov));
            if (mc.options.fov().get() != base) mc.options.fov().set(base);
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
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
    }
}
