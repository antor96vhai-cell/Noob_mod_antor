package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

/**
 * Freecam ("Photo Fly"): a toggle key (default F6) detaches the camera
 * from the player and lets it fly freely — mouse look, WASD, Jump up,
 * Sneak down, Sprint for 3x speed — for taking screenshots.
 *
 * <p>How it works (public, javap-verified APIs only):
 * <ul>
 *   <li>The render view entity is swapped to a tiny dummy {@link Entity}
 *   subclass ({@link CameraEntity}) via
 *   {@code Minecraft.setRenderViewEntity}. The dummy is never added to
 *   the world, so it is never rendered or ticked; it only carries the
 *   camera position/rotation. Using a plain Entity (not a player
 *   entity) keeps every renderer code path on its generic branch —
 *   the same mechanism vanilla spectator mode uses to view arbitrary
 *   entities.</li>
 *   <li>The player's own movement is frozen by swapping
 *   {@code EntityPlayerSP.movementInput} for a zeroed
 *   {@link MovementInput} (its update method is a no-op), and the
 *   player's rotation is restored every tick exactly as in Freelook —
 *   so the character stands still and sends no movement to the server.
 *   The mouse deltas vanilla applies to the player are consumed into
 *   the camera's yaw/pitch instead.</li>
 *   <li>Toggle key or ESC exits. If any GUI opens mid-flight the camera
 *   is restored instantly; if that GUI is the pause menu opened by the
 *   ESC press itself, it is closed again so ESC feels like a pure
 *   exit. Death / world unload also force a clean restore.</li>
 * </ul>
 *
 * <p>Limits (also in BUILD_NOTES.md): the camera can only see chunks
 * the server has already sent and the client has rendered; the player
 * never actually moves server-side; some servers ban freecam use —
 * that is the user's rules responsibility.
 */
public class FreecamModule extends Module {

    /** Minimal camera carrier: an Entity with zero eye height that is never spawned into the world. */
    public static class CameraEntity extends Entity {
        public CameraEntity(World world) {
            super(world);
            this.setSize(0.0f, 0.0f);
        }

        @Override
        protected void entityInit() {
        }

        @Override
        public float getEyeHeight() {
            return 0.0f;
        }

        @Override
        protected void readEntityFromNBT(NBTTagCompound tag) {
        }

        @Override
        protected void writeEntityToNBT(NBTTagCompound tag) {
        }
    }

    private static boolean active = false;
    private static CameraEntity camera = null;
    private static MovementInput savedInput = null;
    private static float savedYaw = 0.0f;
    private static float savedPitch = 0.0f;
    private static float camYaw = 0.0f;
    private static float camPitch = 0.0f;
    private static float prevCamYaw = 0.0f;
    private static float prevCamPitch = 0.0f;

    public FreecamModule() {
        super("freecam", "Freecam");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<SliderOpt> sliders() {
        return Collections.singletonList(new SliderOpt("speed", "Fly speed (blocks/s)", 2, 40, 1, 10));
    }

    public static boolean isActive() {
        return active;
    }

    public static void tick(Minecraft mc) {
        FreecamModule self = (FreecamModule) Modules.get("freecam");
        if (self == null) return;

        // Toggle key (only while no screen is open).
        if (noobmodantor.NoobModAntor.freecamKey != null) {
            while (noobmodantor.NoobModAntor.freecamKey.isPressed()) {
                if (mc.currentScreen == null && mc.thePlayer != null && mc.theWorld != null
                        && self.enabled()) {
                    if (active) {
                        deactivate(mc);
                    } else {
                        activate(mc);
                    }
                }
            }
        }
        if (!active) return;

        // Forced clean exits.
        if (!self.enabled() || mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.isDead) {
            deactivate(mc);
            return;
        }
        if (mc.currentScreen != null) {
            boolean pauseMenu = mc.currentScreen instanceof GuiIngameMenu;
            deactivate(mc);
            if (pauseMenu) {
                // The ESC press that opened this menu counts as the
                // exit key: close the menu again so ESC is a pure exit.
                mc.displayGuiScreen(null);
            }
            return;
        }

        EntityPlayerSP player = mc.thePlayer;

        // Consume this tick's mouse look into the camera, then freeze
        // the player's own facing at its toggle-on values.
        prevCamYaw = camYaw;
        prevCamPitch = camPitch;
        camYaw += player.rotationYaw - savedYaw;
        camPitch += player.rotationPitch - savedPitch;
        camPitch = MathHelper.clamp_float(camPitch, -90.0f, 90.0f);
        player.rotationYaw = savedYaw;
        player.rotationPitch = savedPitch;
        player.prevRotationYaw = savedYaw;
        player.prevRotationPitch = savedPitch;

        // Fly movement from the movement keybinds.
        double speed = self.sliderVal(self.sliders().get(0));
        if (mc.gameSettings.keyBindSprint.isKeyDown()) speed *= 3.0;
        double step = speed * 0.05; // per tick (20 ticks/s)
        double yawRad = Math.toRadians(camYaw);
        double pitchRad = Math.toRadians(camPitch);
        // Look vector (Minecraft convention) and horizontal strafe vectors.
        double lookX = -Math.sin(yawRad) * Math.cos(pitchRad);
        double lookY = -Math.sin(pitchRad);
        double lookZ = Math.cos(yawRad) * Math.cos(pitchRad);
        double rightX = -Math.cos(yawRad);
        double rightZ = -Math.sin(yawRad);

        double mx = 0, my = 0, mz = 0;
        if (mc.gameSettings.keyBindForward.isKeyDown()) { mx += lookX; my += lookY; mz += lookZ; }
        if (mc.gameSettings.keyBindBack.isKeyDown()) { mx -= lookX; my -= lookY; mz -= lookZ; }
        if (mc.gameSettings.keyBindRight.isKeyDown()) { mx += rightX; mz += rightZ; }
        if (mc.gameSettings.keyBindLeft.isKeyDown()) { mx -= rightX; mz -= rightZ; }
        if (mc.gameSettings.keyBindJump.isKeyDown()) my += 1.0;
        if (mc.gameSettings.keyBindSneak.isKeyDown()) my -= 1.0;

        camera.lastTickPosX = camera.posX;
        camera.lastTickPosY = camera.posY;
        camera.lastTickPosZ = camera.posZ;
        camera.prevPosX = camera.posX;
        camera.prevPosY = camera.posY;
        camera.prevPosZ = camera.posZ;
        camera.prevRotationYaw = prevCamYaw;
        camera.prevRotationPitch = prevCamPitch;
        camera.rotationYaw = camYaw;
        camera.rotationPitch = camPitch;
        camera.setPosition(camera.posX + mx * step, camera.posY + my * step, camera.posZ + mz * step);
    }

    private static void activate(Minecraft mc) {
        EntityPlayerSP player = mc.thePlayer;
        camera = new CameraEntity(mc.theWorld);
        savedYaw = player.rotationYaw;
        savedPitch = player.rotationPitch;
        camYaw = prevCamYaw = savedYaw;
        camPitch = prevCamPitch = savedPitch;
        double eyeX = player.posX;
        double eyeY = player.posY + player.getEyeHeight();
        double eyeZ = player.posZ;
        camera.setPosition(eyeX, eyeY, eyeZ);
        camera.lastTickPosX = camera.prevPosX = eyeX;
        camera.lastTickPosY = camera.prevPosY = eyeY;
        camera.lastTickPosZ = camera.prevPosZ = eyeZ;
        camera.rotationYaw = camYaw;
        camera.rotationPitch = camPitch;
        camera.prevRotationYaw = camYaw;
        camera.prevRotationPitch = camPitch;
        savedInput = player.movementInput;
        player.movementInput = new MovementInput();
        mc.setRenderViewEntity(camera);
        active = true;
    }

    private static void deactivate(Minecraft mc) {
        if (mc != null) {
            if (mc.thePlayer != null) {
                mc.setRenderViewEntity(mc.thePlayer);
                if (savedInput != null) {
                    mc.thePlayer.movementInput = savedInput;
                }
            }
        }
        savedInput = null;
        camera = null;
        active = false;
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
