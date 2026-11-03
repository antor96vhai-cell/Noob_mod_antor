package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Chat Tweaks (Chatting/ChatTweaks equivalent):
 * <ul>
 *   <li><b>Compact repeats:</b> when the same chat message arrives
 *   again back-to-back, the duplicate is not printed as a new line.
 *   Instead a single counter line ("message §7×N") is maintained via
 *   GuiNewChat's own replaceable-message API
 *   (printChatMessageWithOptionalDeletion with a private id range),
 *   and it updates in place as more copies arrive. The first
 *   occurrence prints normally; a different message resets the
 *   counter.</li>
 *   <li><b>Copy last chat line:</b> the Copy Chat key (default F7,
 *   rebindable in Controls) copies the most recent chat message's
 *   plain text to the system clipboard and confirms in chat.</li>
 * </ul>
 * Chat tabs/filtering are intentionally not implemented: they would
 * require replacing the whole chat GUI (see BUILD_NOTES.md).
 */
public class ChatTweaksModule extends Module {

    /** Our replaceable-message ids live far away from vanilla's (vanilla uses 0 and small values). */
    private static final int ID_BASE = 0x4E090000;
    private static int nextId = ID_BASE;
    private static int currentId = -1;
    private static String lastFormatted = null;
    private static int repeatCount = 0;
    private static long lastRepeatAt = 0L;
    private static String lastPlainText = null;

    public ChatTweaksModule() {
        super("chat_tweaks", "Chat Tweaks");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("compact", "Compact repeats",
                Arrays.asList("On", "Off")));
    }

    public static void onChat(ClientChatReceivedEvent event) {
        ChatTweaksModule self = (ChatTweaksModule) Modules.get("chat_tweaks");
        if (self == null || !self.enabled()) return;
        if (event.type == 2) return; // action bar is not chat
        Minecraft mc = Minecraft.getMinecraft();
        String plain = event.message.getUnformattedText();
        if (plain != null) lastPlainText = plain;
        if (!"On".equals(self.opt("compact"))) return;
        if (mc.ingameGUI == null) return;

        String formatted = event.message.getFormattedText();
        long now = System.currentTimeMillis();
        if (formatted != null && formatted.equals(lastFormatted) && now - lastRepeatAt < 120000L) {
            repeatCount++;
            lastRepeatAt = now;
            event.setCanceled(true);
            mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(
                    new ChatComponentText(formatted + " \u00A77\u00D7" + repeatCount), currentId);
        } else {
            lastFormatted = formatted;
            repeatCount = 1;
            lastRepeatAt = now;
            currentId = nextId++;
            if (nextId > ID_BASE + 100000) nextId = ID_BASE; // ids are only compared for equality
        }
    }

    public static void tick(Minecraft mc) {
        ChatTweaksModule self = (ChatTweaksModule) Modules.get("chat_tweaks");
        if (self == null || !self.enabled()) return;
        if (noobmodantor.NoobModAntor.copyChatKey == null) return;
        while (noobmodantor.NoobModAntor.copyChatKey.isPressed()) {
            if (lastPlainText != null && mc.thePlayer != null) {
                GuiScreen.setClipboardString(lastPlainText);
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        "\u00A78[Noob] \u00A77Chat line copied to clipboard"));
            }
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
