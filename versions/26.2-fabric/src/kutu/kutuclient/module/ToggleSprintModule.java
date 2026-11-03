package kutu.kutuclient.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * While enabled, keeps the player sprinting whenever they move forward.
 * It only ever <em>requests</em> sprint through the same conditions vanilla
 * requires (forward input, enough food or creative mode, not using an item,
 * not sneaking); it never touches the player's sprint key binding, so
 * disabling the module instantly restores 100% vanilla behaviour.
 */
public class ToggleSprintModule extends Module {

    public ToggleSprintModule() {
        super("toggle_sprint", "Toggle Sprint");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null) return;
        Module self = Modules.get("toggle_sprint");
        if (self == null || !self.enabled()) return;
        if (mc.gui.screen() != null) return;
        if (mc.player.isSprinting()) return;
        boolean forward = mc.options.keyUp.isDown();
        boolean fedOrCreative = mc.player.getFoodData().getFoodLevel() > 6 || mc.player.getAbilities().instabuild;
        if (forward && fedOrCreative && !mc.player.isUsingItem() && !mc.player.isShiftKeyDown()) {
            mc.player.setSprinting(true);
        }
    }

    // Non-HUD module: rendering/size methods are never called.
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
    public void render(GuiGraphicsExtractor g, Minecraft mc, int x, int y) {
    }
}
