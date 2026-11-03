package noobmodantor.module;

import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Client-visual weather override. While set to Always Clear or Always
 * Rain, the local world's rain/thunder *rendering strengths* are pinned
 * every client tick (after the world tick, before rendering), so rain
 * particles, rain sound and the darkened sky follow the choice on this
 * screen only. Nothing is sent to the server: the server's actual
 * weather, other players and gameplay are completely unaffected, and
 * "Server default" hands control straight back to vanilla. Ships
 * disabled by default.
 */
public class WeatherModule extends Module {

    public WeatherModule() {
        super("weather", "Weather Changer");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("mode", "Weather",
                Arrays.asList("Server default", "Always Clear", "Always Rain")));
    }

    public static void tick(Minecraft mc) {
        WeatherModule self = (WeatherModule) Modules.get("weather");
        if (self == null || !self.enabled()) return;
        if (mc.theWorld == null) return;
        String mode = self.opt("mode");
        if ("Always Clear".equals(mode)) {
            mc.theWorld.setRainStrength(0.0f);
            mc.theWorld.setThunderStrength(0.0f);
        } else if ("Always Rain".equals(mode)) {
            mc.theWorld.setRainStrength(1.0f);
            mc.theWorld.setThunderStrength(0.0f);
        }
        // "Server default": touch nothing.
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
