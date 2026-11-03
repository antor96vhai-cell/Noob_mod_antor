package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Hitbox highlighter: draws an outline box around other players using
 * each player's real (vanilla) bounding box — F3+B style, but
 * players-only and in a configurable color.
 *
 * <p>Safety properties, by construction:
 * <ul>
 *   <li>The box is only READ from the entity and drawn; the entity's
 *   width/height/bounding box are never written, so the real hitbox,
 *   collision and everything server-side stay exactly vanilla.</li>
 *   <li>Depth testing stays ON (and the depth mask untouched), so
 *   boxes are correctly hidden behind walls — this is a highlighter,
 *   never an ESP/wallhack.</li>
 *   <li>The local player is drawn too in third person (in first person
 *   their box would surround the camera, so it is skipped then).</li>
 * </ul>
 * Ships disabled by default.
 */
public class HitboxModule extends Module {

    public HitboxModule() {
        super("hitbox", "Hitbox Highlighter");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public boolean usesColor() {
        return true;
    }

    public static void render(Minecraft mc, float partialTicks) {
        HitboxModule self = (HitboxModule) Modules.get("hitbox");
        if (self == null || !self.enabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        int rgb = self.cfg().color & 0xFFFFFF;
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;

        double viewerX = mc.getRenderManager().viewerPosX;
        double viewerY = mc.getRenderManager().viewerPosY;
        double viewerZ = mc.getRenderManager().viewerPosZ;
        boolean firstPerson = mc.gameSettings.thirdPersonView == 0;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth(); // depth test ON: no through-wall rendering
        GlStateManager.color(r, g, b, 1.0f);
        GL11.glLineWidth(2.0f);
        try {
            List<EntityPlayer> players = mc.theWorld.playerEntities;
            for (EntityPlayer player : players) {
                if (player == null || player.isDead) continue;
                if (player == mc.thePlayer && firstPerson) continue;
                double ix = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
                double iy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
                double iz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
                AxisAlignedBB world = player.getEntityBoundingBox();
                double dx = ix - player.posX;
                double dy = iy - player.posY;
                double dz = iz - player.posZ;
                AxisAlignedBB box = new AxisAlignedBB(
                        world.minX + dx - viewerX, world.minY + dy - viewerY, world.minZ + dz - viewerZ,
                        world.maxX + dx - viewerX, world.maxY + dy - viewerY, world.maxZ + dz - viewerZ);
                RenderGlobal.drawSelectionBoundingBox(box);
            }
        } finally {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableTexture2D();
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
