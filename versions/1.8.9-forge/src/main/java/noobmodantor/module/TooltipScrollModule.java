package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Tooltip Scroll — makes item tooltips that are taller than the screen
 * readable by scrolling them (mouse wheel on PC, drag on touch).
 *
 * <p>Why this works where a plain text hook cannot: in this Forge build
 * (1.8.9 / 11.15.1.2318, all verified against the userdev patches and
 * the MCP jar, not guessed):
 * <ul>
 *   <li>Forge patches {@code ItemStack.getTooltip} to call
 *   {@code ForgeEventFactory.onItemTooltip(this, player, list, advanced)}
 *   just before returning the list, so {@link ItemTooltipEvent} really
 *   is posted on the client GUI path — {@code GuiContainer.renderToolTip}
 *   calls {@code getTooltip} every frame for the hovered stack. The
 *   event's list is the very list vanilla then decorates (rarity color,
 *   grey lore) and hands to the tooltip renderer.</li>
 *   <li>{@code DrawScreenEvent} is posted by
 *   {@code ForgeHooksClient.drawScreen}, which EntityRenderer calls
 *   <em>instead of</em> {@code GuiScreen.drawScreen}: Pre, then the
 *   <b>whole</b> screen draw (including the tooltip), then Post. So in
 *   Post, vanilla has already drawn everything.</li>
 *   <li>At runtime the tooltip itself is drawn by
 *   {@code GuiUtils.drawHoveringText} (Forge replaces
 *   {@code GuiScreen.drawHoveringText}). It clamps the box bottom to
 *   the screen, which pushes the top of a long tooltip off-screen —
 *   the Auction House lore cut-off this module fixes.</li>
 * </ul>
 *
 * <p>Route: when the captured tooltip (measured with vanilla's own
 * box math) would overflow the screen vertically, the module copies
 * the lines and empties the event list, so vanilla draws nothing;
 * in {@code DrawScreenEvent.Post} it then draws its own replica of
 * the vanilla box (same colors/border recipe as GuiUtils), pinned
 * fully on-screen, with the text clipped by a GL scissor to the box
 * interior and shifted by the scroll offset. Suppressing the vanilla
 * draw was chosen over painting a cover rect: the cover would have
 * to reproduce GuiUtils' exact internal geometry (which differs in
 * small ways from textbook vanilla), while an emptied list is exact.
 * Fitting tooltips are never touched.
 *
 * <p>Scrolling: wheel = 3 lines per notch; on touch (Zalith/Android
 * there is no wheel) dragging vertically with the pointer held inside
 * the tooltip box scrolls the content. The offset resets whenever a
 * different stack or screen is hovered. Everything runs under
 * ClientEvents' crash-guard: any failure disables this module for the
 * session instead of crashing the game.
 */
public class TooltipScrollModule extends Module {

    // Vanilla/GuiUtils tooltip colors (0xF0100010 bg, 0x505000FF border).
    private static final int BG_COLOR = -267386864;
    private static final int BORDER_START = 1347420415;
    private static final int BORDER_END = (BORDER_START & 16711422) >> 1 | BORDER_START & -16777216;

    /** Frame counter, incremented on every DrawScreenEvent.Pre. */
    private static int frame = 0;

    // Capture state (written during the screen draw, read in Post).
    private static int capturedFrame = -1;
    private static ItemStack capturedStack = null;
    private static GuiScreen capturedScreen = null;
    private static List<String> capturedLines = null;

    // Scroll state (persists across frames while the same stack is hovered).
    private static ItemStack scrollStack = null;
    private static GuiScreen scrollScreen = null;
    private static int scroll = 0;
    private static boolean dragWasDown = false;

    public TooltipScrollModule() {
        super("tooltip_scroll", "Tooltip Scroll");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public boolean supportsBackground() {
        return false;
    }

    /** Called from the DrawScreenEvent.Pre handler: starts a new frame. */
    public static void frameStart() {
        frame++;
    }

    // ------------------------------------------------------------------
    // Capture
    // ------------------------------------------------------------------

    public static void onTooltip(ItemTooltipEvent event) {
        TooltipScrollModule self = (TooltipScrollModule) Modules.get("tooltip_scroll");
        if (self == null || !self.enabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen == null || !(mc.currentScreen instanceof GuiContainer)) return;
        if (event.itemStack == null || event.toolTip == null || event.toolTip.isEmpty()) return;

        FontRenderer font = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);
        int screenH = sr.getScaledHeight();
        // The exact mouse coords this frame's drawScreen is using
        // (EntityRenderer derives them from Mouse.getX/getY this way).
        int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;

        int lines = event.toolTip.size();
        int boxH = boxHeight(lines);
        if (!overflows(boxH, mouseY, screenH)) return; // vanilla handles it fine

        capturedLines = new ArrayList<String>(event.toolTip);
        capturedStack = event.itemStack;
        capturedScreen = mc.currentScreen;
        capturedFrame = frame;
        // Suppress vanilla's unscrollable draw; Post draws the replica.
        event.toolTip.clear();
    }

    /** Vanilla tooltip box height for n lines (the "l" of drawHoveringText). */
    private static int boxHeight(int lineCount) {
        int h = 8;
        if (lineCount > 1) h += 2 + (lineCount - 1) * 10;
        return h;
    }

    /** True if vanilla's placement (mouseY-12, bottom-clamped) pushes the box top off-screen. */
    private static boolean overflows(int boxH, int mouseY, int screenH) {
        int textY = mouseY - 12;
        if (textY + boxH + 6 > screenH) textY = screenH - boxH - 6;
        return textY < 0;
    }

    // ------------------------------------------------------------------
    // Render (DrawScreenEvent.Post — vanilla has already drawn)
    // ------------------------------------------------------------------

    public static void onDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        TooltipScrollModule self = (TooltipScrollModule) Modules.get("tooltip_scroll");
        if (self == null || !self.enabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!(event.gui instanceof GuiContainer) || capturedLines == null
                || capturedFrame != frame || event.gui != capturedScreen) {
            resetScroll();
            return;
        }
        if (capturedStack != scrollStack || capturedScreen != scrollScreen) {
            scroll = 0;
            scrollStack = capturedStack;
            scrollScreen = capturedScreen;
            dragWasDown = false;
        }

        FontRenderer font = mc.fontRendererObj;
        int screenW = event.gui.width;
        int screenH = event.gui.height;
        int mouseX = event.mouseX;
        int mouseY = event.mouseY;

        int textW = 0;
        for (String s : capturedLines) textW = Math.max(textW, font.getStringWidth(s));
        int totalH = boxHeight(capturedLines.size());
        int visH = Math.min(totalH, screenH - 8);
        if (visH < 24) { // absurdly small window; leave it alone
            resetScroll();
            return;
        }
        int maxScroll = totalH - visH;

        // Box placement: vanilla x rule; y pinned fully on-screen.
        int textX = mouseX + 12;
        if (textX + textW > screenW) textX -= 28 + textW;
        if (textX < 4) textX = 4;
        int vanillaTextY = mouseY - 12;
        if (vanillaTextY + totalH + 6 > screenH) vanillaTextY = screenH - totalH - 6;
        int outerH = visH + 8;
        int boxTop = Math.max(0, Math.min(vanillaTextY - 4, screenH - outerH));
        int boxBottom = boxTop + outerH;
        int textY = boxTop + 4; // first line's top when scroll == 0, in box terms

        // ---- scroll input (only while this tooltip is up) ----
        int dwheel = Mouse.getDWheel();
        if (dwheel != 0) {
            int notches = dwheel / 120;
            if (notches == 0) notches = dwheel > 0 ? 1 : -1;
            scroll -= notches * 30; // 3 lines per notch
        }
        boolean down = Mouse.isButtonDown(0);
        boolean inside = mouseX >= textX - 4 && mouseX <= textX + textW + 4
                && mouseY >= boxTop && mouseY <= boxBottom;
        if (down && inside) {
            if (dragWasDown) {
                // LWJGL Y is bottom-up: getDY() > 0 = pointer moved up =
                // content should move up = reveal lower lines = scroll grows.
                scroll += Mouse.getDY();
            } else {
                Mouse.getDY(); // press start: discard stale accumulated delta
            }
        }
        dragWasDown = down;
        if (scroll < 0) scroll = 0;
        if (scroll > maxScroll) scroll = maxScroll;

        drawTooltip(mc, font, textX, textY, textW, visH, boxTop, boxBottom, screenH);
    }

    private static void drawTooltip(Minecraft mc, FontRenderer font, int textX, int textY,
                                    int textW, int visH, int boxTop, int boxBottom, int screenH) {
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // The GuiUtils background/border recipe, for a box whose text
        // block starts at textY and whose visible height is visH.
        int x = textX, y = textY, w = textW, h = visH;
        GuiUtils.drawGradientRect(x - 3, y - 4, x + w + 3, y - 3, BG_COLOR, BG_COLOR, 300);
        GuiUtils.drawGradientRect(x - 3, y + h + 3, x + w + 3, y + h + 4, BG_COLOR, BG_COLOR, 300);
        GuiUtils.drawGradientRect(x - 3, y - 3, x + w + 3, y + h + 3, BG_COLOR, BG_COLOR, 300);
        GuiUtils.drawGradientRect(x - 4, y - 3, x - 3, y + h + 3, BG_COLOR, BG_COLOR, 300);
        GuiUtils.drawGradientRect(x + w + 3, y - 3, x + w + 4, y + h + 3, BG_COLOR, BG_COLOR, 300);
        GuiUtils.drawGradientRect(x - 3, y - 3 + 1, x - 4 + 1, y + h + 3 - 1, BORDER_START, BORDER_END, 300);
        GuiUtils.drawGradientRect(x + w + 2, y - 3 + 1, x + w + 3, y + h + 3 - 1, BORDER_END, BORDER_START, 300);

        // Text, clipped to the box interior and shifted by the offset.
        ScaledResolution sr = new ScaledResolution(mc);
        int sf = sr.getScaleFactor();
        int clipTop = boxTop + 2;
        int clipBottom = boxBottom - 2;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((x - 2) * sf, (screenH - clipBottom) * sf, (w + 4) * sf, (clipBottom - clipTop) * sf);
        for (int i = 0; i < capturedLines.size(); i++) {
            int lineY = textY + i * 10 + (i > 0 ? 2 : 0) - scroll;
            if (lineY + 9 < clipTop || lineY > clipBottom) continue;
            font.drawStringWithShadow(capturedLines.get(i), x, lineY, -1);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Scroll arrows (drawn as GL triangles: no font-glyph dependency).
        int cx = x + w - 5;
        if (scroll > 0) drawTriangle(cx, boxTop + 2, true);
        int totalH = boxHeight(capturedLines.size());
        if (scroll < totalH - visH) drawTriangle(cx, boxBottom - 7, false);

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawTriangle(int cx, int top, boolean up) {
        GlStateManager.disableTexture2D();
        GlStateManager.color(0.85F, 0.85F, 0.85F, 1.0F);
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
        if (up) {
            wr.pos(cx, top, 0).endVertex();
            wr.pos(cx - 4, top + 5, 0).endVertex();
            wr.pos(cx + 4, top + 5, 0).endVertex();
        } else {
            wr.pos(cx - 4, top, 0).endVertex();
            wr.pos(cx + 4, top, 0).endVertex();
            wr.pos(cx, top + 5, 0).endVertex();
        }
        tess.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void resetScroll() {
        scroll = 0;
        scrollStack = null;
        scrollScreen = null;
        dragWasDown = false;
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
