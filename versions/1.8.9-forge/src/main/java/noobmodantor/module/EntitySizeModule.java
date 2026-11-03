package noobmodantor.module;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Visually scales other players and mobs at render time only, via a GL
 * scale wrapped around the entity's own render call
 * (RenderLivingEvent.Pre/Post). This is PURELY cosmetic:
 * <ul>
 *   <li>It never touches the entity's width/height, bounding box,
 *   collision, eye height or any field the server can see — the real
 *   hitbox stays exactly vanilla-sized; only the drawn model changes.</li>
 *   <li>The scale is applied about the entity's rendered position, so
 *   the model grows/shrinks in place.</li>
 *   <li>The local player is scaled too (visible in third person / F5),
 *   using the player scale.</li>
 * </ul>
 * Ships disabled by default.
 */
public class EntitySizeModule extends Module {

    /** Entity whose matrix we pushed in Pre, so Post pops exactly ours. */
    private static EntityLivingBase pushedFor = null;

    public EntitySizeModule() {
        super("entity_size", "Entity Size");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<SliderOpt> sliders() {
        return Arrays.asList(
                new SliderOpt("player_scale", "Player size", 0.5, 2.0, 0.05, 1.0),
                new SliderOpt("mob_scale", "Mob size", 0.5, 2.0, 0.05, 1.0));
    }

    public double scaleFor(EntityLivingBase entity) {
        SliderOpt s = sliders().get(entity instanceof EntityPlayer ? 0 : 1);
        return sliderVal(s);
    }

    public static void applyPre(RenderLivingEvent.Pre event) {
        EntitySizeModule self = (EntitySizeModule) Modules.get("entity_size");
        if (self == null || !self.enabled()) return;
        double s = self.scaleFor(event.entity);
        if (s == 1.0) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(event.x, event.y, event.z);
        GlStateManager.scale(s, s, s);
        GlStateManager.translate(-event.x, -event.y, -event.z);
        pushedFor = event.entity;
    }

    public static void applyPost(RenderLivingEvent.Post event) {
        if (pushedFor == event.entity) {
            GlStateManager.popMatrix();
            pushedFor = null;
        }
    }

    // Non-HUD module.
    @Override
    public int[] defaultPos(net.minecraft.client.Minecraft mc) {
        return new int[]{0, 0};
    }

    @Override
    public int width(net.minecraft.client.Minecraft mc) {
        return 0;
    }

    @Override
    public int height(net.minecraft.client.Minecraft mc) {
        return 0;
    }

    @Override
    public void render(net.minecraft.client.Minecraft mc, int x, int y) {
    }
}
