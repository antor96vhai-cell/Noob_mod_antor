package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Particle tweaks for the player's own combat feedback:
 * <ul>
 *   <li><b>Crit particle multiplier (0.5–3x):</b> when the local
 *   player lands an attack that meets vanilla's critical-hit
 *   conditions (falling, airborne — the same fields the game itself
 *   checks), extra crit particles are spawned client-side at the
 *   target, scaled by (multiplier − 1). Enchanted weapons produce
 *   magic-crit particles, like vanilla. Honest limit: crit particles
 *   in multiplayer are spawned from server packets, and 1.8.9 Forge
 *   has no event to intercept or remove them, so the multiplier can
 *   only ADD particles (values below 1.0 behave like 1.0 for the
 *   server-sent base burst — use the density option to reduce).</li>
 *   <li><b>Particle density:</b> Vanilla / Reduced / Minimal, driving
 *   the vanilla particle setting while the module is on — the
 *   supported way to cut overall particle load (and FPS cost).</li>
 * </ul>
 * Attack presses are polled exactly like the CPS/Combo trackers, so
 * the module costs nothing when idle.
 */
public class ParticlesModule extends Module {

    private static boolean wasAttackDown = false;
    private static final Random RANDOM = new Random();

    public ParticlesModule() {
        super("particles", "Particles");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<SliderOpt> sliders() {
        return Collections.singletonList(new SliderOpt("crit", "Crit particles", 0.5, 3.0, 0.5, 1.0));
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("density", "Particle density",
                Arrays.asList("Vanilla", "Reduced", "Minimal")));
    }

    public static void poll(Minecraft mc) {
        ParticlesModule self = (ParticlesModule) Modules.get("particles");
        if (self == null || !self.enabled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        boolean down = Mouse.isButtonDown(0) && mc.currentScreen == null;
        boolean pressed = down && !wasAttackDown;
        wasAttackDown = down;
        if (!pressed) return;
        double mult = self.sliderVal(self.sliders().get(0));
        if (mult <= 1.0) return;
        MovingObjectPosition hit = mc.objectMouseOver;
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY
                || hit.entityHit == null) return;
        // Vanilla critical-hit conditions (client-visible fields).
        if (!(mc.thePlayer.fallDistance > 0.0f && !mc.thePlayer.onGround
                && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater())) return;
        Entity target = hit.entityHit;
        ItemStack held = mc.thePlayer.getHeldItem();
        EnumParticleTypes type = (held != null && held.isItemEnchanted())
                ? EnumParticleTypes.CRIT_MAGIC : EnumParticleTypes.CRIT;
        int extra = (int) Math.round(10 * (mult - 1.0));
        for (int i = 0; i < extra; i++) {
            double px = target.posX + (RANDOM.nextDouble() - 0.5) * target.width;
            double py = target.posY + RANDOM.nextDouble() * target.height;
            double pz = target.posZ + (RANDOM.nextDouble() - 0.5) * target.width;
            mc.theWorld.spawnParticle(type, px, py, pz,
                    (RANDOM.nextDouble() - 0.5) * 0.4, RANDOM.nextDouble() * 0.3,
                    (RANDOM.nextDouble() - 0.5) * 0.4);
        }
    }

    public static void tick(Minecraft mc) {
        ParticlesModule self = (ParticlesModule) Modules.get("particles");
        if (self == null || !self.enabled()) return;
        if (mc.gameSettings == null) return;
        String density = self.opt("density");
        int target = "Reduced".equals(density) ? 1 : ("Minimal".equals(density) ? 2 : -1);
        if (target >= 0 && mc.gameSettings.particleSetting != target) {
            mc.gameSettings.particleSetting = target;
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
