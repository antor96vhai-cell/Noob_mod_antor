package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;

/**
 * Freelook / perspective (orbit): while the Freelook key (default Left
 * Alt) is held in third person, the mouse orbits the camera around the
 * player while the player's own facing stays exactly where it was.
 *
 * <p>Implementation uses only public, javap-verified hooks:
 * <ul>
 *   <li>Each client tick (END phase, i.e. after vanilla has applied the
 *   mouse look to the player), the rotation the mouse just added is
 *   measured, accumulated into the free-camera yaw/pitch, and the
 *   player's rotation (including prevRotation, so rendering does not
 *   smear) is restored to the values captured when the key was
 *   pressed. The player therefore genuinely never turns — movement,
 *   aiming and everything the server sees keep the original facing.</li>
 *   <li>{@link EntityViewRenderEvent.CameraSetup} (fired by Forge's
 *   EntityRenderer patch inside orientCamera) then substitutes the
 *   interpolated free yaw/pitch for the camera, which is what positions
 *   the third-person camera.</li>
 *   <li>On release nothing needs undoing: the player's rotation already
 *   holds the original values, so the camera snaps back behind the
 *   player exactly as vanilla.</li>
 * </ul>
 * In first person, with a screen open, or while Freecam is active, the
 * module stays out of the way.
 */
public class FreelookModule extends Module {

    private static boolean active = false;
    private static float savedYaw = 0.0f;
    private static float savedPitch = 0.0f;
    private static float freeYaw = 0.0f;
    private static float freePitch = 0.0f;
    private static float prevFreeYaw = 0.0f;
    private static float prevFreePitch = 0.0f;

    public FreelookModule() {
        super("freelook", "Freelook");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    public static boolean isActive() {
        return active;
    }

    public static void tick(Minecraft mc) {
        FreelookModule self = (FreelookModule) Modules.get("freelook");
        EntityPlayerSP player = mc.thePlayer;
        boolean want = self != null && self.enabled()
                && !FreecamModule.isActive()
                && player != null
                && mc.currentScreen == null
                && mc.gameSettings != null && mc.gameSettings.thirdPersonView > 0
                && noobmodantor.NoobModAntor.freelookKey != null
                && noobmodantor.NoobModAntor.freelookKey.isKeyDown();

        if (!want) {
            // The player's rotation was restored every tick while
            // active, so deactivation is inherently clean.
            active = false;
            return;
        }

        if (!active) {
            active = true;
            savedYaw = player.rotationYaw;
            savedPitch = player.rotationPitch;
            freeYaw = prevFreeYaw = savedYaw;
            freePitch = prevFreePitch = savedPitch;
        } else {
            prevFreeYaw = freeYaw;
            prevFreePitch = freePitch;
            freeYaw += player.rotationYaw - savedYaw;
            freePitch += player.rotationPitch - savedPitch;
            freePitch = MathHelper.clamp_float(freePitch, -90.0f, 90.0f);
        }
        player.rotationYaw = savedYaw;
        player.rotationPitch = savedPitch;
        player.prevRotationYaw = savedYaw;
        player.prevRotationPitch = savedPitch;
    }

    public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (!active) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (event.entity != mc.thePlayer) return;
        float partial = (float) event.renderPartialTicks;
        event.yaw = prevFreeYaw + (freeYaw - prevFreeYaw) * partial;
        event.pitch = MathHelper.clamp_float(
                prevFreePitch + (freePitch - prevFreePitch) * partial, -90.0f, 90.0f);
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
