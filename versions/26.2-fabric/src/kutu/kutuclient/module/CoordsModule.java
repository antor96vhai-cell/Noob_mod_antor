package kutu.kutuclient.module;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

/** Shows XYZ coordinates, the facing direction (with axis hint), and optionally the current biome. */
public class CoordsModule extends TextModule {

    public CoordsModule() {
        super("coords", "Coordinates");
    }

    @Override
    public List<Opt> options() {
        return List.of(new Opt("biome", "Show biome", "On", "Off"));
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        List<String> lines = new ArrayList<>();
        if (mc.player == null) return lines;
        BlockPos pos = mc.player.blockPosition();
        lines.add("XYZ: " + pos.getX() + " / " + pos.getY() + " / " + pos.getZ());

        Direction dir = mc.player.getDirection();
        String facing = pretty(dir.getSerializedName());
        String axis = switch (dir) {
            case NORTH -> " (-Z)";
            case SOUTH -> " (+Z)";
            case WEST -> " (-X)";
            case EAST -> " (+X)";
            default -> "";
        };
        lines.add("Facing: " + facing + axis);

        if ("On".equals(opt("biome")) && mc.level != null) {
            Holder<Biome> biome = mc.level.getBiomeManager().getBiome(pos);
            String name = biome.unwrapKey()
                    .map(key -> pretty(key.identifier().getPath()))
                    .orElse("Unknown");
            lines.add("Biome: " + name);
        }
        return lines;
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 32};
    }
}
