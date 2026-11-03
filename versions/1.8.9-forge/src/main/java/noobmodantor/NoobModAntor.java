package noobmodantor;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import noobmodantor.hud.ClientEvents;
import noobmodantor.module.Modules;
import org.lwjgl.input.Keyboard;

/**
 * Noob_mod_antor — a lightweight, original in-game HUD/menu client mod for
 * Minecraft 1.8.9 (Forge). Pure client-side HUD and menu features; no
 * authentication, login or account code of any kind.
 *
 * <ul>
 *   <li>Right Shift opens the module menu; C (hold) zooms.</li>
 *   <li>All settings persist in config/noob_mod_antor.json.</li>
 * </ul>
 */
@Mod(modid = NoobModAntor.MODID, name = "Noob_mod_antor", version = NoobModAntor.VERSION,
        clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class NoobModAntor {
    public static final String MODID = "noob_mod_antor";
    public static final String VERSION = "1.1.1";

    public static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger("Noob_mod_antor");

    public static KeyBinding menuKey;
    public static KeyBinding zoomKey;
    public static KeyBinding freelookKey;
    public static KeyBinding freecamKey;
    public static KeyBinding behindKey;
    public static KeyBinding copyChatKey;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NoobConfig.init(event.getModConfigurationDirectory());
        Modules.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        menuKey = new KeyBinding("key.noobmodantor.menu", Keyboard.KEY_RSHIFT, "Noob_mod_antor");
        zoomKey = new KeyBinding("key.noobmodantor.zoom", Keyboard.KEY_C, "Noob_mod_antor");
        freelookKey = new KeyBinding("key.noobmodantor.freelook", Keyboard.KEY_LMENU, "Noob_mod_antor");
        freecamKey = new KeyBinding("key.noobmodantor.freecam", Keyboard.KEY_F6, "Noob_mod_antor");
        behindKey = new KeyBinding("key.noobmodantor.behindyou", Keyboard.KEY_F8, "Noob_mod_antor");
        copyChatKey = new KeyBinding("key.noobmodantor.copychat", Keyboard.KEY_F7, "Noob_mod_antor");
        ClientRegistry.registerKeyBinding(menuKey);
        ClientRegistry.registerKeyBinding(zoomKey);
        ClientRegistry.registerKeyBinding(freelookKey);
        ClientRegistry.registerKeyBinding(freecamKey);
        ClientRegistry.registerKeyBinding(behindKey);
        ClientRegistry.registerKeyBinding(copyChatKey);
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        // Ship the built-in FPS resource pack into the game's resourcepacks
        // folder (first run only; the user enables it in Options).
        FpsPack.extract(net.minecraft.client.Minecraft.getMinecraft().mcDataDir);
    }
}
