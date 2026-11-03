package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import noobmodantor.NoobConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Smart Culling: skips rendering work that cannot be seen anyway —
 * the safe, real parts of "render only what matters":
 * <ul>
 *   <li><b>Entity distance:</b> living entities farther than the
 *   entity slider are not rendered (same RenderLivingEvent
 *   cancellation as the Performance page control; when this module is
 *   enabled its slider wins, when disabled the Performance page value
 *   applies — see {@link #effectiveEntityLimit()}).</li>
 *   <li><b>Tile-entity distance:</b> the special renderers of the
 *   common block entities (chests, signs, banners, skulls, beacons,
 *   spawners, brewing stands, pistons, flower pots, end portals,
 *   enchanting tables, ender chests) are wrapped — via the public
 *   dispatcher lookup + FML's bind API — with a delegating renderer
 *   that skips its draw when the block is farther than the tile
 *   slider. The block itself still renders normally; only the
 *   special-rendered content (lid, book, beam, text, ...) is skipped.</li>
 * </ul>
 *
 * <p>What this deliberately does NOT do: any chunk/block-level
 * culling. Vanilla 1.8.9 already performs frustum culling, chunk
 * occlusion culling and per-face culling of hidden block faces;
 * replacing that graph would need a coremod, would fight OptiFine,
 * and risks crashes (see BUILD_NOTES.md).
 */
public class SmartCullingModule extends Module {

    /** Delegating renderer: distance-checks, then forwards to the original renderer untouched. */
    public static class CullingRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T> {
        private final TileEntitySpecialRenderer<T> delegate;

        public CullingRenderer(TileEntitySpecialRenderer<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void renderTileEntityAt(T te, double x, double y, double z, float partialTicks, int destroyStage) {
            if (shouldCull(te)) return;
            delegate.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);
        }
    }

    private static boolean wrapped = false;

    public SmartCullingModule() {
        super("smart_culling", "Smart Culling");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<SliderOpt> sliders() {
        return Arrays.asList(
                new SliderOpt("entity_distance", "Entity distance", 0, 256, 16, 64),
                new SliderOpt("tile_distance", "Tile-entity distance", 0, 192, 16, 48));
    }

    /** Entity render limit in blocks (0 = unlimited), honoring module on/off and the Performance page fallback. */
    public static int effectiveEntityLimit() {
        SmartCullingModule self = (SmartCullingModule) Modules.get("smart_culling");
        if (self != null && self.enabled()) {
            return (int) self.sliderVal(self.sliders().get(0));
        }
        return NoobConfig.get().entityRenderDistance;
    }

    public static boolean shouldCull(TileEntity te) {
        SmartCullingModule self = (SmartCullingModule) Modules.get("smart_culling");
        if (self == null || !self.enabled() || te == null || te.getPos() == null) return false;
        int limit = (int) self.sliderVal(self.sliders().get(1));
        if (limit <= 0) return false;
        Minecraft mc = Minecraft.getMinecraft();
        Entity view = mc.getRenderViewEntity() != null ? mc.getRenderViewEntity() : mc.thePlayer;
        if (view == null) return false;
        double dx = te.getPos().getX() + 0.5 - view.posX;
        double dy = te.getPos().getY() + 0.5 - view.posY;
        double dz = te.getPos().getZ() + 0.5 - view.posZ;
        return dx * dx + dy * dy + dz * dz > (double) limit * limit;
    }

    /** One-time lazy install of the tile-entity wrappers (first tick in a world, when the dispatcher is fully populated). */
    public static void tick(Minecraft mc) {
        if (wrapped || mc.theWorld == null) return;
        SmartCullingModule self = (SmartCullingModule) Modules.get("smart_culling");
        if (self == null) return;
        wrap(TileEntityChest.class);
        wrap(TileEntityEnderChest.class);
        wrap(TileEntitySign.class);
        wrap(TileEntityBanner.class);
        wrap(TileEntitySkull.class);
        wrap(TileEntityBeacon.class);
        wrap(TileEntityMobSpawner.class);
        wrap(TileEntityBrewingStand.class);
        wrap(TileEntityPiston.class);
        wrap(TileEntityFlowerPot.class);
        wrap(TileEntityEndPortal.class);
        wrap(TileEntityEnchantmentTable.class);
        wrapped = true;
    }

    private static <T extends TileEntity> void wrap(Class<T> clazz) {
        try {
            TileEntitySpecialRenderer<T> current =
                    TileEntityRendererDispatcher.instance.getSpecialRendererByClass(clazz);
            if (current != null && !(current instanceof CullingRenderer)) {
                ClientRegistry.bindTileEntitySpecialRenderer(clazz, new CullingRenderer<T>(current));
            }
        } catch (Throwable ignored) {
            // A renderer we cannot wrap simply stays vanilla.
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
