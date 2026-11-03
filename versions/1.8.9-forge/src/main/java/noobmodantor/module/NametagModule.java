package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;

import java.util.Collections;
import java.util.List;

/**
 * Nametags: shows the local player's OWN nametag in third person / F5
 * (vanilla never draws it), with a scale slider, the module text color
 * (chroma included) and a background that follows the appearance
 * system. The same scale is applied to other players' vanilla
 * nametags by scaling the matrix during their nameplate pass
 * (RenderLivingEvent.Specials pre/post, about the entity's rendered
 * position — the same push/pop discipline as Entity Size).
 *
 * <p>The own nametag is drawn in the RenderWorldLast pass with
 * billboarding taken from the render manager's public view angles,
 * mirroring vanilla's nameplate transform.
 */
public class NametagModule extends Module {

    private static EntityPlayer scaledFor = null;

    public NametagModule() {
        super("nametag", "Nametag");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public boolean usesColor() {
        return true;
    }

    @Override
    public boolean usesBackground() {
        return true;
    }

    @Override
    public boolean supportsBackground() {
        return true;
    }

    @Override
    public List<SliderOpt> sliders() {
        return Collections.singletonList(new SliderOpt("scale", "Nametag scale", 0.5, 2.0, 0.1, 1.0));
    }

    public double scale() {
        return sliderVal(sliders().get(0));
    }

    // ---- other players: scale vanilla nameplates -------------------

    public static void applyScalePre(RenderLivingEvent.Specials.Pre event) {
        NametagModule self = (NametagModule) Modules.get("nametag");
        if (self == null || !self.enabled()) return;
        if (!(event.entity instanceof EntityPlayer)) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (event.entity == mc.thePlayer) return; // we draw our own plate
        double s = self.scale();
        if (s == 1.0) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(event.x, event.y, event.z);
        GlStateManager.scale(s, s, s);
        GlStateManager.translate(-event.x, -event.y, -event.z);
        scaledFor = (EntityPlayer) event.entity;
    }

    public static void applyScalePost(RenderLivingEvent.Specials.Post event) {
        if (scaledFor != null && scaledFor == event.entity) {
            GlStateManager.popMatrix();
            scaledFor = null;
        }
    }

    // ---- own nametag ------------------------------------------------

    public static void renderOwn(Minecraft mc, float partialTicks) {
        NametagModule self = (NametagModule) Modules.get("nametag");
        if (self == null || !self.enabled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.gameSettings.thirdPersonView == 0) return;
        EntityPlayer player = mc.thePlayer;
        if (player.isDead || player.isInvisible()) return;

        double ix = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double iy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double iz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        double x = ix - mc.getRenderManager().viewerPosX;
        double y = iy - mc.getRenderManager().viewerPosY + player.height + 0.5;
        double z = iz - mc.getRenderManager().viewerPosZ;

        String name = player.getName();
        FontRenderer font = mc.fontRendererObj;
        int textW = font.getStringWidth(name);
        double s = self.scale();
        float f1 = 0.016666668f * 1.6f * (float) s;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX
                * (mc.gameSettings.thirdPersonView == 2 ? -1 : 1), 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-f1, -f1, f1);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        try {
            // Background per the appearance system (Clear = none).
            String mode = self.bgMode();
            if (!"Clear".equals(mode)) {
                int alpha = (int) Math.round(self.bgOpacityPct() / 100.0 * 255.0);
                if (alpha > 0) {
                    int rgb = "Custom".equals(mode) ? self.bgColorRgb() : 0x101418;
                    fill(-textW / 2 - 2, -2, textW + 4, 12, (alpha << 24) | rgb);
                }
            }
            font.drawString(name, -textW / 2, 0, self.textColor());
        } finally {
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
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
