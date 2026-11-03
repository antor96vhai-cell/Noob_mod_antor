package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base FOV changer: a slider (30-110) that keeps the vanilla
 * {@code gameSettings.fovSetting} at the chosen value while the module is
 * enabled. It is deliberately independent from Zoom:
 * <ul>
 *   <li>While Zoom is actively overriding the FOV, this module does not
 *   touch the setting at all; Zoom captured this module's value as its
 *   base and restores exactly to it on release.</li>
 *   <li>The first time the module runs with no stored value, it seeds the
 *   slider from the player's current FOV, so enabling it never causes a
 *   sudden FOV jump.</li>
 *   <li>Disabling it simply stops touching the setting; the value left in
 *   place becomes the vanilla FOV until changed in Options.</li>
 * </ul>
 */
public class FovModule extends Module {

    public FovModule() {
        super("fov", "FOV Changer");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<SliderOpt> sliders() {
        return Collections.singletonList(new SliderOpt("fov", "Base FOV", 30, 110, 1, 70));
    }

    @Override
    public List<Opt> options() {
        return Arrays.asList(
                new Opt("dynamic_fov", "Dynamic FOV", Arrays.asList("On", "Off")),
                new Opt("lock_fov", "Lock FOV", Arrays.asList("Off", "On")));
    }

    /**
     * Dynamic FOV off: neutralize the sprint / flying / speed-effect
     * FOV multiplier at its source. FOVUpdateEvent's newfov is the
     * multiplier EntityPlayerSP computed from movement speed, flying
     * and speed potions; pinning it to 1.0 removes exactly those
     * changes and leaves the base FOV (and Zoom) alone.
     */
    public static void onFovUpdate(FOVUpdateEvent event) {
        FovModule self = (FovModule) Modules.get("fov");
        if (self == null || !self.enabled()) return;
        if ("Off".equals(self.opt("dynamic_fov"))) {
            event.newfov = 1.0f;
        }
    }

    /**
     * Lock FOV: pin the final rendered FOV to the base FOV setting,
     * ignoring every dynamic modifier (sprint, potions, bow draw).
     * While Zoom is overriding, gameSettings.fovSetting holds the
     * zoomed value, so zoom keeps working. Note: the same hook also
     * serves the first-person hand pass, so with Lock on the hand
     * renders at the world FOV instead of vanilla's fixed hand FOV —
     * cosmetic only.
     */
    public static void onFovModifier(EntityViewRenderEvent.FOVModifier event) {
        FovModule self = (FovModule) Modules.get("fov");
        if (self == null || !self.enabled()) return;
        if (!"On".equals(self.opt("lock_fov"))) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings == null) return;
        if (event.entity != mc.thePlayer) return;
        event.setFOV(mc.gameSettings.fovSetting);
    }

    public static void tick(Minecraft mc) {
        FovModule self = (FovModule) Modules.get("fov");
        if (self == null || mc.gameSettings == null || mc.thePlayer == null) return;
        if (!self.enabled()) return;
        if (ZoomModule.isZooming()) return; // Zoom owns the FOV right now
        SliderOpt slider = self.sliders().get(0);
        if (!self.cfg().options.containsKey(slider.key)) {
            self.setSlider(slider, mc.gameSettings.fovSetting); // seed, no jump
            return;
        }
        float target = (float) self.sliderVal(slider);
        if (Math.abs(mc.gameSettings.fovSetting - target) > 0.01f) {
            mc.gameSettings.fovSetting = target;
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
