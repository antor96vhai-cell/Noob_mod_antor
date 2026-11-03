package nooblaunchar.hud;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import nooblaunchar.NoobLaunchar;
import nooblaunchar.gui.NoobMenuScreen;
import nooblaunchar.module.FullbrightModule;
import nooblaunchar.module.Module;
import nooblaunchar.module.Modules;
import nooblaunchar.module.ToggleSprintModule;
import nooblaunchar.module.ZoomModule;
import nooblaunchar.NoobConfig;

/**
 * Forge event handlers: client tick (menu key, zoom, fullbright, toggle
 * sprint) and the post-overlay render pass that draws every enabled HUD
 * module at its configured position.
 */
public class ClientEvents {
    private boolean cleanedUp = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!cleanedUp) {
            cleanedUp = true;
            ZoomModule.cleanupOnStart(mc);
            FullbrightModule.cleanupOnStart(mc);
            NoobConfig.save();
        }
        while (NoobLaunchar.menuKey.isPressed()) {
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new NoobMenuScreen());
            }
        }
        ZoomModule.keyHeld = NoobLaunchar.zoomKey.isKeyDown();
        ZoomModule.tick(mc);
        FullbrightModule.tick(mc);
        ToggleSprintModule.tick(mc);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        CpsTracker.poll();
        for (Module module : Modules.all()) {
            if (!module.enabled() || !module.hasHud()) continue;
            int[] pos = module.pos(mc);
            module.render(mc, pos[0], pos[1]);
        }
    }
}
