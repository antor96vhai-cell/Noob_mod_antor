package nooblaunchar.module;

import net.minecraft.client.Minecraft;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Shows the real-world time (HH:MM). */
public class ClockModule extends TextModule {
    private static final DateTimeFormatter FMT_24H = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_12H = DateTimeFormatter.ofPattern("h:mm a");

    public ClockModule() {
        super("clock", "Clock");
    }

    @Override
    public List<Opt> options() {
        return Collections.singletonList(new Opt("format", "Time format", Arrays.asList("24h", "12h")));
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        LocalTime now = LocalTime.now();
        return Collections.singletonList(now.format("12h".equals(opt("format")) ? FMT_12H : FMT_24H));
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        int w = width(mc);
        int screenW = new net.minecraft.client.gui.ScaledResolution(mc).getScaledWidth();
        return new int[]{Math.max(0, screenW - w - 4), 4};
    }
}
