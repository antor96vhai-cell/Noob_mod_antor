package nooblaunchar.hud;

import org.lwjgl.input.Mouse;

import java.util.ArrayDeque;

/**
 * Counts left/right mouse clicks in a rolling 1-second window.
 * Polled once per rendered frame from the overlay handler using
 * {@code Mouse.isButtonDown} edge detection, so no input hooks are needed.
 */
public final class CpsTracker {
    private static final ArrayDeque<Long> LEFT_CLICKS = new ArrayDeque<Long>();
    private static final ArrayDeque<Long> RIGHT_CLICKS = new ArrayDeque<Long>();
    private static boolean prevLeft;
    private static boolean prevRight;

    private CpsTracker() {
    }

    public static void poll() {
        boolean left = Mouse.isButtonDown(0);
        boolean right = Mouse.isButtonDown(1);
        long now = System.currentTimeMillis();
        if (left && !prevLeft) LEFT_CLICKS.addLast(now);
        if (right && !prevRight) RIGHT_CLICKS.addLast(now);
        prevLeft = left;
        prevRight = right;
        prune(LEFT_CLICKS, now);
        prune(RIGHT_CLICKS, now);
    }

    private static void prune(ArrayDeque<Long> queue, long now) {
        while (!queue.isEmpty() && now - queue.peekFirst() >= 1000L) {
            queue.pollFirst();
        }
    }

    public static int leftCps() {
        prune(LEFT_CLICKS, System.currentTimeMillis());
        return LEFT_CLICKS.size();
    }

    public static int rightCps() {
        prune(RIGHT_CLICKS, System.currentTimeMillis());
        return RIGHT_CLICKS.size();
    }
}
