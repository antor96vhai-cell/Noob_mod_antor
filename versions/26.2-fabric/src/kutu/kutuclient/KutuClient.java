package kutu.kutuclient;

import com.mojang.blaze3d.platform.InputConstants;
import kutu.kutuclient.gui.KutuMenuScreen;
import kutu.kutuclient.hud.HudRenderer;
import kutu.kutuclient.module.FullbrightModule;
import kutu.kutuclient.module.Modules;
import kutu.kutuclient.module.ToggleSprintModule;
import kutu.kutuclient.module.ZoomModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Noob_launchar — a lightweight, original in-game HUD/menu client mod.
 * Pure client-side HUD and menu features; no authentication or account
 * code of any kind.
 *
 * <ul>
 *   <li>Right Shift opens the module menu; C (hold) zooms.</li>
 *   <li>All settings persist in config/kutuclient.json.</li>
 * </ul>
 */
public class KutuClient implements ClientModInitializer {
    private static KeyMapping menuKey;
    private static KeyMapping zoomKey;
    private static boolean cleanedUp = false;

    @Override
    public void onInitializeClient() {
        KutuConfig.load();
        Modules.init();

        menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.kutuclient.menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.MISC));
        zoomKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.kutuclient.zoom",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                KeyMapping.Category.MISC));

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("kutuclient", "hud"), new HudRenderer());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!cleanedUp) {
                cleanedUp = true;
                ZoomModule.cleanupOnStart(client);
                FullbrightModule.cleanupOnStart(client);
                KutuConfig.save();
            }
            while (menuKey.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.setScreenAndShow(new KutuMenuScreen());
                }
            }
            ZoomModule.keyHeld = zoomKey.isDown();
            ZoomModule.tick(client);
            FullbrightModule.tick(client);
            ToggleSprintModule.tick(client);
        });
    }
}
