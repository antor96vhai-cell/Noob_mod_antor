package noobmodantor.module;

import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Shows the current client FPS (Minecraft.getDebugFPS). */
public class FpsModule extends TextModule {

    public FpsModule() {
        super("fps", "FPS Counter");
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("label", "Show \"FPS\" label", Arrays.asList("On", "Off")));
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        int fps = Minecraft.getDebugFPS();
        return Collections.singletonList("On".equals(opt("label")) ? "FPS: " + fps : Integer.toString(fps));
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 4};
    }
}
