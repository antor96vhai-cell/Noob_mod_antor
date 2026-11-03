package noobmodantor.module;

import net.minecraft.client.Minecraft;

import java.util.Collections;
import java.util.List;

/**
 * Session timer: counts up from the first moment the player is in a world
 * after launching the client, formatted as H:MM:SS (or M:SS under an hour).
 */
public class PlaytimeModule extends TextModule {
    private static long sessionStart = -1L;

    public PlaytimeModule() {
        super("playtime", "Playtime");
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        if (sessionStart < 0 && mc.thePlayer != null) {
            sessionStart = System.currentTimeMillis();
        }
        long elapsed = sessionStart < 0 ? 0L : (System.currentTimeMillis() - sessionStart) / 1000L;
        long hours = elapsed / 3600L;
        long minutes = (elapsed % 3600L) / 60L;
        long seconds = elapsed % 60L;
        String text = hours > 0
                ? String.format("Playtime: %d:%02d:%02d", hours, minutes, seconds)
                : String.format("Playtime: %d:%02d", minutes, seconds);
        return Collections.singletonList(text);
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 126};
    }
}
