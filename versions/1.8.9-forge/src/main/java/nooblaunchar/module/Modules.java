package nooblaunchar.module;

import nooblaunchar.ModuleConfig;
import nooblaunchar.NoobConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ordered registry of all modules (menu order). Same ids/order as every Noob_launchar version. */
public final class Modules {
    private static final Map<String, Module> BY_ID = new LinkedHashMap<String, Module>();
    private static final List<Module> ALL = new ArrayList<Module>();

    private Modules() {
    }

    public static void init() {
        if (!ALL.isEmpty()) return;
        register(new FpsModule());
        register(new CpsModule());
        register(new KeystrokesModule());
        register(new CoordsModule());
        register(new ArmorModule());
        register(new PotionModule());
        register(new PingModule());
        register(new ClockModule());
        register(new ToggleSprintModule());
        register(new ZoomModule());
        register(new FullbrightModule());
        applyDefaults();
    }

    private static void register(Module module) {
        BY_ID.put(module.id, module);
        ALL.add(module);
    }

    /** Modules that ship disabled by default (everything else ships enabled). */
    private static void applyDefaults() {
        boolean fresh = NoobConfig.get().modules.isEmpty();
        for (Module m : ALL) {
            boolean existed = NoobConfig.get().modules.containsKey(m.id);
            ModuleConfig cfg = NoobConfig.module(m.id);
            if (!existed && (m.id.equals("toggle_sprint") || m.id.equals("fullbright"))) {
                cfg.enabled = false;
            }
        }
        if (fresh) NoobConfig.save();
    }

    public static Module get(String id) {
        return BY_ID.get(id);
    }

    public static List<Module> all() {
        return ALL;
    }
}
