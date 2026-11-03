package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.util.Collections;
import java.util.List;

/** Shows the address of the multiplayer server being played on, or "Singleplayer". */
public class ServerModule extends TextModule {

    public ServerModule() {
        super("server", "Server Address");
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        String text;
        if (mc.isSingleplayer()) {
            text = "Server: Singleplayer";
        } else {
            ServerData data = mc.getCurrentServerData();
            text = "Server: " + (data != null ? data.serverIP : "Unknown");
        }
        return Collections.singletonList(text);
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 154};
    }
}
