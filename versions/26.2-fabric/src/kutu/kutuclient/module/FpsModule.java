package kutu.kutuclient.module;

import net.minecraft.client.Minecraft;

import java.util.List;

/** Shows the current client FPS (Minecraft#getFps). */
public class FpsModule extends TextModule {

    public FpsModule() {
        super("fps", "FPS Counter");
    }

    @Override
    public List<Opt> options() {
        return List.of(new Opt("label", "Show \"FPS\" label", "On", "Off"));
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        int fps = mc.getFps();
        return List.of("On".equals(opt("label")) ? "FPS: " + fps : Integer.toString(fps));
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 4};
    }
}
