package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;

import java.util.Collections;
import java.util.List;

/**
 * Walk-animation speed multiplier, visual only. During each living
 * entity's render call (RenderLivingEvent.Pre/Post) the entity's
 * {@code limbSwing} phase is temporarily scaled by the multiplier and
 * restored immediately afterwards, so the legs/arms cycle faster or
 * slower on screen while the entity's real movement, position and
 * server-visible state are untouched. At 1.0x nothing is modified.
 */
public class AnimationModule extends Module {

    private static EntityLivingBase savedEntity = null;
    private static float savedLimbSwing = 0.0f;

    public AnimationModule() {
        super("animation", "Animation Speed");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<SliderOpt> sliders() {
        return Collections.singletonList(new SliderOpt("speed", "Speed multiplier", 0.25, 3.0, 0.25, 1.0));
    }

    public double multiplier() {
        return sliderVal(sliders().get(0));
    }

    public static void applyPre(EntityLivingBase entity) {
        AnimationModule self = (AnimationModule) Modules.get("animation");
        if (self == null || !self.enabled()) return;
        double m = self.multiplier();
        if (m == 1.0) return;
        savedEntity = entity;
        savedLimbSwing = entity.limbSwing;
        entity.limbSwing = (float) (entity.limbSwing * m);
    }

    public static void applyPost(EntityLivingBase entity) {
        if (savedEntity == entity) {
            entity.limbSwing = savedLimbSwing;
            savedEntity = null;
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
