package noobmodantor.module;

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
        return Arrays.asList(
                new Opt("biome", "Show biome", Arrays.asList("On", "Off")),
                new Opt("compass", "Compass strip", Arrays.asList("Off", "On")));
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

        if ("On".equals(opt("compass"))) {
            lines.add(compassStrip(yaw));
            lines.add("              ^");
        }

        if ("On".equals(opt("biome")) && mc.theWorld != null) {
            BiomeGenBase biome = mc.theWorld.getBiomeGenForCoords(pos);
            lines.add("Biome: " + (biome == null ? "Unknown" : biome.biomeName));
        }
        return lines;
    }

    /**
     * A 15-slot compass tape centered on the current bearing (0 = North,
     * clockwise). F3 yaw is 0 = South increasing toward West, so the
     * bearing is (yaw + 180) mod 360. Slots are 10 degrees apart:
     * cardinal letters at N/E/S/W, '+' at intercardinal points, '|'
     * otherwise; the '^' line marks the center (current heading).
     */
    private static String compassStrip(float yaw) {
        double bearing = (yaw + 180.0) % 360.0;
        int center = (int) (Math.round(bearing / 10.0) * 10);
        StringBuilder sb = new StringBuilder();
        for (int i = -7; i <= 7; i++) {
            int a = ((center + i * 10) % 360 + 360) % 360;
            char c;
            if (a == 0) c = 'N';
            else if (a == 90) c = 'E';
            else if (a == 180) c = 'S';
            else if (a == 270) c = 'W';
            else if (a % 45 == 0) c = '+';
            else c = '|';
            if (sb.length() > 0) sb.append(' ');
            sb.append(c);
        }
        return sb.toString();
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 32};
    }
}
