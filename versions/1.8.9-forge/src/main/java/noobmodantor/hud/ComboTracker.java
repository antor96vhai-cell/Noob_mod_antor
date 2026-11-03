package noobmodantor.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

/**
 * Best-effort, purely client-side combo counter. A "hit" is counted when
 * the attack key is newly pressed while the crosshair is over an entity —
 * the closest a client mod can get without server feedback. The combo
 * resets when more than 2 seconds pass without a hit, or when the player
 * takes damage (their own hit streak was interrupted). Because it never
 * sees the server's view, fast clicking, lag or sweeping attacks can make
 * it differ slightly from a server-side count: it is an approximation by
 * design (same limitation as every client-side combo counter).
 *
 * Polled once per rendered frame from the overlay handler, like
 * {@link CpsTracker}.
 */
public final class ComboTracker {
    private static final long WINDOW_MS = 2000L;

    private static boolean prevAttack;
    private static int prevHurtTime;
    private static long lastHitAt = 0L;
    private static int current = 0;
    private static int best = 0;

    private ComboTracker() {
    }

    public static void poll(Minecraft mc) {
        if (mc.thePlayer == null || mc.currentScreen != null || !mc.inGameHasFocus) {
            prevAttack = false;
            return;
        }
        // Taking damage breaks the combo.
        int hurtTime = mc.thePlayer.hurtTime;
        if (hurtTime > prevHurtTime) {
            current = 0;
            lastHitAt = 0L;
        }
        prevHurtTime = hurtTime;

        boolean attack = mc.gameSettings.keyBindAttack.isKeyDown();
        if (attack && !prevAttack) {
            MovingObjectPosition target = mc.objectMouseOver;
            if (target != null && target.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                long now = System.currentTimeMillis();
                current = (now - lastHitAt <= WINDOW_MS) ? current + 1 : 1;
                lastHitAt = now;
                if (current > best) best = current;
            }
        }
        prevAttack = attack;

        if (current > 0 && System.currentTimeMillis() - lastHitAt > WINDOW_MS) {
            current = 0;
        }
    }

    public static int current() {
        return current;
    }

    public static int best() {
        return best;
    }
}
