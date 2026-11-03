package kutu.kutuclient.module;

import kutu.kutuclient.KutuConfig;
import kutu.kutuclient.ModuleConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ordered registry of all modules (menu order). */
public final class Modules {
    private static final Map<String, Module> BY_ID = new LinkedHashMap<>();
    private static final List<Module> ALL = new ArrayList<>();

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
        boolean fresh = KutuConfig.get().modules.isEmpty();
        for (Module m : ALL) {
            boolean existed = KutuConfig.get().modules.containsKey(m.id);
            ModuleConfig cfg = KutuConfig.module(m.id);
            if (!existed && (m.id.equals("toggle_sprint") || m.id.equals("fullbright"))) {
                cfg.enabled = false;
            }
        }
        if (fresh) KutuConfig.save();
    }

    public static Module get(String id) {
        return BY_ID.get(id);
    }

    public static List<Module> all() {
        return ALL;
    }
}
