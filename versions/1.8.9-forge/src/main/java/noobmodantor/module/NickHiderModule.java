package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.Collections;
import java.util.List;

/**
 * Replaces the player's own username with a chosen nickname in chat, on
 * this client only — other players still see the real name. The swap is
 * done on the message's formatted text (so colors/formatting survive as
 * section-sign codes) whenever a received chat message contains the
 * username. Ships disabled by default: it is a privacy opt-in.
 */
public class NickHiderModule extends Module {

    public NickHiderModule() {
        super("nick_hider", "Nick Hider");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<TextOpt> textOpts() {
        return Collections.singletonList(new TextOpt("nick", "Nickname", "Player"));
    }

    public static void onChat(ClientChatReceivedEvent event) {
        NickHiderModule self = (NickHiderModule) Modules.get("nick_hider");
        if (self == null || !self.enabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getSession() == null) return;
        String username = mc.getSession().getUsername();
        String nick = self.textVal(self.textOpts().get(0));
        if (username == null || username.isEmpty() || nick.isEmpty() || username.equals(nick)) return;
        String formatted = event.message.getFormattedText();
        if (formatted != null && formatted.contains(username)) {
            event.message = new ChatComponentText(formatted.replace(username, nick));
        }
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
