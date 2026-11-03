package nooblaunchar.module;

import net.minecraft.client.Minecraft;

import java.util.List;

/** Convenience base for HUD modules that render one or more plain text lines. */
public abstract class TextModule extends Module {

    protected TextModule(String id, String name) {
        super(id, name);
    }

    protected abstract List<String> lines(Minecraft mc);

    @Override
    public int width(Minecraft mc) {
        return linesWidth(lines(mc), mc.fontRendererObj);
    }

    @Override
    public int height(Minecraft mc) {
        return linesHeight(Math.max(1, lines(mc).size()));
    }

    @Override
    public void render(Minecraft mc, int x, int y) {
        renderLines(mc, x, y, lines(mc));
    }
}
