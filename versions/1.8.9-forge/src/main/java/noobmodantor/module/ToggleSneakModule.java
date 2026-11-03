package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

/**
 * Toggle Sneak: pressing the sneak key once latches sneaking on; pressing
 * it again releases it. The latch is applied by driving the sneak key
 * binding's own state ({@link KeyBinding#setKeyBindState}), so the vanilla
 * movement code does all the work exactly as if the key were held — no
 * player fields are modified directly. Toggling off, or disabling the
 * module, restores the binding to the physical key state. Ships disabled
 * by default, like Toggle Sprint.
 */
public class ToggleSneakModule extends Module {
    private static boolean toggled = false;

    public ToggleSneakModule() {
        super("toggle_sneak", "Toggle Sneak");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    public static void tick(Minecraft mc) {
        if (mc.gameSettings == null) return;
        KeyBinding sneak = mc.gameSettings.keyBindSneak;
        Module self = Modules.get("toggle_sneak");
        if (self == null || !self.enabled()) {
            if (toggled) {
                toggled = false;
                KeyBinding.setKeyBindState(sneak.getKeyCode(), Keyboard.isKeyDown(sneak.getKeyCode()));
            }
            return;
        }
        if (mc.thePlayer == null) return;
        if (sneak.isPressed()) {
            toggled = !toggled;
            if (!toggled) {
                KeyBinding.setKeyBindState(sneak.getKeyCode(), Keyboard.isKeyDown(sneak.getKeyCode()));
            }
        }
        if (toggled) {
            // Re-assert every tick: vanilla clears binding states when some
            // screens open, and the latch must survive that.
            KeyBinding.setKeyBindState(sneak.getKeyCode(), true);
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
