package nooblaunchar.module;

import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Shows XYZ coordinates, the facing direction (F3 convention, from rotationYaw), and optionally the biome. */
public class CoordsModule extends TextModule {
    // Indexed by floor(yaw / 90 + 0.5) & 3 with F3 yaw convention: 0=South(+Z), 90=West(-X), 180=North(-Z), 270=East(+X)
    private static final String[] DIRECTIONS = {"South", "West", "North", "East"};
    private static final String[] AXIS = {" (+Z)", " (-X)", " (-Z)", " (+X)"};

    public CoordsModule() {
        super("coords", "Coordinates");
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("biome", "Show biome", Arrays.asList("On", "Off")));
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        List<String> lines = new ArrayList<String>();
        if (mc.thePlayer == null) return lines;
        BlockPos pos = mc.thePlayer.getPosition();
        lines.add("XYZ: " + pos.getX() + " / " + pos.getY() + " / " + pos.getZ());

        float yaw = mc.thePlayer.rotationYaw % 360.0f;
        if (yaw < 0) yaw += 360.0f;
        int idx = MathHelper.floor_double(yaw / 90.0 + 0.5) & 3;
        lines.add("Facing: " + DIRECTIONS[idx] + AXIS[idx]);

        if ("On".equals(opt("biome")) && mc.theWorld != null) {
            BiomeGenBase biome = mc.theWorld.getBiomeGenForCoords(pos);
            lines.add("Biome: " + (biome == null ? "Unknown" : biome.biomeName));
        }
        return lines;
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 32};
    }
}
