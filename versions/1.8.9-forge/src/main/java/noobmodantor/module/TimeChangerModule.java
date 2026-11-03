package noobmodantor.module;

import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Locks the client-side world time to a fixed point of day. This is a
 * purely visual change: it only sets the local world's time (used for sky,
 * lighting and the clock), never the server's — gameplay, mob spawning and
 * other players are unaffected. The time is re-asserted every tick, which
 * also overrides the periodic time-sync packets servers send. Ships
 * disabled by default so it never surprises anyone.
 */
public class TimeChangerModule extends Module {

    public TimeChangerModule() {
        super("time_changer", "Time Changer");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("time", "Locked time",
                Arrays.asList("Day", "Noon", "Sunset", "Night", "Midnight")));
    }

    private long targetTime() {
        String t = opt("time");
        if ("Noon".equals(t)) return 6000L;
        if ("Sunset".equals(t)) return 12000L;
        if ("Night".equals(t)) return 13000L;
        if ("Midnight".equals(t)) return 18000L;
        return 1000L; // Day
    }

    public static void tick(Minecraft mc) {
        Module self = Modules.get("time_changer");
        if (self == null || !self.enabled()) return;
        if (mc.theWorld == null) return;
        mc.theWorld.setWorldTime(((TimeChangerModule) self).targetTime());
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
