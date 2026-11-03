package kutu.kutuclient.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.List;

/**
 * Shows the player's multiplayer latency from their player-list entry.
 * In singleplayer (no server data) it shows "--".
 */
public class PingModule extends TextModule {

    public PingModule() {
        super("ping", "Ping");
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        if (mc.getCurrentServer() == null || mc.player == null) {
            return List.of("Ping: --");
        }
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) return List.of("Ping: --");
        PlayerInfo info = connection.getPlayerInfo(mc.player.getUUID());
        if (info == null) return List.of("Ping: --");
        return List.of("Ping: " + info.getLatency() + " ms");
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 66};
    }
}
