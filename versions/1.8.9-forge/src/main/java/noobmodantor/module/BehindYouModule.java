package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;

/**
 * Behind You: hold the Behind You key (default F8) in third person
 * and the camera snaps to look directly behind the player (180°);
 * release and the normal view returns instantly. Implemented as a
 * pure CameraSetup substitution — the same hook as Freelook — so the
 * player's facing is never touched and there is nothing to restore.
 * Stays out of the way in first person, with a screen open, or while
 * Freelook/Freecam own the camera.
 */
public class BehindYouModule extends Module {

    private static boolean active = false;

    public BehindYouModule() {
        super("behind_you", "Behind You");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    public static void tick(Minecraft mc) {
        BehindYouModule self = (BehindYouModule) Modules.get("behind_you");
        active = self != null && self.enabled()
                && !FreecamModule.isActive()
                && !FreelookModule.isActive()
                && mc.thePlayer != null
                && mc.currentScreen == null
                && mc.gameSettings != null && mc.gameSettings.thirdPersonView > 0
                && noobmodantor.NoobModAntor.behindKey != null
                && noobmodantor.NoobModAntor.behindKey.isKeyDown();
    }

    public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (!active) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (event.entity != mc.thePlayer) return;
        event.yaw += 180.0f;
        event.pitch = -event.pitch;
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
