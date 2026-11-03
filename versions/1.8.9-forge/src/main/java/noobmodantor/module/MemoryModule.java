package noobmodantor.module;

import net.minecraft.client.Minecraft;

import java.util.Collections;
import java.util.List;

/** Shows the JVM memory usage (used / max heap) reported by the Runtime. */
public class MemoryModule extends TextModule {

    public MemoryModule() {
        super("memory", "Memory Usage");
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L);
        long maxMb = rt.maxMemory() / (1024L * 1024L);
        long pct = maxMb > 0 ? usedMb * 100L / maxMb : 0L;
        return Collections.singletonList("Memory: " + usedMb + "/" + maxMb + " MB (" + pct + "%)");
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 140};
    }
}
