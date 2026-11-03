package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Highlights chat messages that mention the player: any message whose
 * text contains the player's own username (always watched) or any word
 * from the configurable list gets a colored background + border drawn
 * behind its chat line(s), and can optionally play a soft sound ping
 * (default off).
 *
 * <p>Implementation note: 1.8.9's GuiNewChat keeps its drawn lines in a
 * private list, so instead of reaching into it, this module tracks the
 * wrapped line count of recent messages itself (using the same public
 * FontRenderer wrapping and the chat GUI's public width/scale), and
 * re-draws the highlight for a matched message at its computed line
 * offset while it is fresh (10 s, matching vanilla's chat fade).
 * It assumes the chat is not scrolled up; if the geometry ever
 * disagrees it degrades to a slightly misplaced box, never a crash.
 */
public class NameHighlightModule extends Module {

    private static final long HIGHLIGHT_MS = 10000L;
    private static final int MAX_TRACKED_LINES = 120;

    private static class Entry {
        final int lines;
        final boolean matched;
        final long time;

        Entry(int lines, boolean matched) {
            this.lines = lines;
            this.matched = matched;
            this.time = System.currentTimeMillis();
        }
    }

    /** Recent chat messages, oldest first. */
    private static final List<Entry> recent = new ArrayList<Entry>();
    private static long lastPingAt = 0L;

    public NameHighlightModule() {
        super("name_highlight", "Name Highlight");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("sound", "Sound ping", Arrays.asList("Off", "On")));
    }

    @Override
    public List<TextOpt> textOpts() {
        return Collections.singletonList(new TextOpt("words", "Words (comma list)", ""));
    }

    private List<String> words() {
        List<String> out = new ArrayList<String>();
        String raw = textVal(textOpts().get(0));
        if (raw != null) {
            for (String w : raw.split(",")) {
                String t = w.trim().toLowerCase();
                if (!t.isEmpty()) out.add(t);
            }
        }
        return out;
    }

    public static void onChat(ClientChatReceivedEvent event) {
        NameHighlightModule self = (NameHighlightModule) Modules.get("name_highlight");
        if (self == null || !self.enabled()) return;
        if (event.type == 2) return; // action-bar messages are not chat lines
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.ingameGUI == null) return;

        String plain = event.message.getUnformattedText();
        String lower = plain == null ? "" : plain.toLowerCase();
        boolean matched = false;
        String username = mc.getSession() != null ? mc.getSession().getUsername() : null;
        if (username != null && !username.isEmpty() && lower.contains(username.toLowerCase())) {
            matched = true;
        }
        if (!matched) {
            for (String w : self.words()) {
                if (lower.contains(w)) {
                    matched = true;
                    break;
                }
            }
        }

        // Track the wrapped line count so the highlight can be placed
        // at the right line offset while the message is on screen.
        int lines = 1;
        try {
            GuiNewChat chat = mc.ingameGUI.getChatGUI();
            int wrapWidth = MathHelper.floor_float((float) chat.getChatWidth() / chat.getChatScale());
            if (wrapWidth > 0) {
                lines = Math.max(1, mc.fontRendererObj.listFormattedStringToWidth(
                        event.message.getFormattedText(), wrapWidth).size());
            }
        } catch (Exception ignored) {
            lines = 1;
        }
        recent.add(new Entry(lines, matched));
        int total = 0;
        for (Iterator<Entry> it = recent.iterator(); it.hasNext(); ) {
            total += it.next().lines;
        }
        while (total > MAX_TRACKED_LINES && recent.size() > 1) {
            total -= recent.remove(0).lines;
        }

        if (matched && "On".equals(self.opt("sound"))) {
            long now = System.currentTimeMillis();
            if (now - lastPingAt > 500L) {
                lastPingAt = now;
                mc.getSoundHandler().playSound(
                        PositionedSoundRecord.create(new ResourceLocation("random.orb"), 1.0f));
            }
        }
    }

    /** Draws highlights for fresh matched messages. Called on the CHAT overlay post pass. */
    public static void drawHighlights(Minecraft mc, ScaledResolution res) {
        NameHighlightModule self = (NameHighlightModule) Modules.get("name_highlight");
        if (self == null || !self.enabled() || recent.isEmpty()) return;
        if (mc.ingameGUI == null || mc.thePlayer == null) return;
        GuiNewChat chat = mc.ingameGUI.getChatGUI();
        float scale = chat.getChatScale();
        int visible = chat.getLineCount();
        int k = MathHelper.ceiling_float_int((float) chat.getChatWidth() / scale);
        int x0 = 2;
        int x1 = 2 + Math.round((k + 4) * scale);
        int baseY = res.getScaledHeight() - 48 + 20; // chat origin (posX=0, posY=height-48 defaults)
        long now = System.currentTimeMillis();
        int accent = noobmodantor.gui.Ui.accent();
        int fillColor = (0x55 << 24) | (accent & 0xFFFFFF);

        int offset = 0; // lines below this entry (newer messages)
        for (int i = recent.size() - 1; i >= 0; i--) {
            Entry e = recent.get(i);
            if (e.matched && now - e.time < HIGHLIGHT_MS) {
                for (int j = offset; j < offset + e.lines && j < visible; j++) {
                    int yTop = baseY - Math.round((j + 1) * 9 * scale);
                    int yBottom = baseY - Math.round(j * 9 * scale);
                    Gui.drawRect(x0, yTop, x1, yBottom, fillColor);
                    Gui.drawRect(x0, yTop, x1, yTop + 1, accent);
                    Gui.drawRect(x0, yBottom - 1, x1, yBottom, accent);
                    Gui.drawRect(x0, yTop, x0 + 1, yBottom, accent);
                    Gui.drawRect(x1 - 1, yTop, x1, yBottom, accent);
                }
            }
            offset += e.lines;
            if (offset > visible + 20) break;
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
