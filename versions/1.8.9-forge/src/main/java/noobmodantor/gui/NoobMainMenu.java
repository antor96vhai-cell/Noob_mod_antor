package noobmodantor.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import noobmodantor.NoobModAntor;

import java.util.Random;

/**
 * Branded main menu (the game's home page) — replaces the vanilla
 * GuiMainMenu via a GuiOpenEvent hook in ClientEvents. Fully custom 2D
 * drawing: an animated dark gradient backdrop with a slow hue drift
 * toward the live accent color, ~40 softly floating dots and a vignette,
 * the big NOOB_MOD_ANTOR title with a rotating vanilla-style splash, and
 * NoobButton rows for Singleplayer / Multiplayer, three quick-join
 * servers (Fakepixel, AgeraPvP, AyorMC), the module menu, Options and
 * Quit. No panorama, no dirt background, no textures, no shaders — plain
 * rects and alphas only, so it stays smooth on weak iGPUs and at phone
 * GUI scales (everything is laid out from width/height in initGui).
 *
 * Realms is intentionally not offered (it needs the premium-account
 * Realms flow); the vanilla language button is skipped too — language
 * lives in Options.
 */
public class NoobMainMenu extends GuiScreen {
    private static final String TITLE = "NOOB_MOD_ANTOR";
    private static final String TAGLINE = "Antor's PvP Client";
    private static final String[] SPLASHES = {
            "100% Noob Power!",
            "Fakepixel Ready!",
            "FPS Boost ON!",
            "Made by Antor!",
            "Also try AgeraPvP!",
            "Right Shift = Mods!"
    };

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;
    private static final int GAP = 4;

    private static final int ID_SINGLE = 0;
    private static final int ID_MULTI = 1;
    private static final int ID_FAKEPIXEL = 2;
    private static final int ID_AGERA = 3;
    private static final int ID_AYOR = 4;
    private static final int ID_MODS = 5;
    private static final int ID_OPTIONS = 6;
    private static final int ID_QUIT = 7;

    private static final String[] QUICK_NAMES = {"Fakepixel", "AgeraPvP", "AyorMC"};
    private static final String[] QUICK_IPS = {"mc.fakepixel.me", "agerapvp.club", "mc.ayormc.net"};

    // Floating background dots: positions are screen fractions and speeds
    // are fractions/second, so the field is resolution- and resize-safe.
    private static final int PARTICLES = 40;
    private final float[] pX = new float[PARTICLES];
    private final float[] pY = new float[PARTICLES];
    private final float[] pSpeed = new float[PARTICLES];
    private final float[] pPhase = new float[PARTICLES];
    private final int[] pSize = new int[PARTICLES];

    public NoobMainMenu() {
        Random rng = new Random(987654321L);
        for (int i = 0; i < PARTICLES; i++) {
            pX[i] = rng.nextFloat();
            pY[i] = rng.nextFloat();
            pSpeed[i] = 0.004F + rng.nextFloat() * 0.010F;
            pPhase[i] = rng.nextFloat() * 6.2832F;
            pSize[i] = 1 + rng.nextInt(2);
        }
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int cx = this.width / 2;
        int top = buttonsTop();

        this.buttonList.add(new NoobButton(ID_SINGLE, cx - BTN_W / 2, top, BTN_W, BTN_H, "Singleplayer"));
        this.buttonList.add(new NoobButton(ID_MULTI, cx - BTN_W / 2, top + (BTN_H + GAP), BTN_W, BTN_H, "Multiplayer"));

        // Quick-join row: three small buttons under Multiplayer.
        int rowY = top + 2 * (BTN_H + GAP);
        int smallW = (BTN_W - 2 * GAP) / 3;
        for (int i = 0; i < 3; i++) {
            int id = ID_FAKEPIXEL + i;
            this.buttonList.add(new NoobButton(id, cx - BTN_W / 2 + i * (smallW + GAP), rowY,
                    smallW, BTN_H, QUICK_NAMES[i]));
        }

        int halfY = top + 3 * (BTN_H + GAP);
        int halfW = (BTN_W - GAP) / 2;
        this.buttonList.add(new NoobButton(ID_MODS, cx - BTN_W / 2, halfY, halfW, BTN_H, "Noob Mod Menu"));
        this.buttonList.add(new NoobButton(ID_OPTIONS, cx - BTN_W / 2 + halfW + GAP, halfY, halfW, BTN_H, "Options"));

        this.buttonList.add(new NoobButton(ID_QUIT, cx - BTN_W / 2, top + 4 * (BTN_H + GAP), BTN_W, BTN_H, "Quit Game"));
    }

    /** Y of the title's top edge. */
    private int titleY() {
        return Math.max(18, (int) (this.height * 0.14F));
    }

    /** Title scale, capped so the title always fits the screen width. */
    private float titleScale() {
        int tw = this.fontRendererObj.getStringWidth(TITLE);
        return Math.min(3.0F, (this.width - 32) / (float) Math.max(1, tw));
    }

    /** First button row Y: under the title block, but the whole 5-row
     * block is clamped to stay above the footer even on short screens. */
    private int buttonsTop() {
        int blockH = 5 * BTN_H + 4 * GAP;
        int under = titleY() + (int) (9 * titleScale()) + 30;
        int maxTop = this.height - 22 - blockH;
        return Math.min(Math.max(under, 8), Math.max(maxTop, 8));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case ID_SINGLE:
                this.mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case ID_MULTI:
                this.mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case ID_FAKEPIXEL:
            case ID_AGERA:
            case ID_AYOR: {
                int i = button.id - ID_FAKEPIXEL;
                ServerData data = new ServerData(QUICK_NAMES[i], QUICK_IPS[i], false);
                this.mc.displayGuiScreen(new GuiConnecting(this, this.mc, data));
                break;
            }
            case ID_MODS:
                this.mc.displayGuiScreen(new NoobMenuScreen());
                break;
            case ID_OPTIONS:
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case ID_QUIT:
                this.mc.shutdown();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        long now = System.currentTimeMillis();
        drawBackdrop(now);
        drawTitle(now);
        super.drawScreen(mouseX, mouseY, partialTicks); // the buttons
        drawFooter();
    }

    private void drawBackdrop(long now) {
        // Vertical gradient: fixed dark top, bottom slowly drifting
        // toward the live accent color and back (~63 s period).
        float drift = 0.5F + 0.5F * (float) Math.sin(now * 0.0001D);
        int acc = Ui.accent();
        float mixF = 0.10F + 0.10F * drift;
        int r = mix(0x0A, (acc >> 16) & 0xFF, mixF);
        int g = mix(0x08, (acc >> 8) & 0xFF, mixF);
        int b = mix(0x14, acc & 0xFF, mixF);
        int bottom = 0xFF000000 | (r << 16) | (g << 8) | b;
        drawGradientRect(0, 0, this.width, this.height, 0xFF0B0F16, bottom);

        // Floating dots rising slowly, twinkling in the accent color.
        int rgb = acc & 0x00FFFFFF;
        for (int i = 0; i < PARTICLES; i++) {
            float yFrac = (pY[i] - (now / 1000.0F) * pSpeed[i]) % 1.0F;
            if (yFrac < 0) yFrac += 1.0F;
            float xFrac = pX[i] + (float) Math.sin(now * 0.0004D + pPhase[i]) * 0.006F;
            int alpha = 36 + (int) (34 * (0.5D + 0.5D * Math.sin(now * 0.002D + pPhase[i] * 3.0D)));
            Ui.fill((int) (xFrac * this.width), (int) (yFrac * this.height), pSize[i], pSize[i],
                    (alpha << 24) | rgb);
        }

        // Vignette: darken the top and bottom edges.
        drawGradientRect(0, 0, this.width, 48, 0x66000000, 0x00000000);
        drawGradientRect(0, this.height - 64, this.width, this.height, 0x00000000, 0x99000000);
    }

    private void drawTitle(long now) {
        float scale = titleScale();
        int ty = titleY();

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2, ty, 0);
        GlStateManager.scale(scale, scale, scale);
        drawCenteredString(this.fontRendererObj, TITLE, 0, 0, Ui.accent());
        GlStateManager.popMatrix();

        int titleW = (int) (this.fontRendererObj.getStringWidth(TITLE) * scale);
        int below = ty + (int) (9 * scale);
        Ui.centeredText(this.fontRendererObj, TAGLINE, this.width / 2, below + 4, Ui.TEXT_DIM);

        // Vanilla-style splash: yellow, tilted, pulsing, anchored just
        // past the title's right edge; the line cycles every ~3.5 s.
        String splash = SPLASHES[(int) ((now / 3500L) % SPLASHES.length)];
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2 + titleW / 2 + 8, ty + 7, 0);
        GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
        float f = 1.8F - Math.abs((float) Math.sin((now % 1000L) / 1000.0F * Math.PI * 2.0D) * 0.1F);
        f = f * 100.0F / (float) (this.fontRendererObj.getStringWidth(splash) + 32);
        GlStateManager.scale(f, f, f);
        drawCenteredString(this.fontRendererObj, splash, 0, -8, 0xFFFFFF00);
        GlStateManager.popMatrix();
    }

    private void drawFooter() {
        int fh = 16;
        int fy = this.height - fh;
        Ui.fill(0, fy, this.width, fh, 0xCC0C1015);
        Ui.fill(0, fy, this.width, 1, Ui.accent());
        this.fontRendererObj.drawString(
                "Noob_mod_antor v" + NoobModAntor.VERSION + " \u2022 39 modules",
                4, fy + 4, Ui.TEXT_DIM);
        String user = "Player";
        try {
            if (this.mc.getSession() != null && this.mc.getSession().getUsername() != null) {
                user = this.mc.getSession().getUsername();
            }
        } catch (Throwable ignored) {
            // Footer must never break the menu; keep the fallback name.
        }
        String right = "Player: " + user;
        this.fontRendererObj.drawString(right,
                this.width - this.fontRendererObj.getStringWidth(right) - 4, fy + 4, Ui.TEXT_DIM);
    }

    private static int mix(int base, int target, float f) {
        return (int) (base + (target - base) * f);
    }
}
