package nooblaunchar.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Collections;
import java.util.List;

/**
 * Shows the player's multiplayer latency from their player-list entry
 * (NetworkPlayerInfo.getResponseTime). In singleplayer it shows "--".
 */
public class PingModule extends TextModule {

    public PingModule() {
        super("ping", "Ping");
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        if (mc.getCurrentServerData() == null || mc.thePlayer == null || mc.getNetHandler() == null) {
            return Collections.singletonList("Ping: --");
        }
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        if (info == null) return Collections.singletonList("Ping: --");
        return Collections.singletonList("Ping: " + info.getResponseTime() + " ms");
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 66};
    }
}
