package nooblaunchar;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import nooblaunchar.hud.ClientEvents;
import nooblaunchar.module.Modules;
import org.lwjgl.input.Keyboard;

/**
 * Noob_launchar — a lightweight, original in-game HUD/menu client mod for
 * Minecraft 1.8.9 (Forge). Pure client-side HUD and menu features; no
 * authentication, login or account code of any kind.
 *
 * <ul>
 *   <li>Right Shift opens the module menu; C (hold) zooms.</li>
 *   <li>All settings persist in config/nooblaunchar.json.</li>
 * </ul>
 */
@Mod(modid = NoobLaunchar.MODID, name = "Noob_launchar", version = NoobLaunchar.VERSION,
        clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class NoobLaunchar {
    public static final String MODID = "nooblaunchar";
    public static final String VERSION = "1.0.0";

    public static KeyBinding menuKey;
    public static KeyBinding zoomKey;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NoobConfig.init(event.getModConfigurationDirectory());
        Modules.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        menuKey = new KeyBinding("key.nooblaunchar.menu", Keyboard.KEY_RSHIFT, "Noob_launchar");
        zoomKey = new KeyBinding("key.nooblaunchar.zoom", Keyboard.KEY_C, "Noob_launchar");
        ClientRegistry.registerKeyBinding(menuKey);
        ClientRegistry.registerKeyBinding(zoomKey);
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }
}
