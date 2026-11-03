package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.util.Arrays;
import java.util.List;

/**
 * Custom crosshair drawn at the exact centre of the screen, replacing the
 * vanilla one (the vanilla crosshair is hidden by cancelling its
 * RenderGameOverlayEvent.Pre in ClientEvents). Size, gap, centre dot and
 * color (the standard module color setting) are configurable. The element
 * is always centred: {@link #pos} ignores any stored position, so the HUD
 * editor shows it in place but dragging it has no effect.
 */
public class CrosshairModule extends Module {
    private static final int THICKNESS = 2;

    public CrosshairModule() {
        super("crosshair", "Crosshair");
    }

    @Override
    public boolean supportsBackground() {
        return false;
    }

    @Override
    public List<Opt> options() {
        return Arrays.asList(
                new Opt("size", "Arm length", Arrays.asList("Small", "Medium", "Large")),
                new Opt("gap", "Gap", Arrays.asList("Small", "Medium", "Large")),
                new Opt("dot", "Center dot", Arrays.asList("On", "Off")),
                new Opt("vanilla", "Vanilla crosshair", Arrays.asList("Hidden", "Shown")));
    }

    private int armLength() {
        String s = opt("size");
        if ("Small".equals(s)) return 3;
        if ("Large".equals(s)) return 7;
        return 5;
    }

    private int gap() {
        String s = opt("gap");
        if ("Small".equals(s)) return 2;
        if ("Large".equals(s)) return 5;
        return 3;
    }

    /** True when the vanilla crosshair should be suppressed (module on + "Hidden"). */
    public static boolean shouldHideVanilla() {
        Module self = Modules.get("crosshair");
        return self != null && self.enabled() && "Hidden".equals(self.opt("vanilla"));
    }

    @Override
    public int[] pos(Minecraft mc) {
        ScaledResolution res = new ScaledResolution(mc);
        return new int[]{(res.getScaledWidth() - width(mc)) / 2, (res.getScaledHeight() - height(mc)) / 2};
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return pos(mc);
    }

    @Override
    public int width(Minecraft mc) {
        return 2 * (armLength() + gap()) + THICKNESS;
    }

    @Override
    public int height(Minecraft mc) {
        return width(mc);
    }

    @Override
    public void render(Minecraft mc, int x, int y) {
        int color = textColor();
        int l = armLength();
        int g = gap();
        int t = THICKNESS;
        int c0 = l + g; // offset of the centre block
        // Top and bottom arms
        fill(x + c0, y, t, l, color);
        fill(x + c0, y + c0 + t + g, t, l, color);
        // Left and right arms
        fill(x, y + c0, l, t, color);
        fill(x + c0 + t + g, y + c0, l, t, color);
        // Centre dot
        if ("On".equals(opt("dot"))) {
            fill(x + c0, y + c0, t, t, color);
        }
    }
}
